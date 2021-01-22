package com.xiaoyi.monitor;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.awt.SunHints;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created on 2021/1/23.
 *
 * 1.提供外围接口来获取最新的子节点信息
 * 2.需要监听path下各个子节点个数的变化(增加、删除)需要监听path下各个子节点个数的变化(增加、删除)
 * 3.需要监听各个子节点数据的变化
 * 4.zkClient的启动线程需要在在后台运行.
 * @author 小逸
 * @description
 */
@Component
public class MyZkClientHolder {
    private Logger logger = LoggerFactory.getLogger(MyZkClientHolder.class);

    private MyZkClientHolder() {
    }

    private volatile static MyZkClientHolder myZkClientHolder;

    public static MyZkClientHolder getInstance(){
        if (myZkClientHolder != null){
            return myZkClientHolder;
        }else {
            synchronized (MyZkClientHolder.class){
                if (myZkClientHolder == null){
                    myZkClientHolder = new MyZkClientHolder();

                }
                return myZkClientHolder;
            }
        }
    }

    private CountDownLatch shutdownLatch = new CountDownLatch(1);

    private volatile boolean alreadySetUpFlag = false;
    private ZkClient zkClient;

    private final static HashMap<String,Object> _cache = new HashMap<>();

    public void setUp(String path,String server){
        if (alreadySetUpFlag){
            return;
        }
        zkClient = new ZkClient(server,300000);
        zkClient.setZkSerializer(new MyZkSerializer());
        List<String> children = zkClient.getChildren(path);
        // 初始化缓存
        fillCacheMap(path, children);

        //监听 ----- 子节点变化  == 下面一种写法
        zkClient.subscribeChildChanges(path, (String parentPath, List<String> currentChilds) -> {
            //子节点发生变化: 重新填充缓存
            logger.info("The parentPath {} has children {}",parentPath,currentChilds);
            fillCacheMap(parentPath, currentChilds);

        });
        /*zkClient.subscribeChildChanges(path, new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {

            }
        });*/
        logger.info("MyZkClientHolder started ...");
        alreadySetUpFlag = true;

        try {
            shutdownLatch.await();//阻塞----后台一直运行


        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            //运行到此处，说明已经关闭
            alreadySetUpFlag = false;
        }
    }


    private void fillCacheMap(String parentPath, List<String> children){
        logger.info("~~~~~~~~~~~~fillCacheMap~~~~~~~~~~~~~~~~");
        if (!_cache.isEmpty()){
            _cache.clear();
        }
        children.stream().forEach(child->{
            String fullPath = parentPath + "/" + child;
            System.out.println("`````````````````fullPath:"+fullPath);

            Object value = zkClient.readData(fullPath);
            System.out.println("`````````````````value:"+value);

            _cache.put(fullPath, value);

            zkClient.subscribeDataChanges(fullPath, new IZkDataListener() {
                @Override
                public void handleDataChange(String dataPath, Object data) throws Exception {
                    logger.info("********** The dataPath:【{}】 has been change to 【{}】",dataPath,data);
                    _cache.put(dataPath, data);
                }

                @Override
                public void handleDataDeleted(String dataPath) throws Exception {
                    logger.info("********** The dataPath:【{}】 has been delete",dataPath);
                    _cache.remove(dataPath);
                }
            });
        });

        // todo sendto ...
    }

    /**
     * 供外部调用接口，获取缓存信息
     * */
    public HashMap getCache(){
        return _cache;
    }
    /**
     * 关闭
    * */
    public void shutDown(){
        shutdownLatch.countDown();
    }

    public static void main(String[] args) throws InterruptedException {
        String path = "/cfg";
        String ZKServers = "101.133.167.247:2181,101.133.167.247:2182,101.133.167.247:2183";
        new Thread(()->{
            MyZkClientHolder.getInstance().setUp(path, ZKServers);
        }).start();

        for (int i = 0; i < 1000; i++) {
            System.out.println(MyZkClientHolder.getInstance().getCache());
            Thread.sleep(3000);
        }
    }
}
