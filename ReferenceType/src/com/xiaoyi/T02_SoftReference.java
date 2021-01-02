package com.xiaoyi;

import java.lang.ref.SoftReference;

/**
 * Created on 2021/1/3.
 *
 * @author 小逸
 * @description 软引用 -- 当空间不足时会被垃圾回收器回收，适合应用于缓存
 *
 * 启动参数： -Xmx20M
 *
 */
public class T02_SoftReference {
    public static void main(String[] args) {
        SoftReference<byte[]> s = new SoftReference<>(new byte[1024*1024*10]);
        System.out.println(s.get());
        // 第一次没有回收
        System.gc();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.println(s.get());
        // 再分配一个数组，heap中将装不下，此时垃圾回收器将回收一次，如果空间不足，会把软引用回收
        byte[] b = new byte[1024*1024*10];
        System.out.println(s.get());
    }
}
