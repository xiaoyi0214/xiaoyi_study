## ClickHouse

### 一、相关概念

#### 1. OLAP | OLTP

OLAP（On-Line Analytical Processing）联机分析处理，聚焦于查

OLTP（on-line transaction processing）联机事务处理，聚焦于数据增删改

![1610435268731](..\resource\OLAP-OLTP.png)

OLAP特点：

- 读多于写
- 大宽表--读大量行但少量列，结果集较小
- 数据批量写入，且数据不更新或少量更新
- 无需事务，数据一致性要求低
- 灵活多变，不适合预先建模



#### 2.Row-oriented DBMS || Column-oriented DBMS 

[https://ericfu.me/columnar-storage-overview-storage/](https://ericfu.me/columnar-storage-overview-storage/)

##### 2.1 行式存储与OLTP

传统OLTP数据库通常采用行式存储，所有列依次排列构成一行，以行为单位存储，再配合以 B+ 树或 SS-Table 作为索引，就能快速通过主键找到相应的行数据。

![1610446925342](..\resource\行式存储.png)

应用场景：以实体entity为单位，大多增删改查一整行记录，把一行数据存在物理内存中相邻的位置比较好

##### 2.2 列式存储与OLAP

对于 OLAP 场景，采用行式存储的话一个查询需要遍历整个表，进行分组、排序、聚合等操作，并且常常不会用到所有的列，而仅仅对其中某些感兴趣的列做运算。

列式存储同一列数据被存储在胃里内存相邻位置，相对来说更有优势

![1610447274360](..\resource\列式存储.png)

优点：

- 当查询语句只涉及部分列时，只需要扫描相关的列
- 每一列的数据都是相同类型的，彼此间相关性更大，对列数据压缩的效率较高

对比

> 1）如前所述，分析场景中往往需要读大量行但是少数几个列。在行存模式下，数据按行连续存储，所有列的数据都存储在一个block中，不参与计算的列在IO时也要全部读出，读取操作被严重放大。而列存模式下，只需要读取参与计算的列即可，极大的减低了IO cost，加速了查询。
>
> 2）同一列中的数据属于同一类型，压缩效果显著。列存往往有着高达十倍甚至更高的压缩比，节省了大量的存储空间，降低了存储成本。
>
> 3）更高的压缩比意味着更小的data size，从磁盘中读取相应数据耗时更短。
>
> 4）自由的压缩算法选择。不同列的数据具有不同的数据类型，适用的压缩算法也就不尽相同。可以针对不同列类型，选择最合适的压缩算法。
>
> 5）高压缩比，意味着同等大小的内存能够存放更多数据，系统cache效果更好。



##### 2.3 列式存储设计思想

1. **跳过无关的数据**。从行存到列存，就是消除了无关列的扫描；ORC 中通过三层索引信息，能快速跳过无关的数据分片。
2. **编码既是压缩，也是索引**。Dremel 中用精巧的嵌套编码避免了大量 NULL 的出现；C-Store 对 distinct 值的编码同时也是对 distinct 值的索引；PowerDrill 则将字典编码用到了极致（见下一篇文章）。
3. **假设数据不可变**。无论 C-Store、Dremel 还是 ORC，它们的编码和压缩方式都完全不考虑数据更新。如果一定要有更新，暂时写到别处、读时合并即可。
4. **数据分片**。处理大规模数据，既要纵向切分也要横向切分，不必多说





### 二、ClickHouse

#### 1. 基础知识

##### 1.1 应用场景及限制

应用：

> 1. 绝大多数请求都是用于读访问
> 2. 数据没有更新或以大批次更新
> 3. 数据只写入数据库，不涉及修改
> 4. 大宽表--读大量行只用少量列
> 5. 用于简单查询，频率低(小于百次/s)，延迟50毫秒内
> 6. 列值比较小的数值或字符串
> 7. 处理当个查询时需要高吞吐量(每台服务器每秒十亿行...)
> 8. 不需事务
> 9. 数据一致性要求低
> 10. 每次查询中只会查询一个大表(可有其余小表)
> 11. 查询结果经过过滤/聚合，返回结果不大于单个服务器内存大小

限制：

> 1. 不支持真正删除/更新支持 不支持事务??
> 2. 不支持二级索引
> 3. 有限的SQL支持，join实现方式不同
> 4. 不支持窗口功能
> 5. 元数据管理需要人工干预维护

##### 1.2 常用SQL语法

```sql
show databases; -- 列出数据库列表
show tables; -- 列出数据库表
create database test; -- 创建数据库
drop table if exists test.t1; -- 删除表
create /*temporary*/ table /*if not exists*/ test.t1(
    id UInt16,
    name String
)ENGINE = Memory;

insert into test.t1(id,name) values (1,'aaa'),(2,'bbb');
select * from test.t1;

```

##### 1.3 数据类型

https://clickhouse.tech/docs/en/sql-reference/data-types/

物化列：materialized 值自己计算出来，不能从insert中获取，会保存，不会出现在select * 结果中

```sql
create table test.m2 (
    a MATERIALIZED (b+1),
    b UInt16
) ENGINE = Memory;
```

表达式列：alias  自己计算，不会保存，每次查询时候计算一次

```sql
create table test.m3 (
    a ALIAS (b+1),
    b UInt16
) ENGINE = Memory;
```

#### 2. 引擎

##### 2.1  Tinylog

最简单的一种引擎，每一列保存为一个文件，里面的内容是压缩过的，不支持索引
这种引擎没有并发控制，所以，当你需要在读，又在写时，读会出错。并发写，内容都会坏掉。

```sql
create table test.tinylog (
    a UInt16, b UInt16
) ENGINE = TinyLog;
```

应用场景:

> a. 基本上就是那种只写一次
> b. 然后就是只读的场景。
> c. 不适用于处理量大的数据，官方推荐，使用这种引擎的表最多 100 万行的数据

文件：

```css
├── a.bin
├── b.bin
└── sizes.json
a.bin 和 b.bin 是压缩过的对应的列的数据， sizes.json 中记录了每个 *.bin 文件的大小
```

##### 2.2 Log

跟 TinyLog 基本一致，不支持索引，加了一个 __marks.mrk 文件，里面记录了每个数据块的偏移，准确地切分读的范围，从而使用**并发读**取成为可能。

但是不支持并发写，写操作会阻塞其他操作

```sql
create table test.log (
    a UInt16, 
    b UInt16
) ENGINE = Log;
```

应用场景:同 TinyLog 差不多

```css
├── __marks.mrk
├── a.bin
├── b.bin
└── sizes.json
```

##### 2.3 Memory

内存引擎，数据以未压缩的原始形式直接保存在内存当中，服务器重启数据就会消失

可以并行读，读写互斥锁的时间也非常短

不支持索引，简单查询下有非常非常高的性能表现

应用场景:

> a. 进行测试
>
> b. 在需要非常高的性能，同时数据量又不太大（上限大概 1 亿行）的场景



##### 2.4 Merge

工具引擎，本身不保存数据，只用于把指定库中的指定多个表链在一起

读取操作可以并发执行，同时也可以利用原表的索引，但是，此引擎不支持写操作
指定引擎的同时，需要指定要链接的库及表，库名可以使用一个表达式，表名可以使用正则表达式指定

```sql
create table test.tinylog1 (id UInt16, name String) ENGINE=TinyLog;
create table test.tinylog2 (id UInt16, name String) ENGINE=TinyLog;
create table test.tinylog3 (id UInt16, name String) ENGINE=TinyLog;

insert into test.tinylog1(id, name) values (1, 'tinylog1');
insert into test.tinylog2(id, name) values (2, 'tinylog2');
insert into test.tinylog3(id, name) values (3, 'tinylog3');

use test;
create table test.merge (id UInt16, name String) ENGINE=Merge(currentDatabase(), '^tinylog[0-9]+');
select _table,* from test.merge order by id desc
```

`注：`_table 这个列表示数据来源表，使用 Merge 多出来的虚拟列，不包含在select *



##### 2.5 Distributed

与 Merge 类似， Distributed 也是通过一个逻辑表，去访问各个物理表

```sql
Distributed(remote_group, database, table [, sharding_key])

remote_group /etc/clickhouse-server/config.xml中remote_servers参数
database 是各服务器中的库名
table 是表名
sharding_key 是一个寻址表达式，可以是一个列名，也可以是像 rand() 之类的函数调用，它与 remote_servers 中的 weight 共同作用，决定在 写 时往哪个 shard 写

```

##### 2.6 Null

空引擎，写入的任何数据都会被忽略，读取的结果一定是空

虽然数据本身不会被存储，但是结构上的和数据格式上的约束还是跟普通表一样是存在的，同时，你也可以在这个引擎上创建视图



##### 2.7 Buffer

`1.`Buffer 引擎，像是Memory 存储的一个上层应用似的（磁盘上也是没有相应目录的）
`2.`它的行为是一个缓冲区，写入的数据先被放在缓冲区，达到一个阈值后，这些数据会自动被写到指定的另一个表中
`3.`和Memory 一样，有很多的限制，比如没有索引
`4.`Buffer 是接在其它表前面的一层，对它的读操作，也会自动应用到后面表，但是因为前面说到的限制的原因，一般我们读数据，就直接从源表读就好了，缓冲区的这点数据延迟，只要配置得当，影响不大的
`5.`Buffer 后面也可以不接任何表，这样的话，当数据达到阈值，就会被丢弃掉

> 如果一次写入的数据太大或太多，超过了 max 条件，则会直接写入源表。
>
> 删源表或改源表的时候，建议 Buffer 表删了重建。
>
> “友好重启”时， Buffer 数据会先落到源表，“暴力重启”， Buffer 表中的数据会丢失。
>
> 即使使用了 Buffer ，多次的小数据写入，对比一次大数据写入，也 慢得多

```sql
-- 创建源表
create table test.mergetree (sdt  Date, id UInt16, name String, point UInt16) ENGINE=MergeTree(sdt, (id, name), 10);
-- 创建 Buffer表
-- Buffer(database, table, num_layers, min_time, max_time, min_rows, max_rows, min_bytes, max_bytes)
/*
database 数据库
table 源表，这里除了字符串常量，也可以使用变量的。
num_layers 是类似“分区”的概念，每个分区的后面的 min / max 是独立计算的，官方推荐的值是 16 。
min / max 这组配置荐，就是设置阈值的，分别是 时间（秒），行数，空间（字节）。

阈值的规则: 是“所有的 min 条件都满足， 或 至少一个 max 条件满足”。
*/
create table test.mergetree_buffer as test.mergetree ENGINE=Buffer(test, mergetree, 16, 3, 20, 2, 10, 1, 10000);



```

##### 2.8 Set

只用在 IN 操作符右侧，你不能对它 select

Set 引擎表，是全内存运行的，但是相关数据会落到磁盘上保存，启动时会加载到内存中。所以，意外中断或暴力重启，是可能产生数据丢失问题的

```sql
create table test.set(id UInt16, name String) ENGINE=Set;
insert into test.set(id, name) values (1, 'hello');
-- select 1 where (1, 'hello') in test.set; -- 默认UInt8 需要手动进行类型转换
select 1 where (toUInt16(1), 'hello') in test.set;
```



##### 2.9 Join

##### 2.10 MergeTree

支持`一个日期和一组主键的两层式索引`，还可以`实时更新数据`。同时，索引的粒度可以自定义，外加直接支持采样功能

```sql
MergeTree(EventDate, (CounterID, EventDate), 8192)
MergeTree(EventDate, intHash32(UserID), (CounterID, EventDate, intHash32(UserID)), 8192)
/*
EventDate 一个日期的列名
intHash32(UserID) 采样表达式
(CounterID, EventDate) 主键组（里面除了列名，也支持表达式），也可以是一个表达式
8192 主键索引的粒度
*/

drop table if exists test.mergetree1;
create table test.mergetree1 (
    sdt  Date, 
    id UInt16, 
    name String, 
    cnt UInt16
) ENGINE=MergeTree(sdt, (id, name), 10);

-- 日期的格式，好像必须是 yyyy-mm-dd
insert into test.mergetree1(sdt, id, name, cnt) values ('2018-06-01', 1, 'aaa', 10);
insert into test.mergetree1(sdt, id, name, cnt) values ('2018-06-02', 4, 'bbb', 10);
insert into test.mergetree1(sdt, id, name, cnt) values ('2018-06-03', 5, 'ccc', 11);
```



##### 2.11 ReplacingMergeTree

`1`.在 MergeTree 的基础上，添加了“处理重复数据”的功能=>实时数据场景
`2`.相比 MergeTree ,ReplacingMergeTree 在最后加一个"版本列",它跟时间列配合一起，用以区分哪条数据是"新的"，并把旧的丢掉(这个过程是在 merge 时处理，不是数据写入时就处理了的，平时重复的数据还是保存着的，并且查也是跟平常一样会查出来的)
`3`.主键列组用于区分重复的行

```sql
-- 版本列 允许的类型是， UInt 一族的整数，或 Date 或 DateTime
create table test.replacingmergetree (
    sdt  Date, 
    id UInt16, 
    name String, 
    cnt UInt16
) ENGINE=ReplacingMergeTree(sdt, (name), 10, cnt);

insert into test.replacingmergetree (sdt, id, name, cnt) values ('2018-06-10', 1, 'a', 20);
insert into test.replacingmergetree (sdt, id, name, cnt) values ('2018-06-10', 1, 'a', 30);
insert into test.replacingmergetree (sdt, id, name, cnt) values ('2018-06-11', 1, 'a', 20);
insert into test.replacingmergetree (sdt, id, name, cnt) values ('2018-06-11', 1, 'a', 30);
insert into test.replacingmergetree (sdt, id, name, cnt) values ('2018-06-11', 1, 'a', 10);

select * from test.replacingmergetree;

-- 如果记录未执行merge，可以手动触发一下 merge 行为
optimize table test.replacingmergetree;
```

##### 2.12 SummingMergeTree

`1`.SummingMergeTree 就是在 merge 阶段把数据sum求和
`2`.sum求和的列可以指定，不可加的未指定列，会取一个最先出现的值

```sql
create table test.summingmergetree (sdt Date, name String, a UInt16, b UInt16) ENGINE=SummingMergeTree(sdt, (sdt, name), 8192, (a));

insert into test.summingmergetree (sdt, name, a, b) values ('2018-06-10', 'a', 1, 20);
insert into test.summingmergetree (sdt, name, a, b) values ('2018-06-10', 'b', 2, 11);
insert into test.summingmergetree (sdt, name, a, b) values ('2018-06-11', 'b', 3, 18);
insert into test.summingmergetree (sdt, name, a, b) values ('2018-06-11', 'b', 3, 82);
insert into test.summingmergetree (sdt, name, a, b) values ('2018-06-11', 'a', 3, 11);
insert into test.summingmergetree (sdt, name, a, b) values ('2018-06-12', 'c', 1, 35);

-- 手动触发一下 merge 行为
optimize table test.summingmergetree;

select * from test.summingmergetree;
```



##### 2.13 AggregatingMergeTree

AggregatingMergeTree 是在 MergeTree 基础之上，针对聚合函数结果，作增量计算优化的一个设计，它会在 merge 时，针对主键预处理聚合的数据
应用于AggregatingMergeTree 上的聚合函数除了普通的 sum, uniq等，还有 sumState , uniqState ，及 sumMerge ， uniqMerge 这两组

##### 2.14 CollapsingMergeTree

专门为 OLAP 场景下，一种“变通”存数做法而设计的，在数据是不能改，更不能删的前提下，通过“运算”的方式，去抹掉旧数据的影响，把旧数据“减”去即可，从而解决"最终状态"类的问题，比如 `当前有多少人在线？`

“以加代删”的增量存储方式，带来了聚合计算方便的好处，代价却是存储空间的翻倍，并且，对于只关心最新状态的场景，中间数据都是无用的

CollapsingMergeTree 在创建时与 MergeTree 基本一样，除了最后多了一个参数，需要指定 Sign 位（必须是 Int8 类型）

```sql
create table test.collapsingmergetree(sign Int8, sdt Date, name String, cnt UInt16) ENGINE=CollapsingMergeTree(sdt, (sdt, name), 8192, sign);
```

