package com.xiaoyi.monitor;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * Created on 2021/1/23.
 *
 * @author 小逸
 * @description
 */
@Component
public class MyZkClientEventSubscriber implements DisposableBean,Runnable {

    private String path;
    private String server;
    private Thread thread;

    public MyZkClientEventSubscriber(){
        this.path = "/cfg";
        this.server = "101.133.167.247:2181,101.133.167.247:2182,101.133.167.247:2183";
        this.thread = new Thread(this,"my_zk_client_holder_thread");
        this.thread.start();
    }



    @Override
    public void run() {
        MyZkClientHolder.getInstance().setUp(this.path, this.server);
    }

    @Override
    public void destroy() throws Exception {
        MyZkClientHolder.getInstance().shutDown();
    }
}
