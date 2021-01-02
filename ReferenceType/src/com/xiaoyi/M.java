package com.xiaoyi;

/**
 * Created on 2021/1/3.
 *
 * @author 小逸
 * @description 观察回收
 */
public class M {
    /**
    * finalize() 在开发中会出现oom问题，谨慎
    */
    @Override
    protected void finalize() throws Throwable {
        System.out.println("finalize start ......");
    }
}
