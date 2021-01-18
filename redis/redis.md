### 一、redis数据类型

​	Redis主要有5种数据类型，包括String、List、Set、Zset、Hash

![1609759109899](..\resource\1609759109899.png)

常用命令：http://redisdoc.com/index.html

##### 1. String

```shell
SET key value 			//存入字符串键值对
MSET key value [key value ...] 	//批量存储字符串键值对
SETNX key value 		//存入一个不存在的字符串键值对
GET key 			//获取一个字符串键值
MGET key [key ...]	 	//批量获取字符串键值
DEL key [key ...] 		//删除一个键
EXPIRE key seconds 		//设置一个键的过期时间(秒)

原子加减
INCR  key 			//将key中储存的数字值加1
DECR  key 			//将key中储存的数字值减1
INCRBY  key  increment 		//将key所储存的值加上increment
DECRBY  key  decrement 	//将key所储存的值减去decrement
```

**用途**：

1. 简单分布式锁：
           setnx product:1001 true 				// 返回1代表获取锁成功，0代表失败
           set product:1001 true ex 10 nx 	 // 防止程序意外导致死锁
2. 计数器：
           incr article:readcount:id
           get article:readcount:id
3. web集群session共享：spring session + redis
4. 分布式系统全局序列号
       	incrby orderId 1000 //批量生成，放入本地内存，提高性能



##### 2. List

```shell
LPUSH  key  value [value ...] 		//将一个或多个值value插入到key列表的表头(最左边)
RPUSH  key  value [value ...]	 	//将一个或多个值value插入到key列表的表尾(最右边)
LPOP  key			//移除并返回key列表的头元素
RPOP  key			//移除并返回key列表的尾元素
LRANGE  key  start  stop		//返回列表key中指定区间内的元素，区间以偏移量start和stop指定

blpop key [key...] timeout  // 从key列表弹出元素，若没有阻塞等待timeout秒，=0为一直等待
brpop key [key...] timeout 
```

用途：

> 常用数据结构(不多)：
>
> ​		stack = lpush + lpop 
>
> ​		queue = lpush + rpop
>
> ​		blocking mq阻塞队列 = lpush + brpop 
>
> 微博消息和微信公众号消息列表：
>
> ​		lpush msg:{userId} titleId  // 往粉丝队列里存消息
>
> ​		lrange msg:{userId} 0 5  // 查看最新文章

##### 3. Set

```shell
SADD  key  member  [member ...]//往集合key中存入元素，元素存在则忽略，若key不存在则新建
SREM  key  member  [member ...]//从集合key中删除元素
SMEMBERS  key				  //获取集合key中所有元素
SCARD  key					 //获取集合key的元素个数
SISMEMBER  key  member		  //判断member元素是否存在于集合key中
SRANDMEMBER  key  [count]   	//从集合key中选出count个元素，元素不从key中删除
SPOP  key  [count]			//从集合key中选出count个元素，元素从key中删除

Set运算操作
SINTER  key  [key ...] 				//交集运算
SINTERSTORE  destination  key  [key ..]		//将交集结果存入新集合destination中
SUNION  key  [key ..] 				//并集运算
SUNIONSTORE  destination  key  [key ...]		//将并集结果存入新集合destination中
SDIFF  key  [key ...] 				//差集运算
SDIFFSTORE  destination  key  [key ...]		//将差集结果存入新集合destination中


```

用途：

> 微信抽奖小程序：
>
> ​		sadd key {userId}  // 用户加入抽奖
>
> ​		smember key // 查看所有抽奖用户
>
> ​		srangmember key [count]   || spop key [count]  // 抽取count个中奖者
>
> 微信微博点赞、搜藏、标签：
>
> ​		 sadd like:{msgId} {userId}  // 点赞
>
> ​		 srem like:{msgId} {userId}  // 取消
>
> ​		sismember like:{msgId} {userId}  // 是否点过赞
>
>   		smembers like:{msgId} // 点赞用户列表
>
> ​		 scard like:{msgId} // 点赞数
>
> 集合操作：set1：abc  set2：bcd  set3：cde
>
> ​		sinter set1 set2 set3  // 交集   c
>
> ​		sunion set1 set2 set3 // 并集   abcde
>
> ​		sdiff set1 set2 set3 // 差集(第一个集合的元素减去后面集合的所有元素)  a
>
> 集合操作实现微博微信关注模型：
>
> ​		共同关注：sinter
>
> ​		我关注的人也关注他：sismember 
>
> ​		可能认识：sdiff
>
> 集合操作实现电商商品筛选：
>
> ​		sadd brand:huawei P40  // 上架商品时将相关筛选条件加入redis  
>
> ​		sinter os:android cpu:brand:intel  ram:8G -- {产品}  // 按条件取交集

##### 4. Hash

```shell
HSET  key  field  value 		//存储一个哈希表key的键值
HSETNX  key  field  value 		//存储一个不存在的哈希表key的键值
HMSET  key  field  value [field value ...] 	//在一个哈希表key中存储多个键值对
HGET  key  field 				//获取哈希表key对应的field键值
HMGET  key  field  [field ...] 		//批量获取哈希表key中多个field键值
HDEL  key  field  [field ...] 		//删除哈希表key中的field键值
HLEN  key				//返回哈希表key中field的数量
HGETALL  key				//返回哈希表key中所有的键值

HINCRBY  key  field  increment 		//为哈希表key中field键的值加上增量increment

优点：同类数据归类存储、比string消耗cpu小且节省空间
缺点：过期只能用在key上、集群架构下不适合大规模使用
```

**用途**：

1. 对象缓存
2. 电商购物车

![1610640257832](..\resource\redis_hash用途.png)

3. 集群分片

   ![1610809799547](..\resource\redishash集群分片.png)





##### 5. Zset

```shell
ZADD key score member [[score member]…]	//往有序集合key中加入带分值元素
ZREM key member [member …]		//从有序集合key中删除元素
ZSCORE key member 			//返回有序集合key中元素member的分值
ZINCRBY key increment member//为有序集合key中元素member的分值加上increment 
ZCARD key				  //返回有序集合key中元素个数
ZRANGE key start stop [WITHSCORES]	//正序获取有序集合key从start下标到stop下标的元素
ZREVRANGE key start stop [WITHSCORES]	//倒序获取有序集合key从start下标到stop下标的元素

Zset集合操作
ZUNIONSTORE destkey numkeys key [key ...] 	//并集计算
ZINTERSTORE destkey numkeys key [key …]	//交集计算
```

用途：

> 排行榜：
>
> ​		zincrby hotNews:20201010 1 电影  // 点击话题增加热度
>
> ​		zrevrange hotNews:20201010  0  9 withscores // 查看前十
>
> ​		zunionstore hotNews:20201010-20201017 7  hotNews:20201010 ... hotNews:20201017//7日搜索榜单计算
>
> ​		zrevrange hotNews:20201010-20201017 0 9 withscores  // 7日榜单前十

### 二、核心原理

##### 1. Redis是单线程吗？

> Redis 的单线程主要是指 Redis 的网络 IO 和键值对读写是由一个线程来完成的，这也是 Redis 对外提供键值存储服务的主要流程。
>
> 但 Redis 的其他功能，比如持久化、异步删除、集群数据同步等，其实是由额外的线程执行的。

##### 2. 为什么那么快

> 内存存储、单线程避免线程切换消耗
>
> 注意：因为是单线程，所以对于耗时指令要谨慎使用，避免redis卡顿

##### 3. 单线程如何处理高并发客户端连接

> 利用epoll实现IO的多路复用，将连接信息和事件放到队列中，依次放到文件事件分派器，事件分派器将事件分发给事件处理器。
>
> ![img](..\resource\clipboard.png)
>
> ```properties
> # 查看redis支持的最大连接数，在redis.conf文件中可修改，
> maxclients 10000
> 
> 127.0.0.1:6379> CONFIG GET maxclients
>     ##1) "maxclients"
>     ##2) "10000"
> ```



##### 4. 其他高级命令

**keys** ： 全量遍历，用来列出所有满足特定正则字符串规则的key，当数据量比较大时，性能比较差，要避免使用

**scan：渐进式遍历键**：

>  SCAN cursor [MATCH pattern] [COUNT count] 
>
> 第一个是 cursor 整数值(hash桶的索引值)，第二个是 key 的正则模式，第三个是一次遍历的key的数量(参考值，底层遍历的数量不一定)，并不是符合条件的结果数量。第一次遍历时，cursor 值为 0，然后将返回结果中第一个整数值作为下一次遍历的 cursor。一直遍历到返回的 cursor 值为 0 时结束。
>
> 注意：如果在scan的过程中如果有键的变化（增加、 删除、 修改） ，那么遍历效果可能会碰到如下问题： 新增的键可能没有遍历到， 遍历出了重复的键等情况， 也就是说scan并不能保证完整的遍历出来所有的键

**info**：查看redis服务运行信息

>  9 个块分别是: 
>
> Server 服务器运行的环境参数 
>
> Clients 客户端相关信息 
>
> Memory 服务器运行内存统计数据 
>
> Persistence 持久化信息 
>
> Stats 通用统计数据 
>
> Replication 主从复制相关信息 
>
> CPU CPU 使用情况 
>
> Cluster 集群信息 
>
> KeySpace 键值对统计数量信息



### 三、持久化问题

Redis是内存型数据库，为了防止系统故障/重用数据，需要将内存中数据持久化到硬盘上

默认开启：RDB

重启时，优先使用AOF文件还原数据

#### 1.RDB持久化（快照）

解释：将某个时间节点的内存数据库快照保存在名字为 dump.rdb 的二进制文件中

优点：可以将快照复制到其他服务器从而创建相同数据的服务器副本

缺点：如果系统发生故障，会丢失最后一次创建快照后的数据；如果数据量大，保存快照时间会很长

适用：即使丢失一部分数据也不会造成大问题

redis.conf配置文件中配置：关闭RDB只需要将所有的save保存策略注释掉即可

```properties
# dir 文件位置
# dbfilename 文件名
#在XXX秒之后，如果至少有yyy个key发生变化，Redis就会自动触发BGSAVE命令创建快照。
save 900 1              
save 300 10            
save 60 10000    
```

创建快照情况：

1. **BGSAVE命令**
   Redis会调用fork来创建一个子进程(**消耗内存**)，然后子进程负责将快照写入硬盘，而父进程则继续处理命令请求

2. **SAVE命令**

   接到SAVE命令的Redis服务器在快照创建完毕之前不会再响应任何其他命令。

   通常只会在没有足够内存去执行BGSAVE，又或者即使等待持久化操作执行完毕也不影响的情况下

3. **save选项**

   如果用户设置了save选项（一般会默认设置），redis会在save条件满足时自动调用BGSAVE命令

4. **SHUTDOWN命令**

   当Redis通过SHUTDOWN命令接收到关闭服务器的请求时，或者接收到标准TERM信号时，会执行一个SAVE命令，阻塞所有客户端，不再执行客户端发送的任何命令，并在SAVE命令执行完毕之后关闭服务器

5. **一个Redis服务器连接到另一个Redis服务器**
   当一个Redis服务器连接到另一个Redis服务器，并向对方发送SYNC命令来开始一次复制操作的时候，如果主服务器目前没有执行BGSAVE操作，或者主服务器并非刚刚执行完BGSAVE操作，那么主服务器就会执行BGSAVE命令



#### 2.AOF持久化

解释：将写命令添加到 AOF 文件（Append Only File）的末尾

优点：实时性更好

缺点：同步频繁会降低redis速度，AOF文件体积变大（解决：AOF重写特性，去除冗余写命令）

适用：对数据完整性要求高

```shell
//AOF文件： resp协议格式数据
// * 代表命令有多少个参数，$代表这个参数有几个字符 ,过期时间是时间戳
*3 
$3
set
$5
zhuge
$3
666
```

开启AOF：

```properties
# 开启AOF同步 默认文件名 appendonly.aof
appendonly yes
```

同步选项：

1. **appendfsync always**

   每次有新命令追加到 AOF 文件时就执行一次 fsync ，非常慢，也非常安全

2. **appendfsync everysec**（*默认*）

   每秒 fsync 一次，足够快，并且在故障时只会丢失 1 秒钟的数据

3. **appendfsync no**

   从不 fsync ,将数据交给操作系统来处理。更快，也更不安全的选择。

#### 3. 重写/压缩AOF

AOF问题：极端情况下AOF不断膨胀会用完硬盘空间，且AOF过大还原操作会很耗时

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

Redis 4.0 开始支持 RDB 和 AOF 混合持久化(**必须先开启aof**)

```properties
# 默认关闭 
aof-use-rdb-preamble yes 
```

如果把混合持久化打开，AOF 重写的时候就直接把 RDB 的内容写到 AOF 文件开头。

好处：可以结合 RDB 和 AOF 的优点, 快速加载同时避免丢失过多的数据。

缺点： AOF 里面的 RDB 部分就是压缩格式不再是 AOF 格式，可读性较差

**混合持久化重写流程：**

将重写**这一刻之前**的内存做RDB快照处理，并且将RDB快照内容和**增量的**AOF修改内存数据的命令存在一起，都写入新的AOF文件，之后覆盖原有的AOF文件，完成新旧两个AOF文件的替换。

Redis 重启的时候，可以先加载 RDB 的内容，然后再重放增量 AOF 日志就可以完全替代之前的 AOF 全量文件重放，因此重启效率大幅得到提升。

#### 5. 如何选择合适的持久化方式

- 一般来说， 如果想达到足以媲美PostgreSQL的数据安全性，你应该同时使用两种持久化功能。在这种情况下，当 Redis 重启的时候会优先载入AOF文件来恢复原始的数据，因为在通常情况下AOF文件保存的数据集要比RDB文件保存的数据集要完整
- 如果你非常关心你的数据， 但仍然可以承受数分钟以内的数据丢失，那么你可以只使用RDB持久化
- 有很多用户都只使用AOF持久化，但并不推荐这种方式，因为定时生成RDB快照（snapshot）非常便于进行数据库备份， 并且 RDB 恢复数据集的速度也要比AOF恢复的速度要快，除此之外，使用RDB还可以避免AOF程序的bug
- 如果你只希望你的数据在服务器运行的时候存在，你也可以不使用任何持久化方式
- 除了进行持久化外，用户还必须对持久化得到的文件进行备份（最好是备份到不同的地方），这样才能尽量避免数据丢失事故发生。如果条件允许的话，最好能将快照文件和重新重写的AOF文件备份到不同的服务器上面

#### 6. Redis数据推荐备份策略

> 1. crontab定时调度脚本，每小时copy一份备份到一个目录中，仅仅保留最近48小时的备份
> 2. 每天都保留一份当日的数据备份到一个目录中去，可以保留最近1个月的备份
> 3. 每次copy备份的时候，都把太旧的备份删除
> 4. 每天晚上将当前机器上的备份复制一份到其他机器上，以防机器损坏



### 四、过期键删除策略、数据淘汰策略

#### 1. 过期键删除

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

> 



#### 2. 内存满淘汰

可以设置内存最大使用量，当内存使用量超出时，会施行数据淘汰策略

作为内存数据库，出于对性能和内存消耗的考虑，Redis 的淘汰算法实际实现上并非针对所有 key，而是抽样一小部分并且从中选出被淘汰的 key

在运行过程中也可以通过命令动态设置淘汰策略，并通过 INFO 命令监控缓存的 miss 和 hit，来进行调优。

> **主动清理策略**在Redis 4.0 之前一共实现了 6 种内存淘汰策略，在 4.0 之后，又增加了 2 种策略，总共8种：
>
> **a) 针对设置了过期时间的key做处理：**
>
> 1. volatile-ttl：在筛选时，会针对设置了过期时间的键值对，根据过期时间的先后进行删除，越早过期的越先被删除。
> 2. volatile-random：就像它的名称一样，在设置了过期时间的键值对中，进行随机删除。
> 3. volatile-lru：会使用 LRU**（最近最少使用）** 算法筛选设置了过期时间的键值对删除。
> 4. volatile-lfu：会使用 LFU (**最不经常使用**)算法筛选设置了过期时间的键值对删除。
>
> **b) 针对所有的key做处理：**
>
> 1. allkeys-random：从所有键值对中随机选择并删除数据。
> 2. allkeys-lru：使用 LRU 算法在所有数据中进行筛选删除。
> 3. allkeys-lfu：使用 LFU 算法在所有数据中进行筛选删除。
>
> **c) 不处理：**
>
> 1. noeviction：不会剔除任何数据，拒绝所有写入操作并返回客户端错误信息"(error) OOM command not allowed when used memory"，此时Redis只响应读操作。



![1609780434152](..\resource\redis淘汰策略.png)

**淘汰策略的内部实现**

- 客户端执行一个命令，导致 Redis 中的数据增加，占用更多内存
- Redis 检查内存使用量，如果超出 maxmemory 限制，根据策略清除部分 key
- 继续执行下一条命令，以此类推

在这个过程中，内存使用量会不断地达到 limit 值，然后超过，然后删除部分 key，使用量又下降到 limit 值之下。

如果某个命令导致大量内存占用(比如通过新key保存一个很大的set)，在一段时间内，可能内存的使用量会明显超过 maxmemory 限制

### 四、主从、哨兵、集群架构

#### 1. Redis主从原理

如果你为master配置了一个slave，不管这个slave是否是第一次连接上Master，它都会发送一个**PSYNC**命令给master请求复制数据。

master收到PSYNC命令后，会在后台进行数据持久化通过bgsave生成最新的rdb快照文件，持久化期间，master会继续接收客户端的请求，它会把这些可能修改数据集的请求缓存在内存中。当持久化进行完毕以后，master会把这份rdb文件数据集发送给slave，slave会把接收到的数据进行持久化生成rdb，然后再加载到内存中。然后，master再将之前缓存在内存中的命令发送给slave。

当master与slave之间的连接由于某些原因而断开时，slave能够自动重连Master，如果master收到了多个slave并发连接请求，它只会进行一次持久化，而不是一个连接一次，然后再把这一份持久化的数据发送给多个并发连接的slave。

![img](..\resource\redis主从复制.png)

#### 2. 数据部分复制

当master和slave断开重连后，一般都会对整份数据进行复制。但从**redis2.8版本**开始，redis改用可以支持部分数据复制的命令PSYNC去master同步数据，slave与master能够在网络连接断开重连后只进行部分数据复制(**断点续传**)。

master会在其内存中创建一个复制数据用的缓存队列，缓存最近一段时间的数据，master和它所有的slave都维护了复制的数据下标offset和master的进程id，因此，当网络连接断开后，slave会请求master继续进行未完成的复制，从所记录的数据下标开始。如果master进程id变化了，或者从节点数据下标offset太旧，已经不在master的缓存队列里了，那么将会进行一次全量数据的复制。

![img](..\resource\redis部分复制.png)



注：如果有多个从节点，为了缓解**主从复制风暴**可以做如下架构

![img](..\resource\避免主从复制风暴.png)





#### 3. 哨兵原理

sentinel哨兵是特殊的redis服务，不提供读写服务，主要用来监控redis实例节点。

哨兵架构下client端第一次从哨兵找出redis的主节点，后续就直接访问redis的主节点，不会每次都通过sentinel代理访问redis的主节点，当redis的主节点发生变化，哨兵会第一时间感知到，并且将新的redis主节点通知给client端(这里面redis的client端一般都实现了订阅功能，订阅sentinel发布的节点变动消息)

缺点：存在主从切换的瞬间存在访问瞬断的情况，而且哨兵模式只有一个主节点对外提供服务，没法支持很高的并发，且单个主节点内存也不宜设置得过大，否则会导致持久化文件过大，影响数据恢复或主从同步的效率

**哨兵leader选举流程**

> 当一个master服务器被某sentinel视为下线状态后，该sentinel会与其他sentinel协商选出sentinel的leader进行故障转移工作。每个发现master服务器进入下线的sentinel都可以要求其他sentinel选自己为sentinel的leader，选举是先到先得。同时每个sentinel每次选举都会自增配置纪元(选举周期)，每个纪元中只会选择一个sentinel的leader。如果所有超过一半的sentinel选举某sentinel作为leader。之后该sentinel进行故障转移操作，从存活的slave中选举出新的master，这个选举过程跟集群的master选举很类似。
>
> 哨兵集群只有一个哨兵节点，redis的主从也能正常运行以及选举master，如果master挂了，那唯一的那个哨兵节点就是哨兵leader了，可以正常选举新master。
>
> 不过为了高可用一般都推荐至少部署三个哨兵节点。为什么推荐奇数个哨兵节点原理跟集群奇数个master节点类似。

![img](..\resource\redis哨兵架构.png)

#### 4. 高可用集群

一个由多个主从节点群组成的分布式服务器群，它具有**复制、高可用和分片**特性。Redis集群不需要sentinel哨兵·也能完成节点移除和故障转移的功能。需要将每个节点设置成集群模式，这种集群模式没有中心节点，可水平扩展，性能和高可用性均优于之前版本的哨兵模式，且集群配置非常简单 (**官方推荐不超过1000个节点**)

**原理分析：**

> Redis Cluster 将所有数据划分为 16384 个 slots(槽位)，每个节点负责其中一部分槽位。槽位的信息存储于每个节点中。
>
> 当 Redis Cluster 的客户端来连接集群时，它也会得到一份集群的槽位配置信息并将其缓存在客户端本地。这样当客户端要查找某个 key 时，可以直接定位到目标节点。同时因为槽位的信息可能会存在客户端与服务器不一致的情况，还需要纠正机制来实现槽位信息的校验调整。

**槽位定位算法**

> Cluster 默认会对 key 值使用 crc16 算法进行 hash 得到一个整数值，然后用这个整数值对 16384 进行取模来得到具体槽位。

```properties
HASH_SLOT = CRC16(key) mod 16384
```

**跳转重定位**：

> 当客户端向一个错误的节点发出了指令，该节点会发现指令的 key 所在的槽位并不归自己管理，它会向客户端发送一个特殊的跳转指令携带目标操作的节点地址，客户端收到指令后除了跳转到正确的节点上去操作，还会同步更新纠正本地的槽位映射表缓存，后续所有 key 将使用新的槽位映射表

![img](..\resource\redis集群重定向.png)

**Redis集群节点间的通信机制**

> redis cluster节点间采取gossip协议进行通信 
>
> 维护集群的元数据(集群节点信息，主从角色，节点数量，各节点共享的数据等)有两种方式：
>
> ​	集中式、gossip 
>
> - **集中式**
>
>   优点：元数据的更新和读取，时效性非常好，一旦元数据出现变更立即就会更新到集中式的存储中，其他节点读取的时候立即就可以立即感知到；
>
>   缺点：所有的元数据的更新压力全部集中在一个地方，可能导致元数据的存储压力。 很多中间件都会借助zookeeper集中式存储元数据。
>
> - **gossip**
>
>   ![img](..\resource\redis-gossip.gif)
>
>   gossip协议包含多种消息，包括ping，pong，meet，fail等等
>
>   meet:某个节点发送meet给新加入的节点，让新节点加入集群中，然后新节点就会开始与其他节点进行通信
>
>   ping：每个节点都会频繁给其他节点发送ping，其中包含自己的状态还有自己维护的集群元数据，互相通过ping交换元数据
>
>   pong: 对ping和meet消息的返回，包含自己的状态和其他信息，也可以用于信息广播和更新
>
>   fail: 某个节点判断另一个节点fail之后，就发送fail给其他节点，通知其他节点，指定的节点宕机了
>
>   **gossip通信的10000端口** :gossip协议端口=服务端口+10000
>
>   **网络抖动：**cluster-node-timeout，表示当某个节点持续 timeout 的时间失联时，才可以认定该节点出现故障，需要进行主从切换。如果没有这个选项，网络抖动会导致主从频繁切换 (数据的重新复制)。

**集群选举**

> - 过程：
>
>   ```p
>   1.slave发现自己的master变为FAIL
>   2.将自己记录的集群currentEpoch加1，并广播FAILOVER_AUTH_REQUEST 信息
>   3.其他节点收到该信息，只有master响应，判断请求者的合法性，并发送FAILOVER_AUTH_ACK，对每一个epoch只发送一次ack
>   4.尝试failover的slave收集master返回的FAILOVER_AUTH_ACK
>   5.slave收到超过半数master的ack后变成新Master(这里解释了集群为什么至少需要三个主节点，如果只有两个，当其中一个挂了，只剩一个主节点是不能选举成功的)
>   6.slave广播Pong消息通知其他集群节点。
>   ```
>
> - 延迟计算公式
>
>   ```p
>   DELAY = 500ms + random(0 ~ 500ms) + SLAVE_RANK * 1000ms
>   从节点并不是在主节点一进入 FAIL 状态就马上尝试发起选举，而是有一定延迟，一定的延迟确保我们等待FAIL状态在集群中传播，slave如果立即尝试选举，其它masters或许尚未意识到FAIL状态，可能会拒绝投票
>   SLAVE_RANK表示此slave已经从master复制数据的总量的rank。Rank越小代表已复制的数据越新。这种方式下，（理论上）持有最新数据的slave将会首先发起选举
>   ```

**集群脑裂数据丢失问题**

> redis集群没有过半机制会有脑裂问题，网络分区导致脑裂后多个主节点对外提供写服务，一旦网络分区恢复，会将其中一个主节点变为从节点，这时会有大量数据丢失。
>
> ```properties
> min-replicas-to-write 1  
> #写数据成功最少同步的slave数量，这个数量可以模仿大于半数机制配置，比如集群总共三个节点可以配置1，加上leader就是2，超过了半数
> 注意：这个配置在一定程度上会影响集群的可用性，比如slave要是少于1个，这个集群就算leader正常也不能提供服务了，需要具体场景权衡选择。
> ```

**集群是否完整才能对外提供服务**

> ```properties
> cluster-require-full-coverage
> #为no时，表示当负责一个插槽的主库下线且没有相应的从库进行故障恢复时，集群仍然可用，如果为yes则集群不可用。
> ```

**Redis集群为什么至少需要三个master节点，并且推荐节点数为奇数？**

> 最少三个节点：新master的选举需要大于半数的集群master节点同意，如果两个节点达不到选举条件
>
> 奇数：在满足选举该条件的基础上节省一个节点

**Redis集群对批量操作命令的支持**

> mset，mget这样的多个key的原生批量操作命令，redis集群只支持所有key落在同一slot的情况，如果有多个key一定要用mset命令在redis集群上操作，则可以在key的前面加上{XX}，这样参数数据分片hash计算的只会是大括号里的值，这样能确保不同的key能落到同一slot里去
>
> ```shell
> mset {user1}:1:name zhuge {user1}:1:age 18
> ```

### 六、Redis缓存设计问题解决方案

#### 1. 缓存雪崩

定义：缓存层支撑不住或宕掉后，后面的请求都会落到数据库上，造成数据库短时间内承受大量请求而崩掉

解决方案：

1.  保证缓存层服务高可用性，比如使用Redis Sentinel或Redis Cluster。

2. 依赖隔离组件为后端限流熔断并降级。比如使用Sentinel或Hystrix限流降级组件。

   比如服务降级，我们可以针对不同的数据采取不同的处理方式。当业务应用访问的是非核心数据（例如电商商品属性，用户信息等）时，暂时停止从缓存中查询这些数据，而是直接返回预定义的默认降级信息、空值或是错误提示信息；当业务应用访问的是核心数据（例如电商商品库存）时，仍然允许查询缓存，如果缓存缺失，也可以继续通过数据库读取。

3. 提前演练。 在项目上线前， 演练缓存层宕掉后， 应用以及后端的负载情况以及可能出现的问题， 在此基础上做一些预案设定。

#### 2. 缓存穿透

定义：查询一个根本不存在的数据，缓存层和存储层都不会命中，导致所有的请求都落到数据库上，造成数据库短时间内承受大量请求而崩掉

原因：

1、自身业务代码或者数据出现问题。

2、 一些恶意攻击、 爬虫等造成大量空命中。

解决方案：

1. 接口层增加校验，比如用户鉴权校验、加密校验等等

2. 缓存空对象：从缓存取不到数据，同时在数据库也未取到，可将key-value写成key-null，并设短点的缓存有效时间。防止攻击用户反复使用一个id暴力攻击

   ```java
   String get(String key) {
       String cacheValue = cache.get(key);
       // 缓存为空
       if (StringUtils.isBlank(cacheValue)) {
           // 从存储中获取
           String storageValue = storage.get(key);
           cache.set(key, storageValue);
           // 如果存储数据为空， 需要设置一个过期时间(300秒)
           if (storageValue == null) {
               cache.expire(key, 60 * 5);
           }
           return storageValue;
       } else {
           return cacheValue;
       }
   }
   ```

3. **布隆过滤器**

   > **布隆过滤器：某个值存在时，这个值可能不存在；当它说不存在时，那就肯定不存在**
   >
   > ​		   		 	**一个大型的位数组和几个不一样的无偏 hash 函数**（hash 值算得比较均匀）
   >
   > 向布隆过滤器中添加 key 时，会使用多个 hash 函数对 key 进行 hash 算得一个整数索引值然后对位数组长度进行取模运算得到一个位置，每个 hash 函数都会算得一个不同的位置。再把位数组的这几个位置都置为 1 就完成了 add 操作。
   >
   > 向布隆过滤器询问 key 是否存在时，跟 add 一样，也会把 hash 的几个位置都算出来，看看位数组中这几个位置是否都为 1，只要有一个位为 0，那么说明布隆过滤器中这个key 不存在。如果都是 1，这并不能说明这个 key 就一定存在，只是极有可能存在，因为这些位被置为 1 可能是因为其它的 key 存在所致。
   >
   > 缺点：Bitmap对于每个元素只能记录1bit信息，如果还想完成额外的功能，恐怕只能靠牺牲更多的空间、时间来完成
   >
   > 适用：数据命中不高、 数据相对固定、 实时性低（通常是数据集较大） 的应用场景， 代码维护较为复杂， 但是**缓存空间占用很少**。
   >
   > 
   >
   > ```java
   > import org.redisson.Redisson;
   > import org.redisson.api.RBloomFilter;
   > import org.redisson.api.RedissonClient;
   > import org.redisson.config.Config;
   > public class RedissonBloomFilter {
   >     public static void main(String[] args) {
   >         Config config = new Config();
   >         config.useSingleServer().setAddress("redis://localhost:6379");
   >         //构造Redisson
   >         RedissonClient redisson = Redisson.create(config);
   >         RBloomFilter<String> bloomFilter = redisson.getBloomFilter("nameList");
   >         //初始化布隆过滤器：预计元素为100000000L,误差率为3%,这两个参数算出底层bit数组大小
   >         bloomFilter.tryInit(100000000L,0.03);
   >         //将zhuge插入到布隆过滤器中
   >         bloomFilter.add("zhuge");
   > 
   >         //判断下面号码是否在布隆过滤器中
   >         System.out.println(bloomFilter.contains("guojia"));//false
   >         System.out.println(bloomFilter.contains("baiqi"));//false
   >         System.out.println(bloomFilter.contains("zhuge"));//true
   >     }
   > }
   > 
   > // ===============================
   > //初始化布隆过滤器
   > RBloomFilter<String> bloomFilter = redisson.getBloomFilter("nameList");
   > //初始化布隆过滤器：预计元素为100000000L,误差率为3%
   > bloomFilter.tryInit(100000000L,0.03);
   >         
   > //把所有数据存入布隆过滤器
   > void init(){
   >     for (String key: keys) {
   >         bloomFilter.put(key);
   >     }
   > }
   > 
   > String get(String key) {
   >     // 从布隆过滤器这一级缓存判断下key是否存在
   >     Boolean exist = bloomFilter.contains(key);
   >     if(!exist){
   >         return "";
   >     }
   >     // 从缓存中获取数据
   >     String cacheValue = cache.get(key);
   >     // 缓存为空
   >     if (StringUtils.isBlank(cacheValue)) {
   >         // 从存储中获取
   >         String storageValue = storage.get(key);
   >         cache.set(key, storageValue);
   >         // 如果存储数据为空， 需要设置一个过期时间(300秒)
   >         if (storageValue == null) {
   >             cache.expire(key, 60 * 5);
   >         }
   >         return storageValue;
   >     } else {
   >         // 缓存非空
   >         return cacheValue;
   >     }
   > }
   > ```
   >
   > **注意：**布隆过滤器不能删除数据，如果要删除得重新初始化数据。

#### 3.缓存击穿（失效）

定义：由于大批量缓存在同一时间失效可能导致大量请求同时穿透缓存直达数据库，可能会造成数据库瞬间压力过大甚至挂掉，对于这种情况我们在批量增加缓存时最好将这一批数据的缓存过期时间设置为一个时间段内的不同时间。

```java
String get(String key) {
    // 从缓存中获取数据
    String cacheValue = cache.get(key);
    // 缓存为空
    if (StringUtils.isBlank(cacheValue)) {
        // 从存储中获取
        String storageValue = storage.get(key);
        cache.set(key, storageValue);
        //设置一个过期时间(300到600之间的一个随机数)
        int expireTime = new Random().nextInt(300)  + 300;
        if (storageValue == null) {
            cache.expire(key, expireTime);
        }
        return storageValue;
    } else {
        // 缓存非空
        return cacheValue;
    }
}
```

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

#### 6. 热点缓存key重建优化

“缓存+过期时间”的策略问题：

> - 当前key是一个热点key（例如一个热门的娱乐新闻），并发量非常大。
> - 重建缓存不能在短时间完成， 可能是一个复杂计算

此时缓存失效的瞬间，大量线程来重建缓存， 造成后端负载加大， 甚至可能会让应用崩溃

解决：利用互斥锁来解决，只允许一个线程重建缓存

```java
String get(String key) {
    // 从Redis中获取数据
    String value = redis.get(key);
    // 如果value为空， 则开始重构缓存
    if (value == null) {
        // 只允许一个线程重建缓存， 使用nx， 并设置过期时间ex
        String mutexKey = "mutext:key:" + key;
        if (redis.set(mutexKey, "1", "ex 180", "nx")) {
             // 从数据源获取数据
            value = db.get(key);
            // 回写Redis， 并设置过期时间
            redis.setex(key, timeout, value);
            // 删除key_mutex
            redis.delete(mutexKey);
        }// 其他线程休息50毫秒后重试
        else {
            Thread.sleep(50);
            get(key);
        }
    }
    return value;
}
```

#### 7. 缓存与数据库双写不一致

todo







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







#### 管道及Lua脚本

**管道（Pipeline）**

> 客户端可以一次性发送多个请求而不用等待服务器的响应，待所有命令都发送完后再一次性读取服务的响应，其内部也是redis一条一条执行，前一条失败不会影响下一条执行
>
> 优点：降低多条命令的网络传输开销
>
> 缺点：pipeline方式打包命令发送，redis必须在**处理完所有命令前先缓存起所有命令的处理结果**。打包的命令越多，缓存消耗内存也越多

**Lua脚本**

> 优点：
>
> 1. **减少网络开销**：跟管道类似
> 2. **原子操作**：Redis会将整个脚本作为一个整体执行，中间不会被其他命令插入。**管道不是原子的，不过redis的批量操作命令(类似mset)是原子的。**
> 3. **替代redis的事务功能**：redis自带的事务功能很鸡肋，报错不支持回滚
>
> ```shell
> EVAL script numkeys key [key ...] arg [arg ...]　
> 
> script参数是一段Lua脚本程序，它会被运行在Redis服务器上下文中
> numkeys参数用于指定键名参数的个数
> 键名参数 key [key ...] 从EVAL的第三个参数开始算起，表示在脚本中所用到的那些Redis键(key)，这些键名参数可以在 Lua中通过全局变量KEYS数组，用1为基址的形式访问( KEYS[1] ， KEYS[2] ，以此类推)。
> 不是键名参数的附加参数 arg [arg ...] ，可以在Lua中通过全局变量ARGV数组访问，访问的形式和KEYS变量类似( ARGV[1] 、 ARGV[2] ，诸如此类)
> 
> eval "return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}" 2 key1 key2 first second
> 1) "key1"
> 2) "key2"
> 3) "first"
> 4) "second"
> 
> ```

**注意，不要在Lua脚本中出现死循环和耗时的运算，否则redis会阻塞，将不接受其他的命令， 所以使用时要注意不能出现死循环、耗时的运算。redis是单进程、单线程执行脚本。管道不会阻塞redis。**



































### 附录一：redis和Memcached不同

![1609777243430](..\resource\redis_vs_memcached.png)



### 附录二：redis安装

##### 1. 单机安装

```powershell
下载地址：http://redis.io/download
安装步骤：
# 安装gcc
yum install gcc
# 把下载好的redis-5.0.3.tar.gz放在/usr/local文件夹下，并解压
wget http://download.redis.io/releases/redis-5.0.3.tar.gz
tar xzf redis-5.0.3.tar.gz
cd redis-5.0.3
# 进入到解压好的redis-5.0.3目录下，进行编译与安装
make
# 修改配置
daemonize yes  #后台启动
protected-mode no  #关闭保护模式，开启的话，只有本机才可以访问redis
requirepass 123
# bind 对外ip bind绑定的是自己机器网卡的ip，如果有多块网卡可以配多个ip，代表允许客户端通过机器的哪些网卡ip去访问，内网一般可以不配置bind
bind 127.0.0.1
# 启动服务
src/redis-server redis.conf
# 验证启动是否成功 
ps -ef | grep redis 
# 进入redis客户端 
src/redis-cli 
# 退出客户端
quit
# 退出redis服务： 
（1）pkill redis-server 
（2）kill 进程号                       
（3）src/redis-cli shutdown 
```

##### 2. 主从复制架构搭建

> 1. 单机的基础上复制一份redis.conf
>
>    cp redis.conf  6380.conf
>
> 2. 修改以下配置
>
>    ```properties
>    port 6380
>    # 把pid进程号写入pidfile配置的文件
>    pidfile /var/run/redis_6380.pid 
>    logfile "6380.log"
>    # 指定数据存放目录,需要提前建好目录，不然会报错
>    dir /usr/local/redis-5.0.3/data/6380
>    # redis 认证
>    requirepass 123　　　　
>    # bind
>    bind 127.0.0.1
>    #配置主从复制  主节点的redis实例复制数据，Redis 5.0之前使用slaveof
>    replicaof 192.168.0.60 6379  
>    # 主节点密码
>    masterauth 123　　　　　　　　
>    # 配置从节点只读
>    replica-read-only yes 
>    ```
>
> 3. 启动从节点
>
>    redis-server 6380.conf
>
>    redis-cli -p 6380

##### 3. 哨兵架构搭建

> 1. 复制一份sentinel.conf 文件
>
>    ```shell
>    cp sentinel.conf /usr/local/redis/conf/
>    ```
>
> 2. 修改配置文件
>
>    ```properties
>    port 26379
>    daemonize yes
>    pidfile "/var/run/redis-sentinel-26379.pid"
>    logfile "26379.log"
>    dir "/usr/local/redis-5.0.3/data"
>    # sentinel monitor <master-redis-name> <master-redis-ip> <master-redis-port> <quorum>
>    # quorum是一个数字，指明当有多少个sentinel认为一个master失效时(值一般为：sentinel总数/2 + 1)，master才算真正失效
>    # mymaster这个名字随便取，客户端访问时会用到
>    sentinel monitor mymaster 192.168.0.60 6379 2 
>    # 多少秒内主节点没有回应，将被认为节点下线，默认为30秒，单位为毫秒
>    sentinel down-after-milliseconds mymaster 30000　
>    # 指定redis主从节点密码
>    sentinel auth-pass mymaster 123　　　　　　　　
>    ```
>
> 3. 启动sentinel哨兵实例
>
>    src/redis-sentinel sentinel-26379.conf
>
> 4. 查看info信息

##### 4. 集群搭建

> redis集群需要至少三个master节点，再给每个master搭建一个slave节点

```shell
第一步：在第一台机器的/usr/local下创建文件夹redis-cluster，然后在其下面分别创建2个文件夾如下
（1）mkdir -p /usr/local/redis-cluster
（2）mkdir 8001 8004

第一步：把之前的redis.conf配置文件copy到8001下，修改如下内容：
（1）daemonize yes
（2）port 8001（分别对每个机器的端口号进行设置）
（3）pidfile /var/run/redis_8001.pid  # 把pid进程号写入pidfile配置的文件
（4）dir /usr/local/redis-cluster/8001/（指定数据文件存放位置，必须要指定不同的目录位置，不然会丢失数据）
（5）cluster-enabled yes（启动集群模式）
（6）cluster-config-file nodes-8001.conf（集群节点信息文件，这里800x最好和port对应上）
（7）cluster-node-timeout 10000
 (8)# bind 127.0.0.1（bind绑定的是自己机器网卡的ip，如果有多块网卡可以配多个ip，代表允许客户端通过机器的哪些网卡ip去访问，内网一般可以不配置bind，注释掉即可）
 (9)protected-mode  no   （关闭保护模式）
 (10)appendonly yes
如果要设置密码需要增加如下配置：
 (11)requirepass zhuge     (设置redis访问密码)
 (12)masterauth zhuge      (设置集群节点间访问密码，跟上面一致)

第三步：把修改后的配置文件，copy到8004，修改第2、3、4、6项里的端口号，可以用批量替换：
:%s/源字符串/目的字符串/g 

第四步：另外两台机器也需要做上面几步操作，第二台机器用8002和8005，第三台机器用8003和8006

第五步：分别启动6个redis实例，然后检查是否启动成功
（1）/usr/local/redis-5.0.3/src/redis-server /usr/local/redis-cluster/800*/redis.conf
（2）ps -ef | grep redis 查看是否启动成功
    
第六步：用redis-cli创建整个redis集群(redis5以前的版本集群是依靠ruby脚本redis-trib.rb实现)
# 下面命令里的1代表为每个创建的主服务器节点创建一个从服务器节点
# 执行这条命令需要确认三台机器之间的redis实例要能相互访问，可以先简单把所有机器防火墙关掉，如果不关闭防火墙则需要打开redis服务端口和集群节点gossip通信端口16379(默认是在redis端口号上加1W)
# 关闭防火墙
# systemctl stop firewalld # 临时关闭防火墙
# systemctl disable firewalld # 禁止开机启动
# 注意：下面这条创建集群的命令大家不要直接复制，里面的空格编码可能有问题导致创建集群不成功
（1）/usr/local/redis-5.0.3/src/redis-cli -a zhuge --cluster create --cluster-replicas 1 192.168.0.61:8001 192.168.0.62:8002 192.168.0.63:8003 192.168.0.61:8004 192.168.0.62:8005 192.168.0.63:8006 

第七步：验证集群：
（1）连接任意一个客户端即可：./redis-cli -c -h -p (-a访问服务端密码，-c表示集群模式，指定ip地址和端口号）
    如：/usr/local/redis-5.0.3/src/redis-cli -a zhuge -c -h 192.168.0.61 -p 800*
（2）进行验证： cluster info（查看集群信息）、cluster nodes（查看节点列表）
（3）进行数据操作验证
（4）关闭集群则需要逐个进行关闭，使用命令：
/usr/local/redis-5.0.3/src/redis-cli -a zhuge -c -h 192.168.0.60 -p 800* shutdown
```

##### 5. 集群水平扩展及拆分

> 在原始集群基础上再增加一主(8007)一从(8008)
>
> ```shell
> 1、增加redis实例
> # 在/usr/local/redis-cluster下创建8007和8008文件夹，并拷贝8001文件夹下的redis.conf文件到8007和8008这两个文件夹下
> mkdir 8007 8008
> cd 8001
> cp redis.conf /usr/local/redis-cluster/8007/
> cp redis.conf /usr/local/redis-cluster/8008/
> 
> # 修改8007文件夹下的redis.conf配置文件
> vim /usr/local/redis-cluster/8007/redis.conf
> # 修改如下内容：
> port:8007
> dir /usr/local/redis-cluster/8007/
> cluster-config-file nodes-8007.conf
> 
> # 修改8008文件夹下的redis.conf配置文件
> vim /usr/local/redis-cluster/8008/redis.conf
> 修改内容如下：
> port:8008
> dir /usr/local/redis-cluster/8008/
> cluster-config-file nodes-8008.conf
> 
> # 启动8007和8008俩个服务并查看服务状态
> /usr/local/redis-5.0.3/src/redis-server /usr/local/redis-cluster/8007/redis.conf
> /usr/local/redis-5.0.3/src/redis-server /usr/local/redis-cluster/8008/redis.conf
> ps -el | grep redis
> ```
>
> todo
>
> 

### 附录三：redis.conf配置文件解析

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

### 附录四：开发规范

#### 1. 键值设计

> 1. **key名设计**
>
>    - (1)【建议】: 可读性和可管理性    
>
>      以业务名(或数据库名)为前缀(防止key冲突)，用冒号分隔，比如业务名:表名:id
>
>      ```properties
>      trade:order:1
>      ```
>
>    - (2)【建议】：简洁性
>
>      保证语义的前提下，控制key的长度，当key较多时，内存占用也不容忽视
>
>      ```properties
>      user:{uid}:friends:messages:{mid} 简化为 u:{uid}:fr:m:{mid}
>      ```
>
>    - (3)【强制】：不要包含特殊字符
>
> 2. **value设计**
>
>    - (1)【强制】：拒绝bigkey(防止网卡流量、慢查询)
>
>      ```properties
>      字符串类型：它的big体现在单个value值很大，一般认为超过10KB就是bigkey。
>      非字符串类型：哈希、列表、集合、有序集合，个数不要超过5000
>      ```
>
>      危害：1.导致redis阻塞  2.网络拥塞  3.过期删除
>
>      **优化bigkey**
>
>      1. 拆
>
>      big list： list1、list2、...listN
>
>      big hash：可以讲数据分段存储，比如一个大的key，假设存了1百万的用户数据，可以拆分成200个key，每个key下面存放5000个用户数据
>
>      2. 如果bigkey不可避免，也要思考一下要不要每次把所有元素都取出来(例如有时候仅仅需要hmget，而不是hgetall)，删除也是一样，尽量使用优雅的方式来处理。
>
>    - (2)【推荐】：选择适合的数据类型
>
>      ```properties
>      hmset user:1 name tom age 19 favor football
>      ```
>
>    - (3)【推荐】：控制key的生命周期，redis不是垃圾桶。
>
>      expire设置过期时间(条件允许可以打散过期时间，防止集中过期)

#### 2. 命令使用

1.【推荐】 O(N)命令关注N的数量

例如hgetall、lrange、smembers、zrange、sinter等并非不能使用，但是需要明确N的值。有遍历的需求可以使用hscan、sscan、zscan代替。

2.【推荐】：禁用命令

禁止线上使用keys、flushall、flushdb等，通过redis的rename机制禁掉命令，或者使用scan的方式渐进式处理。

3.【推荐】合理使用select

redis的多数据库较弱，使用数字进行区分，很多客户端支持较差，同时多业务用多数据库实际还是单线程处理，会有干扰。

4.【推荐】使用批量操作提高效率

原生命令：例如mget、mset。 非原生命令：可以使用pipeline提高效率。

5.【建议】Redis事务功能较弱，不建议过多使用，可以用lua替代

#### 3. 客户端使用

1.【推荐】避免多个应用使用一个Redis实例

正例：不相干的业务拆分，公共数据做服务化。

2.【推荐】使用带有连接池的数据库，可以有效控制连接，同时提高效率

```java
JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
jedisPoolConfig.setMaxTotal(5);
jedisPoolConfig.setMaxIdle(2);
jedisPoolConfig.setTestOnBorrow(true);
JedisPool jedisPool = new JedisPool(jedisPoolConfig, "192.168.0.60", 6379, 3000, null);
Jedis jedis = null;
try {
    jedis = jedisPool.getResource();
    //具体的命令
    jedis.executeCommand()
} catch (Exception e) {
    logger.error("op key {} error: " + e.getMessage(), key, e);
} finally {
    //注意这里不是关闭连接，在JedisPool模式下，Jedis会被归还给资源池。
    if (jedis != null) 
        jedis.close();
}
```

| 序   | 参数名             | 含义                                                         | 默认值           | 使用建议                                          |
| ---- | ------------------ | ------------------------------------------------------------ | ---------------- | :------------------------------------------------ |
| 1    | maxTotal           | 资源池中最大连接数                                           | 8                | 设置建议见下面                                    |
| 2    | maxIdle            | 资源池允许最大空闲的连接数                                   | 8                | 设置建议见下面                                    |
| 3    | minIdle            | 资源池确保最少空闲的连接数                                   | 0                | 设置建议见下面                                    |
| 4    | blockWhenExhausted | 当资源池用尽后，调用者是否要等待。只有当为true时，下面的maxWaitMillis才会生效 | true             | 建议使用默认值                                    |
| 5    | maxWaitMillis      | 当资源池连接用尽后，调用者的最大等待时间(单位为毫秒)         | -1：表示永不超时 | 不建议使用默认值                                  |
| 6    | testOnBorrow       | 向资源池借用连接时是否做连接有效性检测(ping)，无效连接会被移除 | false            | 业务量很大时候建议设置为false(多一次ping的开销)。 |
| 7    | testOnReturn       | 向资源池归还连接时是否做连接有效性检测(ping)，无效连接会被移除 | false            | 业务量很大时候建议设置为false(多一次ping的开销)。 |
| 8    | jmxEnabled         | 是否开启jmx监控，可用于监控                                  | true             | 建议开启，但应用本身也要开启                      |

**优化建议：**

1）**maxTotal**：最大连接数，早期的版本叫maxActive

考虑的因素比较多：

>  业务希望Redis并发量
>
> 客户端执行命令时间
>
> Redis资源：例如 nodes(例如应用个数) * maxTotal 是不能超过redis的最大连接数maxclients。
>
> 资源开销：例如虽然希望控制**空闲连接**(连接池此刻可马上使用的连接)，但是不希望因为连接池的频繁释放创建连接造成不必靠开销。
>
> 例：
>
> ​		一次命令时间（borrow|return resource + Jedis执行命令(含网络) ）的平均耗时约为1ms，一个连接的QPS大约是1000  
>
> ​		业务期望的QPS是50000
>
> 理论上需要的资源池大小是50000 / 1000 = 50个。但事实上这是个理论值，还要考虑到要比理论值预留一些资源，通常来讲maxTotal可以比理论值大一些
>
> 但这个值不是越大越好，一方面连接太多占用客户端和服务端资源，另一方面对于Redis这种高QPS的服务器，一个大命令的阻塞即使设置再大资源池仍然会无济于事。



2）**maxIdle和minIdle**

> maxIdle  实际上才是业务需要的最大连接数，maxTotal是为了**给出余量**
>
> 一般推荐maxIdle可以设置为按上面的业务期望QPS计算出来的理论连接数，maxTotal可以再放大一倍。
>
> minIdle（最小空闲连接数），"**至少需要保持的空闲连接数**"
>
> **连接池预热**
>
> ```java
> List<Jedis> minIdleJedisList = new ArrayList<Jedis>(jedisPoolConfig.getMinIdle());
> 
> for (int i = 0; i < jedisPoolConfig.getMinIdle(); i++) {
>     Jedis jedis = null;
>     try {
>         jedis = pool.getResource();
>         minIdleJedisList.add(jedis);
>         jedis.ping();
>     } catch (Exception e) {
>         logger.error(e.getMessage(), e);
>     } finally {
>         //注意，这里不能马上close将连接还回连接池，否则最后连接池里只会建立1个连接。。
>         //jedis.close();
>     }
> }
> //统一将预热的连接还回连接池
> for (int i = 0; i < jedisPoolConfig.getMinIdle(); i++) {
>     Jedis jedis = null;
>     try {
>         jedis = minIdleJedisList.get(i);
>         //将连接归还回连接池
>         jedis.close();
>     } catch (Exception e) {
>         logger.error(e.getMessage(), e);
>     } finally {
>     }
> }
> ```
>
> 







































