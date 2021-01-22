package com.xiaoyi.controller;

import com.xiaoyi.monitor.MyZkClientEventSubscriber;
import com.xiaoyi.monitor.MyZkClientHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

/**
 * Created on 2021/1/23.
 *
 * @author 小逸
 * @description
 */
@Controller
@RequestMapping("/item")
public class ZookeeperCacheController {

    @Autowired
    private MyZkClientHolder zkClientHolder;

    static {
        MyZkClientEventSubscriber myZkClientEventSubscriber1 = new MyZkClientEventSubscriber();
    }

    @RequestMapping(value = "/getCache",method = {RequestMethod.GET})
    @ResponseBody
    public String getCache(){
        HashMap cache = zkClientHolder.getCache();
        System.out.println(cache);
        return cache.toString();
    }
}
