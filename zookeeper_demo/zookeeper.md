## Zookeeper



### 一、基本信息

##### 1. Zookeeper是什么

> ZooKeeper是用于分布式应用程序的协调服务。分布式应用程序可以以此同步，节点状态、配置等信息、服务注册等信息。JAVA编写，支持JAVA 和C两种语言的客户端

##### 2. Znode节点

> zookeeper 中数据基本单元叫节点，节点之下可包含子节点(跟文件系统类似)，最后以树级方式程现。每个节点拥有唯一的路径path。客户端基于PATH上传节点数据，zookeeper 收到后会实时通知对该路径进行监听的客户端。
>
> znode 结构包括：
>
> - path 唯一路径
> - childNode 子节点
> - stat  状态属性
> - type  节点类型

1. 节点类型

   - PERSISTENT（持久节点）默认  create /test

   - PERSISTENT_SEQUENTIAL(持久序号节点)  

     在路径后加序号做后缀，适合分布式锁、分布式选举等场景 

     创建时加-s 参数  create -s /test   返回实际路径  Created /test0000000001 

   - EPHEMERAL（临时节点）

     临时节点在其创建的客户端会话断开后自动删除，适用于心跳，服务发现等场景。

     创建添加-e 参数  create -e /temp

   - EPHEMERAL_SEQUENTIAL（临时序号节点）

     在路径后加序号做后缀，在会话断开后删除

     -s -e

2. 节点属性

   ```properties
   # 查看节点属性
   stat /test
   # 创建节点的事务ID
   cZxid = 0x100000006
   # 创建时间
   ctime = Fri Jan 22 00:58:56 CST 2021
   # 修改本节点的事务ID（修改数据会变更）
   mZxid = 0x100000006
   # 修改时间
   mtime = Fri Jan 22 00:58:56 CST 2021
   # 子节点变更的事务ID(子节点添加，删除，不包括数据变更)
   pZxid = 0x100000006
   # 子节点更改次数(子节点添加，删除，不包括子节点数据的变更)
   cversion = 0
   # 数据版本
   dataVersion = 0
   # 权限版本
   aclVersion = 0
   # 临时节点会话ID  为0x0时表示为持久节点
   ephemeralOwner = 0x300153dcb470000
   # 数据长度
   dataLength = 0
   # 子节点数(不包含子子节点)
   numChildren = 0
   ```

3. 节点监听（只能监听一次）

   客户添加 -w 参数可实时监听节点与子节点的变化，并且实时收到通知。非常适用保障分布式情况下的数据一至性

   | 命令                 | 描述                                 |
   | -------------------- | ------------------------------------ |
   | ls -w path           | 监听子节点的变化（增，删）           |
   | get -w path          | 监听节点数据的变化                   |
   | stat -w path         | 监听节点属性的变化                   |
   | printwatches on\|off | 触发监听后，是否打印监听事件(默认on) |

   ![1611250209663](..\resource\zookeeper1611250209663.png)

4. **acl权限设置**

   > ACL全称为Access Control List（访问控制列表），用于控制资源的访问权限。
   >
   > ZooKeeper使用ACL来控制对其znode的防问。基于scheme​ :id :permission的方式进行权限控制。
   >
   > **特殊说明**：权限仅对当前节点有效，不会让子节点继承。如限制了IP防问A节点，但不妨碍该IP防问A的子节点 /A/B。
   >
   > scheme表示授权模式
   >
   > | 方案   | 描述                                                         |
   > | ------ | ------------------------------------------------------------ |
   > | world  | 开放模式，world表示全世界都可以访问（这是默认设置）          |
   > | ip     | ip模式，限定客户端IP防问                                     |
   > | auth   | 用户密码认证模式，只有在会话中添加了认证才可以防问           |
   > | digest | 与auth类似，区别在于auth用明文密码，而digest 用sha-1+base64加密后的密码。在实际使用中digest 更常见。 |
   >
   > id 模式对应值
   >
   > permission即具体的增删改权限位
   >
   > | 权限位 | 权限   | 描述                             |
   > | ------ | ------ | -------------------------------- |
   > | c      | CREATE | 可以创建子节点                   |
   > | d      | DELETE | 可以删除子节点（仅下一级节点）   |
   > | r      | READ   | 可以读取节点数据及显示子节点列表 |
   > | w      | WRITE  | 可以设置节点数据                 |
   > | a      | ADMIN  | 可以设置节点访问控制列表权限     |
   >
   > 相关命令：
   >
   > getAcl <path>  读取ACL权限
   >
   > setAcl <path> <acl>   设置ACL权限
   >
   > addauth <scheme> <auth>    添加认证用户
   >
   > 相关模式示例：
   >
   > **world权限** setAcl <path> world:anyone:<权限位>
   >
   > **IP权限** setAcl <path> ip:<ip地址|地址段>:<权限位>
   >
   > **auth模式示例:**setAcl <path> auth:<用户名>:<密码>:<权限位>
   >
   > **digest 权限示例：** addauth digest <用户名>:<密码>
   >
   > 注：linux 加密  echo -n admin:123456 | openssl dgst -binary -sha1 | openssl base64

   

##### 3. 客户端命令

> 基本命令
>
> **close**    关闭当前会话
>
> **connect host:port**   重新连接指定Zookeeper服务
>
> **create [-s] [-e] [-c] [-t ttl] path [data] [acl]**   创建节点
>
> **delete [-v version] path**  删除节点，(不能存在子节点）
>
> **deleteall path**    删除路径及所有子节点
>
> **setquota -n|-b val path**   设置节点限额 -n 子节点数 -b 字节数
>
>  **listquota path**   查看节点限额
>
> **delquota [-n|-b] path**  删除节点限额
>
>  **get [-s] [-w] path**  查看节点数据 -s 包含节点状态 -w 添加监听 
>
> getAcl [-s] path
>
> **ls [-s] [-w] [-R] path**  列出子节点 -s状态 -R 递归查看所有子节点 -w 添加监听
>
> **printwatches on|off**   是否打印监听事件
>
> **quit**   退出客户端
>
>  **history**   查看执行的历史记录
>
> **redo cmdno**  重复 执行命令，history 中命令编号确定
>
> **removewatches path [-c|-d|-a] [-l]**  删除指定监听
>
> set [-s] [-v version] path data  设置值
>
> **setAcl [-s] [-v version] [-R] path acl**  为节点设置ACL权限
>
> **stat [-w] path**  查看节点状态 -w 添加监听
>
> **sync path**  强制同步节点



### 二、客户端API

```xml
<dependency>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>3.6.2</version>
</dependency>
```

示例代码：todo



第三方客户端ZkClient 



### 三、Zookeeper集群

zookeeper集群为了保证系统的性能承载更多的客户端连接专门提供的机制，能实现以下功能：

- 读写分离：提高承载，为更多的客户端提供连接并保障性能
- 主从自动切换：提供服务容错

#### 1. 集群角色

zookeeper集群共分三种角色

| 角色         | 描述                                                         |
| ------------ | ------------------------------------------------------------ |
| **leader**   | 主节点，又名领导者。用于写入数据，通过选举产生，如果宕机将会选举新的主节点。 |
| **follower** | 子节点，又名追随者。用于实现数据的读取。同时他也是主节点的备选节点，并用拥有投票权。 |
| **observer** | 次级子节点，又名观察者。用于读取数据，与fllower区别在于没有投票权，不能选为主节点。并且在计算集群可用状态时不会将observer计算入内。 |



#### 2. 选举机制

1. 选举规则

   第一轮投票全部投给自己，第二轮投票给myid比自己大的相邻节点，如果得票超过半数，选举结束。

2. 选举触发条件

   - 服务节点初始化启动
   - 半数以上的节点无法和Leader建立连接

3. 启动选举情况

   当节点初始起动时会在集群中寻找Leader节点，如果找到则与Leader建立连接，其自身状态变化**follower**或**observer。**如果没有找到Leader，当前节点状态将变化LOOKING，进入选举流程

   在集群运行其间如果有follower或observer节点宕机只要不超过半数并不会影响整个集群服务的正常运行

   如果leader宕机，将暂停对外服务，所有follower将进入LOOKING 状态，进入选举流程

   如果超过半数服务挂掉会导致整个集群挂掉

#### 3. 数据同步机制

zookeeper的数据同步保证各节点中数据的一致性，主要涉及两个流程：

- 正常客户端数据提交
- 集群某个节点宕机恢复后数据同步

1. 客户端正常写入流程：

   > 1. client向zk中的server发送写请求，如果该server不是leader，则会将该写请求转发给leader server，leader将请求事务以proposal形式分发给follower；
   > 2. 当follower收到收到leader的proposal时，根据接收的先后顺序处理proposal
   > 3. 当Leader收到follower针对某个proposal过半的ack后，则发起事务提交，重新发起一个commit的proposal
   > 4. Follower收到commit的proposal后，记录事务提交，并把数据更新到内存数据库
   > 5. 当写成功后，反馈给client
   >
   > **zookeeper的写入是等过半客户端写入成功后才返回状态，是强一致性，与redis不同**

2. 服务节点启动同步：

   > 当宕机节点启动时，第一件事情就是找当前的Leader，比对数据是否一至。不一至则开始同步,同步完成之后在进行对外提供服务
   >
   > **ZXID说明：**
   >
   > ZXID是一个长度64位的数字，其中低32位是按照数字递增，任何数据的变更都会导致,低32位的数字简单加1。高32位是leader周期编号，每当选举出一个新的leader时，新的leader就从本地事物日志中取出ZXID,然后解析出高32位的周期编号，进行加1，再将低32位的全部设置为0。这样就保证了每次新选举的leader后，保证了ZXID的唯一性而且是保证递增的 



### 四、Zookeeper应用场景

#### 1. 分布式集群管理

需求：

> 1. 主动查看线上服务节点
>
> 2. 查看服务节点资源使用情况
>
> 3. 服务离线通知
>
> 4. 服务资源（CPU、内存、硬盘）超出阀值通知

架构设计：

![1611283872851](..\resource\zookeeper1611283872851.png)

功能实现：

> **节点结构：**
>
> 1. server-manger // 根节点
>
> a. server00001 :<json> //服务节点 1
>
> b. server00002 :<json>//服务节点 2
>
> c. server........n :<json>//服务节点 n
>
> 服务状态信息:ip cpu memory disk
>
> **数据生成与上报：**
>
> 1. 创建临时节点：
>
> 2. 定时变更节点状态信息：
>
> **主动查询：**
>
> 1、实时查询 zookeeper 获取集群节点的状态信息。
>
> **被动通知：**
>
> 1. 监听根节点下子节点的变化情况,如果CPU 等硬件资源低于警告位则发出警报。



#### 2. 分布式注册中心

#### 3. 分布式JOB

#### 4. 分布式锁









































### 附录一：Zookeeper安装

##### 1. 单机安装：

> 1.下载
> wget https://mirror.bit.edu.cn/apache/zookeeper/zookeeper-3.6.2/apache-zookeeper-3.6.2-bin.tar.gz
> 2. 解压
>   tar zxvf apache-zookeeper-3.6.2-bin.tar.gz
>
> 3. 拷贝默认配置
>   cd apache-zookeeper-3.6.2-bin/conf/
>   cp zoo_sample.cfg zoo.cfg
>
> 4. 启动客户端
>
>   ./bin/zkServer start

```properties
参数配置
# zookeeper时间配置中的基本单位 (毫秒)
tickTime=2000
# 允许follower初始化连接到leader最大时长，它表示tickTime时间倍数 即:initLimit*tickTime
initLimit=10
# 允许follower与leader数据同步最大时长,它表示tickTime时间倍数 
syncLimit=5
#zookeper 数据存储目录
dataDir=/tmp/zookeeper
#对客户端提供的端口号
clientPort=2181
#单个客户端与zookeeper最大并发连接数
maxClientCnxns=60
# 保存的数据快照数量，之外的将会被清除
autopurge.snapRetainCount=3
#自动触发清除任务时间间隔，小时为单位。默认为0，表示不自动清除。
autopurge.purgeInterval=1
```

> 4. 连接客户端
>
>  ./bin/zkCli.sh

##### 2. 集群安装

> 1. 配置说明
>
>    **节点ID**：服务id手动指定1至125之间的数字，并写到对应服务节点的 {dataDir}/myid 文件中
>
>    **IP地址：**节点的远程IP地址
>
>    **数据同步端口：**主从同时数据复制端口
>
>    **选举端口：**主从节点选举端口
>
> 2. 配置文件*3 
>
>    cp zoo.cfg zoo1.cfg  * 3

```properties
# The number of milliseconds of each tick
tickTime=2000
# The number of ticks that the initial 
# synchronization phase can take
initLimit=10
# The number of ticks that can pass between 
# sending a request and getting an acknowledgement
syncLimit=5
# the directory where the snapshot is stored.
# do not use /tmp for storage, /tmp here is just 
# example sakes.
# 节点2/data/2 节点3/data/3
dataDir=/usr/local/zookeeper/apache-zookeeper-3.6.2-bin/data/1  
# the port at which the clients will connect
# 节点2  2182 节点3  2183
clientPort=2181
# server.[节点id]=[ip]:[数据同步端口]:[选举端口]
# server.1=[ip]:2888:3888:observer 配置观察者
server.1=[ip]:2888:3888
server.2=[ip]:2888:3888
server.3=[ip]:2888:3888
```

> 2. 分别创建三个data目录下节点用于存储各个节点数据
>
>    mkdir data/1
>
>    mkdir data/2
>
>    mkdir data/3
>
>    每个目录下编写myid文件
>
>    echo 1 > 1/myid
>    echo 2 > 2/myid
>    echo 3 > 3/myid
>
> 3. 启动服务
>
>    ./bin/zkServer start conf/zoo1.cfg
>
>    ./bin/zkServer start conf/zoo2.cfg
>
>    ./bin/zkServer start conf/zoo3.cfg
>
> 4. 查看节点状态
>
>    ./zkServer.sh status ../conf/zoo1.cfg
>    		ZooKeeper JMX enabled by default
>    		Using config: ../conf/zoo1.cfg
>    		Client port found: 2181. Client address: localhost. Client SSL: false.
>    		Mode: **follower** (从节点)
>
>    ./zkServer.sh status ../conf/zoo2.cfg
>    		ZooKeeper JMX enabled by default
>    		Using config: ../conf/zoo2.cfg
>    		Client port found: 2182. Client address: localhost. Client SSL: false.
>    		Mode: **leader**(主节点)
>
> 5. 连接集群 (单个连接或集群连接)
>
>    ./zkCli.sh -server 127.0.0.1:2181
>
>    ./zkCli.sh -server 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183



### 附录二：四字运维命令

```shell
ZooKeeper响应少量命令。每个命令由四个字母组成。可通过telnet或nc向ZooKeeper发出命令。
这些命令默认是关闭的，需要配置4lw.commands.whitelist来打开，可打开部分或全部示例如下：
#打开指定命令
4lw.commands.whitelist=stat, ruok, conf, isro
#打开全部
4lw.commands.whitelist=*

安装Netcat工具，已使用nc命令 
#安装Netcat 工具
yum install -y nc
#查看服务器及客户端连接状态
echo stat | nc localhost 2181
命令列表
1.conf：3.3.0中的新增功能：打印有关服务配置的详细信息。
2.缺点：3.3.0中的新增功能：列出了连接到该服务器的所有客户端的完整连接/会话详细信息。包括有关已接收/已发送的数据包数量，会话ID，操作等待时间，最后执行的操作等信息。
3.crst：3.3.0中的新增功能：重置所有连接的连接/会话统计信息。
4.dump：列出未完成的会话和临时节点。这仅适用于领导者。
5.envi：打印有关服务环境的详细信息
6.ruok：测试服务器是否以非错误状态运行。如果服务器正在运行，它将以imok响应。否则，它将完全不响应。响应“ imok”不一定表示服务器已加入仲裁，只是服务器进程处于活动状态并绑定到指定的客户端端口。使用“ stat”获取有关状态仲裁和客户端连接信息的详细信息。
7.srst：重置服务器统计信息。
8.srvr：3.3.0中的新功能：列出服务器的完整详细信息。
9.stat：列出服务器和连接的客户端的简要详细信息。
10.wchs：3.3.0中的新增功能：列出有关服务器监视的简要信息。
11.wchc：3.3.0中的新增功能：按会话列出有关服务器监视的详细信息。这将输出具有相关监视（路径）的会话（连接）列表。请注意，根据手表的数量，此操作可能会很昂贵（即影响服务器性能），请小心使用。
12.dirs：3.5.1中的新增功能：以字节为单位显示快照和日志文件的总大小
13.wchp：3.3.0中的新增功能：按路径列出有关服务器监视的详细信息。这将输出具有关联会话的路径（znode）列表。请注意，根据手表的数量，此操作可能会很昂贵（即影响服务器性能），请小心使用。
14.mntr：3.4.0中的新增功能：输出可用于监视集群运行状况的变量列表。
```



### 附录三：客户端比较

##### 1. Zookeeper原生客户端

> Zookeeper客户端提供了基本的操作，比如，创建会话、创建节点、读取节点、更新数据、删除节点和检查节点是否存在等。
>
> 缺点：
>
> - （1）Watcher注册是一次性的，每次触发之后都需要重新进行注册；
> - （2）Session超时之后没有实现重连机制；
> - （3）异常处理繁琐，Zookeeper提供了很多异常，对于开发人员来说可能根本不知道该如何处理这些异常信息；
> - （4）只提供了简单的byte[]数组的接口，没有提供针对对象级别的序列化；
> - （5）创建节点时如果节点存在抛出异常，需要自行检查节点是否存在；
> - （6）删除节点无法实现级联删除；

##### 2. ZkClient

> ZkClient是一个开源客户端，在Zookeeper原生API接口的基础上进行了包装，更便于开发人员使用
>
> 解决的问题：
>
> - 1）session会话超时重连
> - 2）解决Watcher反复注册
> - 3）简化API开发
>
> 缺点：
>
> - 几乎没有参考文档；
> - 异常处理简化（抛出RuntimeException）；
> - 重试机制比较难用；
> - 没有提供各种使用场景的实现；

##### 3. Apache Curator

> Curator是Netflix公司开源的一套Zookeeper客户端框架，和ZkClient一样，解决了非常底层的细节开发工作，包括连接重连、反复注册Watcher和NodeExistsException异常等。目前已经成为 Apache 的**顶级项目**。
>
> 特点：
>
> 1. Apache 的开源项目
> 2. 解决Watch注册一次就会失效的问题
> 3. 提供一套Fluent风格的 API 更加简单易用
> 4. 提供更多解决方案并且实现简单，例如：分布式锁
> 5. 提供常用的ZooKeeper工具类
> 6. 编程风格更舒服
> 7. 提供了Zookeeper各种应用场景（Recipe，如共享锁服务、Master选举机制和分布式计算器等）的抽象封装























