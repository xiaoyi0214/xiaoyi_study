package com.xiaoyi;

import java.lang.ref.WeakReference;

/**
 * Created on 2021/1/3.
 *
 * @author 小逸
 * @description 弱引用 --- 只要发生垃圾回收，弱引用就会被回收
 */
public class T03_WeakReference {
    public static void main(String[] args) {
        WeakReference<M> m = new WeakReference<>(new M());
        System.out.println(m.get());
        System.gc();
        System.out.println(m.get());


        // 应用
        ThreadLocal<M> threadLocal = new ThreadLocal<>();
        threadLocal.set(new M());
        threadLocal.remove();



    }

}
