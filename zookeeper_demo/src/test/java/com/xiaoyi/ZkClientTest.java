package com.xiaoyi;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created on 2021/1/23.
 *
 * @author 小逸
 * @description
 */
public class ZkClientTest {
    Logger logger = LoggerFactory.getLogger(ZkClientTest.class);


    ZkClient zkClient;
    @Before
    public void init(){

        String ZKServers = "101.133.167.247:2181,101.133.167.247:2182,101.133.167.247:2183";

        /**
         * 创建会话
         * new SerializableSerializer() 创建序列化器接口，用来序列化和反序列化
         */
        zkClient = new ZkClient(ZKServers,10000000,10000000,new SerializableSerializer());

        System.out.println("conneted ok!");
    }

    /**
     * 创建节点
     * */
    @Test
    public void creatNode(){
        // 创建节点
        String s = zkClient.create("/test", "1", CreateMode.PERSISTENT);
        System.out.println(s);
    }

    /**
     * 创建权限节点
     * */
    @Test
    public void creatAclNode(){
        List<ACL> aclList = new ArrayList<>();
        // 权限位：二进制或计算
        int perm = ZooDefs.Perms.ADMIN | ZooDefs.Perms.READ|ZooDefs.Perms.WRITE;
        aclList.add(new ACL(perm, new Id("world", "anyone")));
        aclList.add(new ACL(ZooDefs.Perms.ALL, new Id("ip", "192.168.0.132")));
        // 创建权限节点
        String s1 = zkClient.create("/test/d", "1",aclList, CreateMode.PERSISTENT);
        System.out.println(s1);

        Map.Entry<List<ACL>, Stat> statEntry = zkClient.getAcl(s1);
        System.out.println(statEntry.getKey() +","+ statEntry.getValue().toString());
    }

    /**
     * 获取节点数据
     * */
    @Test
    public void getData(){
        Object data = zkClient.readData("/test");
        Object data1 = zkClient.readData("/test", new Stat());
        System.out.println(data);
        System.out.println(data1);
    }

    /**
     * 写入节点数据
     * */
    @Test
    public void setData(){
        zkClient.writeData("/test", "1234");
    }

    /**
     * 监听节点数据变化
     *      若直接在客户端命令设置会使data序列解析失败报错，所以用 writeData方法更改值
     * */
    @Test
    public void subscribeDataChangesTest(){
        zkClient.subscribeDataChanges("/test", new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                logger.info("********** The dataPath:【{}】 has been change to 【{}】",dataPath,data);
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                logger.info("********** The dataPath:【{}】 has been delete",dataPath);

            }
        });
    }
    /**
     * 监听节点下子节点变化:增减变化
     * */
    @Test
    public void subscribeChildChangesTest(){
        zkClient.subscribeChildChanges("/test", new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                logger.info("The parentPath {} has childs {}",parentPath,currentChilds);
            }
        });
    }

    @Test
    public void newThread() throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                subscribeDataChangesTest();
                subscribeChildChangesTest();
            }
        }).start();
        Thread.sleep(Long.MAX_VALUE);
    }
}
