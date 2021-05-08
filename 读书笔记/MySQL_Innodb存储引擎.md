### 一、MySQL体系结构



基本概念：

> 数据库：物理操作系统文件和其他文件类型的集合。
>
> 实例：MySQL数据库有后台线程以及共享内存区构成，实例在系统上就是一个进程，用来操作数据库文件。

配置文件：

![1610079489890](..\resource\mysql启动文件顺序.png)

> MySQL启动时以以上顺序读取配置文件，并以最后一个文件为准

#### 1. MySQL体系结构

![img](..\resource\mysql体系架构.png)



**连接池组件**：将连接缓存，下次可以直接用已经建立好的连接，减少创建连接时间，提升服务器性能。

**管理服务和工具组件**：系统管理和控制工具，例如备份恢复、Mysql复制、集群等

**SQL接口组件**：接受用户的SQL命令，并且返回用户需要查询的结果。比如select from就是调用SQL Interface

**查询分析器组件:** SQL命令传递到解析器的时候会被解析器验证和解析。

**优化器组件**：查询优化器，SQL语句在查询之前会使用查询优化器对查询进行优化。

**缓冲组件：** 查询缓存，如果查询缓存有命中的查询结果，查询语句就可以直接去查询缓存中取数据。

**插件式存储引擎：**MySQL中 **存储引擎是基于表**

**物理文件**

#### 2.存储引擎

##### 2.1 InnoDB存储引擎

特点：支持事务、行锁、外键、类Oracle的非锁定读（默认读操作不会产生锁）、多版本并发控制（MVCC）获得高并发、4种隔离级别（默认repeatable）、next-keylocking避免幻读

高性能高可用功能：插入缓冲（insert buffer）、二次写（double write）、自适应哈希索引（adaptive hash index）、预读（read ahead）......

表数据存储：聚集方式-----按主键顺序进行存放（如没有定义主键，默认生成6字节的rowid作为主键）

##### 2.2 MyISAM存储引擎

特点：不支持事务，表锁、全文索引、缓冲池只缓存索引文件，不缓冲数据文件

存储：MYD（数据）和MYI（索引）文件

##### 常用存储引擎对比

![](..\resource\mysql存储引擎对比.png)

### 二、InnoDB存储引擎

#### 1. InnoDB体系架构

![img](..\resource\innodb体系架构.jpg)

InnoDB体系结构包括：内存池、后台线程以及存储文件

**内存池：**由多个内存块组成的，主要包括缓存磁盘数据、redo log缓冲等

**后台线程：**负责刷新内存池中的数据，Master Thread、IO Thread以及Purge Thread等；

**存储文件：**一般包括表结构文件（.frm）、共享表空间文件（ibdata1）、独占表空间文件（ibd）以及日志文件（redo文件等）等



#### 2.内存

##### 2.1 缓冲池

InnoDB 基于磁盘，按页管理。需要使用缓冲技术提高性能。

引入缓冲后InnoDB数据流程：

> - 查询：判断是否存在缓冲池中？命中，读取缓冲池：读磁盘
> - 修改：先修改在缓冲池中的页，然后按一定的频率**（checkpoint机制）**刷新至磁盘

**参数：**

>  innodb_buffer_pool_size 缓冲池大小
>
> innodb_buffer_pool_instances 缓冲池实例个数，默认 1  (允许有多个缓冲池实例，页通过哈希值平均分配)
>
> innodb_old_blocks_pct   midpoint位置，默认37(百分比)    new  midpoint  old  
>
> innodb_old_blocks_time  多长时间后，新增页才能放入热端

**命令/表：**

> show engine innodb status : 查看InnoDB引擎的所有详细信息
>
> information_schema.innodb_buffer_pool_stats 缓冲状态表

**构成：**索引页、数据页、undo页、插入缓冲、自适应哈希索引、InnoDB的锁信息、数据字典等

###### 2.1.1 数据页：LRU LIST、FREE LIST、FLUSH LIST

**LRU算法**(Latest Recent Used)：使用频繁在前，使用最少在尾

> ​	算法优化：新增页放置在midpoint位置；加入热端时间限制(innodb_old_blocks_time)
>
> ​	为什么优化：某些SQL操作可能会是缓冲池中的数据刷出，影响缓冲池效率。eg：索引或数据的扫描
>
> LRU过程：
>
> - 数据库启动，LRU空，页都存放在free list 
> - 需要从缓冲池中分配页时，free中有空页？free-1 lru+1：lru淘汰机制
>
> 过程中 old-->new  称为 page made young ，因为innodb_old_blocks_time限制没有从old-->new 称为 page not made young









#### 3.后台线程

**作用**：

> - 刷新内存池中的数据，保证缓存数据是最近常用的
> - 将已修改的数据文件刷新至磁盘文件
> - 保证数据库发生异常情况下InnoDB能恢复正常

**分类：**

> - Master Thread : 缓冲池异步刷新至磁盘，保证数据一致性，脏页刷新、合并插入缓冲、undo回收
> - IO Thread ：负责IO的回调处理
> - Purge Thread ：事务提交后，回收undo页
> - Page Cleaner Thread ：脏页刷新

