### 一、redis数据类型

​	Redis主要有5种数据类型，包括String、List、Set、Zset、Hash

![1609759109899](..\resource\1609759109899.png)

常用命令：http://redisdoc.com/index.html

String

```shell
set hello world // 添加key为 hello，成功返回ok
get hello 	// 获取key 为hello
del hello	// 删除key 为hello，成功返回1
```

List

```shell
rpush list item  //list中尾部添加 item，返回list长度
lpush list item1 //list中头部添加 item1
lrange list start stop //获取从start到stop的数据，当stop为-1获取全部
lindex list 1 // 获取index为1的数据
lpop list // 从左侧移除数据，返回移除的元素
rpop list // 从右侧移除数据
```

Set

```shell
sadd set-key item // set-key中增加item，成功返回1
sadd set-key item // set-key中已存在，返回0
smembers set-key //获取set-key中所有数据
sismember set-key item2 // 判断item2是否存在于set-key中，存在 0
srem set-key item //从set-key中移除值为item，成功1，失败、不存在0
```

Hash

```shell
hset hash-key sub-key1 vakue1 // 在hash-key中增加/修改 sub-key1 ： value1 键值对，若新增返回1，修改返回0
hset hash-key sub-key2 value2
hgetall hash-key // 获得hash-key所有键值对
hdel hash-key sub-key2 // 删除hash-key中sub-key2键值对，成功返回1，不存在返回0
hget hash-key sub-key1 // 获得hash-key中sub-key1对应的值
```

Zset

```shell
zadd zset-key 100 member1 // 在zset-key中增加/修改 100 ： member1 键值对，新增返回1，修改返回0
zadd zset-key 200 member2 
zrange zset-key start stop [withscores] // 返回zset-key指定区间内集合，withscores表示带key返回
zrevrange zset-key start stop [withscores] // 按从大到小的顺序返回zset-key指定区间内集合
zrangebyscore zset-key min max [withscores] // 返回在min和max区间内的值
zrem zset-key member1 // 删除momber1
```



### 二、常见使用场景

计数器：对string进行自增自减运算，实现计数器功能。（redis内存行数据库读写性能非常高，适合存储频繁读写的计数量）

缓存：将热点数据存放在内存中，并设置内存最大使用量及淘汰策略保证缓存命中率

会话缓存：存储多台应用服务器的会话信息（当应用服务器不在存储用户会话信息，也就不在具有状态，一个用户可以请求任意一个应用服务器，从而更容易实现高可用及可伸缩性）

全页缓存FPC：以Magento为例，Magento提供一个插件来使用Redis作为全页缓存后端。对WordPress的用户来说，Pantheon有一个非常好的插件 wp-redis，这个插件能帮助你以最快速度加载你曾浏览过的页面

查找表：DNS 记录就很适合使用 Redis 进行存储。

消息队列：List 是一个双向链表，可以通过 lpush 和 rpop 写入和读取消息，不过最好使用kafka、rabbitMQ等中间件

分布式锁：分布式情况下，可使用Redis自带的SETNX命令实现分布式锁，还可以使用官方提供的RedLock

其他：set可以实现交集并集操作，从而实现共同好友功能

​			zset实现有序性操作，从而实现排行榜等功能



### 三、持久化问题

Redis是内存型数据库，为了防止系统故障/重用数据，需要将内存中数据持久化到硬盘上

默认开启：RDB

重启时，优先使用AOF文件还原数据

#### 1.RDB持久化（快照）

解释：将某个时间节点的所有数据备份到硬盘中

优点：可以将快照复制到其他服务器从而创建相同数据的服务器副本

缺点：如果系统发生故障，会丢失最后一次创建快照后的数据；如果数据量大，保存快照时间会很长

适用：即使丢失一部分数据也不会造成大问题

redis.conf配置文件中配置：

```properties
# dir 文件位置
# dbfilename 文件名
#在900秒(15分钟)之后，如果至少有1个key发生变化，Redis就会自动触发BGSAVE命令创建快照。
save 900 1              

#在300秒(5分钟)之后，如果至少有10个key发生变化，Redis就会自动触发BGSAVE命令创建快照。
save 300 10            

#在60秒(1分钟)之后，如果至少有10000个key发生变化，Redis就会自动触发BGSAVE命令创建快照。
save 60 10000    
```

创建快照的方法：

1. **BGSAVE命令**
   对于支持BGSAVE命令的平台来说（基本上所有平台支持，除了Windows平台），Redis会调用fork来创建一个子进程，然后子进程负责将快照写入硬盘，而父进程则继续处理命令请求

2. **SAVE命令**

   接到SAVE命令的Redis服务器在快照创建完毕之前不会再响应任何其他命令。

   SAVE命令不常用，我们通常只会在没有足够内存去执行BGSAVE命令的情况下，又或者即使等待持久化操作执行完毕也无所谓的情况下

3. **save选项**

   如果用户设置了save选项（一般会默认设置），redis会在save条件满足时自动调用BGSAVE命令

4. **SHUTDOWN命令**

   当Redis通过SHUTDOWN命令接收到关闭服务器的请求时，或者接收到标准TERM信号时，会执行一个SAVE命令，阻塞所有客户端，不再执行客户端发送的任何命令，并在SAVE命令执行完毕之后关闭服务器

5. **一个Redis服务器连接到另一个Redis服务器**
   当一个Redis服务器连接到另一个Redis服务器，并向对方发送SYNC命令来开始一次复制操作的时候，如果主服务器目前没有执行BGSAVE操作，或者主服务器并非刚刚执行完BGSAVE操作，那么主服务器就会执行BGSAVE命令



#### 2.AOF持久化

解释：将写命令添加到 AOF 文件（Append Only File）的末尾

优点：实时性更好

缺点：同步频繁会降低redis速度，AOF会随着服务器请求增多体积变大（解决：AOF重写特性，去除冗余写命令）

适用：对数据完整性要求高

redis.conf配置文件中配置：

```properties
# 开启AOF同步
appendonly yes
# dir 文件位置
# 默认文件名 appendonly.aof

```

同步选项：从而确定写命令同步到磁盘文件上的时机。这是因为对文件进行写入并不会马上将内容同步到磁盘上，而是先存储到缓冲区，然后由操作系统决定什么时候同步到磁盘

![1609773148283](..\resource\1609773148283.png)

1. **appendfsync always**

   可以实现将数据丢失减到最少，但是影响redis速度；减少固定硬盘寿命

2. **appendfsync everysec**

   兼顾数据和写入性能，每秒同步一次AOF，redis性能几乎不受影响；当硬盘忙于执行写入操作的时候，Redis还会优雅的放慢自己的速度以便适应硬盘的最大写入速度？？？

3. **appendfsync no**

   会使Redis丢失不定量的数据而且如果用户的硬盘处理写入操作的速度不够的话，那么当缓冲区被等待写入的数据填满时，Redis的写入操作将被阻塞，这会导致Redis的请求速度变慢

#### 3. 重写/压缩AOF

AOF问题：虽然AOF方式从某种角度上来说能将数据丢失降低到最小且对性能影响也很小，但极端情况下AOF不断膨胀会用完硬盘空间，且AOF过大还原操作会很耗时

**BGREWRITEAOF命令** ：通过移除AOF文件中冗余命令来重写（rewrite）AOF文件

> BGREWRITEAOF命令和BGSAVE创建快照原理十分相似，通过创建子进程进行AOF重写
>
> 可能会导致性能问题和内存问题，不加控制的话AOF体积可能会比快照文件大好几倍

**文件重写流程**：？

![BGREWRITEAOF](..\resource\BGREWRITEAOF.png)

重写设置：

```properties
# 那么当AOF文件体积大于64mb，并且AOF的体积比上一次重写之后的体积大了至少一倍（100%）的时候，Redis将执行BGREWRITEAOF命令
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb
```

#### 4. Redis **4**.0对持久化机制优化

Redis 4.0 开始支持 RDB 和 AOF 的混合持久化（默认关闭，可以通过配置项 `aof-use-rdb-preamble` 开启）

如果把混合持久化打开，AOF 重写的时候就直接把 RDB 的内容写到 AOF 文件开头。这样做的好处是可以结合 RDB 和 AOF 的优点, 快速加载同时避免丢失过多的数据。当然缺点也是有的， AOF 里面的 RDB 部分就是压缩格式不再是 AOF 格式，可读性较差

#### 5. 如何选择合适的持久化方式

- 一般来说， 如果想达到足以媲美PostgreSQL的数据安全性，你应该同时使用两种持久化功能。在这种情况下，当 Redis 重启的时候会优先载入AOF文件来恢复原始的数据，因为在通常情况下AOF文件保存的数据集要比RDB文件保存的数据集要完整
- 如果你非常关心你的数据， 但仍然可以承受数分钟以内的数据丢失，那么你可以只使用RDB持久化
- 有很多用户都只使用AOF持久化，但并不推荐这种方式，因为定时生成RDB快照（snapshot）非常便于进行数据库备份， 并且 RDB 恢复数据集的速度也要比AOF恢复的速度要快，除此之外，使用RDB还可以避免AOF程序的bug
- 如果你只希望你的数据在服务器运行的时候存在，你也可以不使用任何持久化方式
- 除了进行持久化外，用户还必须对持久化得到的文件进行备份（最好是备份到不同的地方），这样才能尽量避免数据丢失事故发生。如果条件允许的话，最好能将快照文件和重新重写的AOF文件备份到不同的服务器上面



### 四、Redis过期键删除策略

set key 的时候，都可以给一个 expire time

项目中的 token 或者一些登录信息，尤其是短信验证码都是有时间限制的

> **注**：对于散列表这种容器，只能为整个键设置过期时间（整个散列表），而不能为键里面的单个元素设置过期时间

**删除策略**：

1. 立即删除

   设置键的过期时间时，创建一个回调事件，当过期时间达到时，由时间处理器自动执行键的删除操作

   **优点：**保证过期键值会在过期后马上被删除，其所占用的内存也会随之释放

   **缺点：**对CPU不友好，目前redis事件处理器对时间事件的处理方式–无序链表，查找一个key的时间复杂度为O(n),所以并不适合用来处理大量的时间事件

2. 惰性删除

   键过期了就过期了，不主动删除。每次从dict字典中按key取值时，先检查此key是否已经过期，如果过期了就删除它，并返回nil，如果没过期，就返回键值

   **优点：**保证过期键值会在过期后马上被删除，其所占用的内存也会随之释放

   **缺点：**浪费内存，dict字典和expires字典都要保存这个键值的信息

3. 定期删除

   每隔一段时间，对expires字典进行检查，删除里面的过期键

   **优点：**通过限制删除操作执行的时长和频率，来减少删除操作对cpu的影响。另一方面定时删除也有效的减少了因惰性删除带来的内存浪费

   

   **Redis使用的策略**：惰性删除+定期删除



   ### 五、Redis数据淘汰策略

   

可以设置内存最大使用量，当内存使用量超出时，会施行数据淘汰策略

作为内存数据库，出于对性能和内存消耗的考虑，Redis 的淘汰算法实际实现上并非针对所有 key，而是抽样一小部分并且从中选出被淘汰的 key

在运行过程中也可以通过命令动态设置淘汰策略，并通过 INFO 命令监控缓存的 miss 和 hit，来进行调优。

![1609780434152](..\resource\redis淘汰策略.png)

**淘汰策略的内部实现**

- 客户端执行一个命令，导致 Redis 中的数据增加，占用更多内存
- Redis 检查内存使用量，如果超出 maxmemory 限制，根据策略清除部分 key
- 继续执行下一条命令，以此类推

在这个过程中，内存使用量会不断地达到 limit 值，然后超过，然后删除部分 key，使用量又下降到 limit 值之下。

如果某个命令导致大量内存占用(比如通过新key保存一个很大的set)，在一段时间内，可能内存的使用量会明显超过 maxmemory 限制





### 六、Redis中缓存雪崩、穿透等问题解决方案

#### 1. 缓存雪崩

定义：缓存同一时间大面积的失效，所以，后面的请求都会落到数据库上，造成数据库短时间内承受大量请求而崩掉

解决方案：

1. 缓存数据的过期时间设置随机，防止同一时间大量数据过期现象发生
2. 一般并发量不是特别多的时候，使用最多的解决方案是加锁排队
3. 给每一个缓存数据增加相应的缓存标记，记录缓存的是否失效，如果缓存标记失效，则更新数据缓存

#### 2. 缓存穿透

定义：缓存和数据库中都没有的数据，导致所有的请求都落到数据库上，造成数据库短时间内承受大量请求而崩掉

解决方案：

1. 接口层增加校验，比如用户鉴权校验、加密校验等等

2. 从缓存取不到数据，同时在数据库也未取到，可将key-value写成key-null，并设短点的缓存有效时间。防止攻击用户反复使用一个id暴力攻击

3. 采用布隆过滤器，将所有可能存在的数据哈希到一个足够大的bitmap中，一个一定不存在的数据会被这个bitmap拦截掉

   > bitmap：典型的就是哈希表
   >
   > ​	缺点：Bitmap对于每个元素只能记录1bit信息，如果还想完成额外的功能，恐怕只能靠牺牲更多的空间、时间来完成
   >
   > 
   >
   > 布隆过滤器（推荐）Bloom-Filter
   >
   > 引入了k(k>1)k(k>1)个相互独立的哈希函数，保证在给定的空间、误判率下，完成元素判重的过程
   >
   > 优点：空间效率和查询时间都远远超过一般的算法
   >
   > 缺点：有一定的误识别率和删除困难
   >
   > 
   >
   > Bloom-Filter算法：利用多个不同的Hash函数来解决“冲突”
   >
   > Hash存在一个冲突（碰撞）的问题，用同一个Hash得到的两个URL的值有可能相同。为了减少冲突，我们可以多引入几个Hash，如果通过其中的一个Hash值我们得出某元素不在集合中，那么该元素肯定不在集合中。只有在所有的Hash函数告诉我们该元素在集合中时，才能确定该元素存在于集合中
   >
   > Bloom-Filter一般用于在大数据量的集合中判定某元素是否存在。

#### 3.缓存击穿

定义：缓存中没有但数据库中有的数据（一般是缓存时间到期），这时由于并发用户特别多，同时读缓存没读到数据，又同时去数据库去取数据，引起数据库压力瞬间增大，造成过大压力。和缓存雪崩不同的是，缓存击穿指并发查同一条数据，缓存雪崩是不同数据都过期了，很多数据都查不到从而查数据库。

解决方案：

1. 设置热点数据永不过期
2. 加**互斥锁**

#### 4.缓存预热

定义：系统上线后，将相关的缓存数据直接加载到缓存系统。这样就可以避免在用户请求的时候，先查询数据库，然后再将数据缓存的问题！用户直接查询事先被预热的缓存数据

解决方案：

1. 写个缓存刷新页面，上线时手动操作
2. 数据量不大，项目启动时自动加载
3. 定时刷新缓存

#### 5.缓存降级

定义：当访问量剧增、服务出现问题（如响应时间慢或不响应）或非核心服务影响到核心流程的性能时，仍然需要保证服务还是可用的，即使是有损服务。系统可以根据一些关键数据进行自动降级，也可以配置开关实现人工降级。**最终目的**是保证**核心服务可用，即使是有损的**

降级前需要对系统进行梳理，哪些可以降级，哪些不能降级，可参考日志级别方案：

> 1. 一般：比如有些服务偶尔因为网络抖动或者服务正在上线而超时，可以自动降级；
> 2. 警告：有些服务在一段时间内成功率有波动（如在95~100%之间），可以自动降级或人工降级，并发送告警；
> 3. 错误：比如可用率低于90%，或者数据库连接池被打爆了，或者访问量突然猛增到系统能承受的最大阀值，此时可以根据情况自动降级或者人工降级；
> 4. 严重错误：比如因为特殊原因数据错误了，此时需要紧急人工降级。

目的：防止Redis服务故障，导致数据库跟着一起发生雪崩问题。

降级方式：Redis出现问题，不去数据库查询，而是直接返回默认值给用户







### Redis客户端对比

#### 客户端通信协议RESP

Redis制定了RESP（Redis Serialization Protocol，Redis序列化协议）实现客户端与服务端的正常交互。

**1. RESP 发送命令格式**

在`RESP`中，发送的数据类型取决于数据报的第一个字节：

- 单行字符串的第一个字节为`+`。
- 错误消息的第一个字节为`-`。
- 整型数字的第一个字节为`:`。
- 定长字符串的第一个字节为`$`。
- `RESP`数组的第一个字节为`*`。

| 数据类型        | 本文翻译名称 | 基本特征                                                     | 例子                           |
| :-------------- | :----------- | :----------------------------------------------------------- | :----------------------------- |
| `Simple String` | 单行字符串   | 第一个字节是`+`，最后两个字节是`\r\n`，其他字节是字符串内容  | `+OK\r\n`                      |
| `Error`         | 错误消息     | 第一个字节是`-`，最后两个字节是`\r\n`，其他字节是异常消息的文本内容 | `-ERR\r\n`                     |
| `Integer`       | 整型数字     | 第一个字节是`:`，最后两个字节是`\r\n`，其他字节是数字的文本内容 | `:100\r\n`                     |
| `Bulk String`   | 定长字符串   | 第一个字节是`$`，紧接着的字节是`内容字符串长度\r\n`，最后两个字节是`\r\n`，其他字节是字符串内容 | `$4\r\ndoge\r\n`               |
| `Array`         | `RESP`数组   | 第一个字节是`*`，紧接着的字节是`元素个数\r\n`，最后两个字节是`\r\n`，其他字节是各个元素的内容，每个元素可以是任意一种数据类型 | `*2\r\n:100\r\n$4\r\ndoge\r\n` |

发送的命令格式如下，CRLF代表"\r\n":

```shell
*<参数数量> CRLF
$<参数1的字节数量> CRLF
<参数1> CRLF
...
$<参数N的字节数量> CRLF
<参数N> CRLF

set hello world

*3
$3
SET
$5
hello
$5
world
# 最终结果
*3\r\n$3\r\nSET\r\n$5\r\nhello\r\n$5\r\nworld\r\n
```



**2. RESP 响应内容**

Redis的返回结果类型分为以下五种：
正确回复：在RESP中第一个字节为"+"
错误回复：在RESP中第一个字节为"-"
整数回复：在RESP中第一个字节为":"
字符串回复：在RESP中第一个字节为"$"

多条字符串回复：在RESP中第一个字节为"\*"
(+) 表示一个正确的状态信息，具体信息是当前行+后面的字符。
(-)  表示一个错误信息，具体信息是当前行－后面的字符。
(\*) 表示消息体总共有多少行，不包括当前行,\*后面是具体的行数。
(\$) 表示下一行数据长度，不包括换行符长度\r\n,$后面则是对应的长度的数据。
(:) 表示返回一个数值，：后面是相应的数字节符。

#### 1. Jedis

Jedis 是老牌的 Redis 的 Java 实现客户端，提供了比较全面的 Redis 命令的支持

优点：支持全面的Redis操作特性（API全面）

缺点：

- 使用阻塞IO，调用方法都是同步的，程序流需要等到sockets处理完IO后才能执行，不支持异步

- Jedis客户端不是线程安全的，需要通过连接池来使用Jedis

#### 2.lettuce

可扩展的线程安全的 Redis 客户端，支持异步模式。如果避免阻塞和事务操作，如BLPOP和MULTI/EXEC，多个线程就可以共享一个连接。lettuce 底层基于 Netty，支持高级的 Redis 特性，比如哨兵，集群，管道，自动重新连接和Redis数据模型。

优点：

- 支持同步异步通信模式；
- Lettuce 的 API 是线程安全的，如果不是执行阻塞和事务操作，如BLPOP和MULTI/EXEC，多个线程就可以共享一个连接

#### 3.Redission

Redisson 是一个在 Redis 的基础上实现的 Java 驻内存数据网格（In-Memory Data Grid）。它不仅提供了一系列的分布式的 Java 常用对象，还提供了许多分布式服务。其中包括( BitSet, Set, Multimap, SortedSet, Map, List, Queue, BlockingQueue, Deque, BlockingDeque, Semaphore, Lock, AtomicLong, CountDownLatch, Publish / Subscribe, Bloom filter, Remote service, Spring cache, Executor service, Live Object service, Scheduler service)

Redisson 提供了使用Redis 的最简单和最便捷的方法。Redisson 的宗旨是促进使用者对Redis的关注分离（Separation of Concern），从而让使用者能够将精力更集中地放在处理业务逻辑上

优点：

- 使用者对 Redis 的关注分离，可以类比 Spring 框架，这些框架搭建了应用程序的基础框架和功能，提升开发效率，让开发者有更多的时间来关注业务逻辑；
- 提供很多分布式相关操作服务，例如，分布式锁，分布式集合，可通过Redis支持延迟队列等。

缺点：

- Redisson 对字符串的操作支持比较差。



**结论**：使用lettuce + Redisson

Jedis 和 lettuce 是比较纯粹的 Redis 客户端，几乎没提供什么高级功能。Jedis 的性能比较差，所以如果你不需要使用 Redis 的高级功能的话，优先推荐使用 lettuce。

Redisson 的优势是提供了很多开箱即用的 Redis 高级功能，如果你的应用中需要使用到 Redis 的高级功能，建议使用















































### 附录一：redis和Memcached不同

![1609777243430](..\resource\redis_vs_memcached.png)

### 附录二：redis.conf配置文件解析

```properties
# Redis配置文件样例

# Note on units: when memory size is needed, it is possible to specifiy
# it in the usual form of 1k 5GB 4M and so forth:
#
# 1k => 1000 bytes
# 1kb => 1024 bytes
# 1m => 1000000 bytes
# 1mb => 1024*1024 bytes
# 1g => 1000000000 bytes
# 1gb => 1024*1024*1024 bytes
#
# units are case insensitive so 1GB 1Gb 1gB are all the same.

# Redis默认不是以守护进程的方式运行，可以通过该配置项修改，使用yes启用守护进程
# 启用守护进程后，Redis会把pid写到一个pidfile中，在/var/run/redis.pid
daemonize no

# 当Redis以守护进程方式运行时，Redis默认会把pid写入/var/run/redis.pid文件，可以通过pidfile指定
pidfile /var/run/redis.pid

# 指定Redis监听端口，默认端口为6379
# 如果指定0端口，表示Redis不监听TCP连接
port 6379

# 绑定的主机地址
# 你可以绑定单一接口，如果没有绑定，所有接口都会监听到来的连接
# bind 127.0.0.1

# Specify the path for the unix socket that will be used to listen for
# incoming connections. There is no default, so Redis will not listen
# on a unix socket when not specified.
#
# unixsocket /tmp/redis.sock
# unixsocketperm 755

# 当客户端闲置多长时间后关闭连接，如果指定为0，表示关闭该功能
timeout 0

# 指定日志记录级别，Redis总共支持四个级别：debug、verbose、notice、warning，默认为verbose
# debug (很多信息, 对开发／测试比较有用)
# verbose (many rarely useful info, but not a mess like the debug level)
# notice (moderately verbose, what you want in production probably)
# warning (only very important / critical messages are logged)
loglevel verbose

# 日志记录方式，默认为标准输出，如果配置为redis为守护进程方式运行，而这里又配置为标准输出，则日志将会发送给/dev/null
logfile stdout

# To enable logging to the system logger, just set 'syslog-enabled' to yes,
# and optionally update the other syslog parameters to suit your needs.
# syslog-enabled no

# Specify the syslog identity.
# syslog-ident redis

# Specify the syslog facility.  Must be USER or between LOCAL0-LOCAL7.
# syslog-facility local0

# 设置数据库的数量，默认数据库为0，可以使用select <dbid>命令在连接上指定数据库id
# dbid是从0到‘databases’-1的数目
databases 16

################################ SNAPSHOTTING  #################################
# 指定在多长时间内，有多少次更新操作，就将数据同步到数据文件，可以多个条件配合
# Save the DB on disk:
#
#   save <seconds> <changes>
#
#   Will save the DB if both the given number of seconds and the given
#   number of write operations against the DB occurred.
#
#   满足以下条件将会同步数据:
#   900秒（15分钟）内有1个更改
#   300秒（5分钟）内有10个更改
#   60秒内有10000个更改
#   Note: 可以把所有“save”行注释掉，这样就取消同步操作了

save 900 1
save 300 10
save 60 10000

# 指定存储至本地数据库时是否压缩数据，默认为yes，Redis采用LZF压缩，如果为了节省CPU时间，可以关闭该选项，但会导致数据库文件变的巨大
rdbcompression yes

# 指定本地数据库文件名，默认值为dump.rdb
dbfilename dump.rdb

# 工作目录.
# 指定本地数据库存放目录，文件名由上一个dbfilename配置项指定
# 
# Also the Append Only File will be created inside this directory.
# 
# 注意，这里只能指定一个目录，不能指定文件名
dir ./

################################# REPLICATION #################################

# 主从复制。使用slaveof从 Redis服务器复制一个Redis实例。注意，该配置仅限于当前slave有效
# so for example it is possible to configure the slave to save the DB with a
# different interval, or to listen to another port, and so on.
# 设置当本机为slav服务时，设置master服务的ip地址及端口，在Redis启动时，它会自动从master进行数据同步
# slaveof <masterip> <masterport>


# 当master服务设置了密码保护时，slav服务连接master的密码
# 下文的“requirepass”配置项可以指定密码
# masterauth <master-password>

# When a slave lost the connection with the master, or when the replication
# is still in progress, the slave can act in two different ways:
#
# 1) if slave-serve-stale-data is set to 'yes' (the default) the slave will
#    still reply to client requests, possibly with out of data data, or the
#    data set may just be empty if this is the first synchronization.
#
# 2) if slave-serve-stale data is set to 'no' the slave will reply with
#    an error "SYNC with master in progress" to all the kind of commands
#    but to INFO and SLAVEOF.
#
slave-serve-stale-data yes

# Slaves send PINGs to server in a predefined interval. It's possible to change
# this interval with the repl_ping_slave_period option. The default value is 10
# seconds.
#
# repl-ping-slave-period 10

# The following option sets a timeout for both Bulk transfer I/O timeout and
# master data or ping response timeout. The default value is 60 seconds.
#
# It is important to make sure that this value is greater than the value
# specified for repl-ping-slave-period otherwise a timeout will be detected
# every time there is low traffic between the master and the slave.
#
# repl-timeout 60

################################## SECURITY ###################################

# Warning: since Redis is pretty fast an outside user can try up to
# 150k passwords per second against a good box. This means that you should
# use a very strong password otherwise it will be very easy to break.
# 设置Redis连接密码，如果配置了连接密码，客户端在连接Redis时需要通过auth <password>命令提供密码，默认关闭
# requirepass foobared

# Command renaming.
#
# It is possilbe to change the name of dangerous commands in a shared
# environment. For instance the CONFIG command may be renamed into something
# of hard to guess so that it will be still available for internal-use
# tools but not available for general clients.
#
# Example:
#
# rename-command CONFIG b840fc02d524045429941cc15f59e41cb7be6c52
#
# It is also possilbe to completely kill a command renaming it into
# an empty string:
#
# rename-command CONFIG ""

################################### LIMITS ####################################

# 设置同一时间最大客户端连接数，默认无限制，Redis可以同时打开的客户端连接数为Redis进程可以打开的最大文件描述符数，
# 如果设置maxclients 0，表示不作限制。当客户端连接数到达限制时，Redis会关闭新的连接并向客户端返回max Number of clients reached错误信息
# maxclients 128

# Don't use more memory than the specified amount of bytes.
# When the memory limit is reached Redis will try to remove keys with an
# EXPIRE set. It will try to start freeing keys that are going to expire
# in little time and preserve keys with a longer time to live.
# Redis will also try to remove objects from free lists if possible.
#
# If all this fails, Redis will start to reply with errors to commands
# that will use more memory, like SET, LPUSH, and so on, and will continue
# to reply to most read-only commands like GET.
#
# WARNING: maxmemory can be a good idea mainly if you want to use Redis as a
# 'state' server or cache, not as a real DB. When Redis is used as a real
# database the memory usage will grow over the weeks, it will be obvious if
# it is going to use too much memory in the long run, and you'll have the time
# to upgrade. With maxmemory after the limit is reached you'll start to get
# errors for write operations, and this may even lead to DB inconsistency.
# 指定Redis最大内存限制，Redis在启动时会把数据加载到内存中，达到最大内存后，Redis会先尝试清除已到期或即将到期的Key，
# 当此方法处理后，仍然到达最大内存设置，将无法再进行写入操作，但仍然可以进行读取操作。
# Redis新的vm机制，会把Key存放内存，Value会存放在swap区
# maxmemory <bytes>

# MAXMEMORY POLICY: how Redis will select what to remove when maxmemory
# is reached? You can select among five behavior:
# 
# volatile-lru -> remove the key with an expire set using an LRU algorithm
# allkeys-lru -> remove any key accordingly to the LRU algorithm
# volatile-random -> remove a random key with an expire set
# allkeys->random -> remove a random key, any key
# volatile-ttl -> remove the key with the nearest expire time (minor TTL)
# noeviction -> don't expire at all, just return an error on write operations
# 
# Note: with all the kind of policies, Redis will return an error on write
#       operations, when there are not suitable keys for eviction.
#
#       At the date of writing this commands are: set setnx setex append
#       incr decr rpush lpush rpushx lpushx linsert lset rpoplpush sadd
#       sinter sinterstore sunion sunionstore sdiff sdiffstore zadd zincrby
#       zunionstore zinterstore hset hsetnx hmset hincrby incrby decrby
#       getset mset msetnx exec sort
#
# The default is:
#
# maxmemory-policy volatile-lru

# LRU and minimal TTL algorithms are not precise algorithms but approximated
# algorithms (in order to save memory), so you can select as well the sample
# size to check. For instance for default Redis will check three keys and
# pick the one that was used less recently, you can change the sample size
# using the following configuration directive.
#
# maxmemory-samples 3

############################## APPEND ONLY MODE ###############################

# 
# Note that you can have both the async dumps and the append only file if you
# like (you have to comment the "save" statements above to disable the dumps).
# Still if append only mode is enabled Redis will load the data from the
# log file at startup ignoring the dump.rdb file.
# 指定是否在每次更新操作后进行日志记录，Redis在默认情况下是异步的把数据写入磁盘，如果不开启，可能会在断电时导致一段时间内的数据丢失。
# 因为redis本身同步数据文件是按上面save条件来同步的，所以有的数据会在一段时间内只存在于内存中。默认为no
# IMPORTANT: Check the BGREWRITEAOF to check how to rewrite the append
# log file in background when it gets too big.

appendonly no

# 指定更新日志文件名，默认为appendonly.aof
# appendfilename appendonly.aof

# The fsync() call tells the Operating System to actually write data on disk
# instead to wait for more data in the output buffer. Some OS will really flush 
# data on disk, some other OS will just try to do it ASAP.

# 指定更新日志条件，共有3个可选值：
# no:表示等操作系统进行数据缓存同步到磁盘（快）
# always:表示每次更新操作后手动调用fsync()将数据写到磁盘（慢，安全）
# everysec:表示每秒同步一次（折衷，默认值）

appendfsync everysec
# appendfsync no

# When the AOF fsync policy is set to always or everysec, and a background
# saving process (a background save or AOF log background rewriting) is
# performing a lot of I/O against the disk, in some Linux configurations
# Redis may block too long on the fsync() call. Note that there is no fix for
# this currently, as even performing fsync in a different thread will block
# our synchronous write(2) call.
#
# In order to mitigate this problem it's possible to use the following option
# that will prevent fsync() from being called in the main process while a
# BGSAVE or BGREWRITEAOF is in progress.
#
# This means that while another child is saving the durability of Redis is
# the same as "appendfsync none", that in pratical terms means that it is
# possible to lost up to 30 seconds of log in the worst scenario (with the
# default Linux settings).
# 
# If you have latency problems turn this to "yes". Otherwise leave it as
# "no" that is the safest pick from the point of view of durability.
no-appendfsync-on-rewrite no

# Automatic rewrite of the append only file.
# Redis is able to automatically rewrite the log file implicitly calling
# BGREWRITEAOF when the AOF log size will growth by the specified percentage.
# 
# This is how it works: Redis remembers the size of the AOF file after the
# latest rewrite (or if no rewrite happened since the restart, the size of
# the AOF at startup is used).
#
# This base size is compared to the current size. If the current size is
# bigger than the specified percentage, the rewrite is triggered. Also
# you need to specify a minimal size for the AOF file to be rewritten, this
# is useful to avoid rewriting the AOF file even if the percentage increase
# is reached but it is still pretty small.
#
# Specify a precentage of zero in order to disable the automatic AOF
# rewrite feature.

auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb

################################## SLOW LOG ###################################

# The Redis Slow Log is a system to log queries that exceeded a specified
# execution time. The execution time does not include the I/O operations
# like talking with the client, sending the reply and so forth,
# but just the time needed to actually execute the command (this is the only
# stage of command execution where the thread is blocked and can not serve
# other requests in the meantime).
# 
# You can configure the slow log with two parameters: one tells Redis
# what is the execution time, in microseconds, to exceed in order for the
# command to get logged, and the other parameter is the length of the
# slow log. When a new command is logged the oldest one is removed from the
# queue of logged commands.

# The following time is expressed in microseconds, so 1000000 is equivalent
# to one second. Note that a negative number disables the slow log, while
# a value of zero forces the logging of every command.
slowlog-log-slower-than 10000

# There is no limit to this length. Just be aware that it will consume memory.
# You can reclaim memory used by the slow log with SLOWLOG RESET.
slowlog-max-len 1024

################################ VIRTUAL MEMORY ###############################

### WARNING! Virtual Memory is deprecated in Redis 2.4
### The use of Virtual Memory is strongly discouraged.

### WARNING! Virtual Memory is deprecated in Redis 2.4
### The use of Virtual Memory is strongly discouraged.

# Virtual Memory allows Redis to work with datasets bigger than the actual
# amount of RAM needed to hold the whole dataset in memory.
# In order to do so very used keys are taken in memory while the other keys
# are swapped into a swap file, similarly to what operating systems do
# with memory pages.
# 指定是否启用虚拟内存机制，默认值为no，
# VM机制将数据分页存放，由Redis将访问量较少的页即冷数据swap到磁盘上，访问多的页面由磁盘自动换出到内存中
# 把vm-enabled设置为yes，根据需要设置好接下来的三个VM参数，就可以启动VM了
vm-enabled no
# vm-enabled yes

# This is the path of the Redis swap file. As you can guess, swap files
# can't be shared by different Redis instances, so make sure to use a swap
# file for every redis process you are running. Redis will complain if the
# swap file is already in use.
#
# Redis交换文件最好的存储是SSD（固态硬盘）
# 虚拟内存文件路径，默认值为/tmp/redis.swap，不可多个Redis实例共享
# *** WARNING *** if you are using a shared hosting the default of putting
# the swap file under /tmp is not secure. Create a dir with access granted
# only to Redis user and configure Redis to create the swap file there.
vm-swap-file /tmp/redis.swap

# With vm-max-memory 0 the system will swap everything it can. Not a good
# default, just specify the max amount of RAM you can in bytes, but it's
# better to leave some margin. For instance specify an amount of RAM
# that's more or less between 60 and 80% of your free RAM.
# 将所有大于vm-max-memory的数据存入虚拟内存，无论vm-max-memory设置多少，所有索引数据都是内存存储的（Redis的索引数据就是keys）
# 也就是说当vm-max-memory设置为0的时候，其实是所有value都存在于磁盘。默认值为0
vm-max-memory 0

# Redis swap文件分成了很多的page，一个对象可以保存在多个page上面，但一个page上不能被多个对象共享，vm-page-size是要根据存储的数据大小来设定的。
# 建议如果存储很多小对象，page大小最后设置为32或64bytes；如果存储很大的对象，则可以使用更大的page，如果不确定，就使用默认值
vm-page-size 32

# 设置swap文件中的page数量由于页表（一种表示页面空闲或使用的bitmap）是存放在内存中的，在磁盘上每8个pages将消耗1byte的内存
# swap空间总容量为 vm-page-size * vm-pages
#
# With the default of 32-bytes memory pages and 134217728 pages Redis will
# use a 4 GB swap file, that will use 16 MB of RAM for the page table.
#
# It's better to use the smallest acceptable value for your application,
# but the default is large in order to work in most conditions.
vm-pages 134217728

# Max number of VM I/O threads running at the same time.
# This threads are used to read/write data from/to swap file, since they
# also encode and decode objects from disk to memory or the reverse, a bigger
# number of threads can help with big objects even if they can't help with
# I/O itself as the physical device may not be able to couple with many
# reads/writes operations at the same time.
# 设置访问swap文件的I/O线程数，最后不要超过机器的核数，如果设置为0，那么所有对swap文件的操作都是串行的，可能会造成比较长时间的延迟，默认值为4
vm-max-threads 4

############################### ADVANCED CONFIG ###############################

# Hashes are encoded in a special way (much more memory efficient) when they
# have at max a given numer of elements, and the biggest element does not
# exceed a given threshold. You can configure this limits with the following
# configuration directives.
# 指定在超过一定的数量或者最大的元素超过某一临界值时，采用一种特殊的哈希算法
hash-max-zipmap-entries 512
hash-max-zipmap-value 64

# Similarly to hashes, small lists are also encoded in a special way in order
# to save a lot of space. The special representation is only used when
# you are under the following limits:
list-max-ziplist-entries 512
list-max-ziplist-value 64

# Sets have a special encoding in just one case: when a set is composed
# of just strings that happens to be integers in radix 10 in the range
# of 64 bit signed integers.
# The following configuration setting sets the limit in the size of the
# set in order to use this special memory saving encoding.
set-max-intset-entries 512

# Similarly to hashes and lists, sorted sets are also specially encoded in
# order to save a lot of space. This encoding is only used when the length and
# elements of a sorted set are below the following limits:
zset-max-ziplist-entries 128
zset-max-ziplist-value 64

# Active rehashing uses 1 millisecond every 100 milliseconds of CPU time in
# order to help rehashing the main Redis hash table (the one mapping top-level
# keys to values). The hash table implementation redis uses (see dict.c)
# performs a lazy rehashing: the more operation you run into an hash table
# that is rhashing, the more rehashing "steps" are performed, so if the
# server is idle the rehashing is never complete and some more memory is used
# by the hash table.
# 
# The default is to use this millisecond 10 times every second in order to
# active rehashing the main dictionaries, freeing memory when possible.
#
# If unsure:
# use "activerehashing no" if you have hard latency requirements and it is
# not a good thing in your environment that Redis can reply form time to time
# to queries with 2 milliseconds delay.
# 指定是否激活重置哈希，默认为开启
activerehashing yes

################################## INCLUDES ###################################

# 指定包含其他的配置文件，可以在同一主机上多个Redis实例之间使用同一份配置文件，而同时各实例又拥有自己的特定配置文件
# include /path/to/local.conf
# include /path/to/other.conf
```

