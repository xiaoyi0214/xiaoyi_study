package com.xiaoyi;

import java.io.IOException;

/**
 * Created on 2021/1/3.
 *
 * @author 小逸
 * @description 强引用--存在对象指向类型时就不会被垃圾回收
 */
public class T01_NormalReference {
    public static void main(String[] args) throws IOException {
        M m = new M();
        // 将引用去除
        m = null;
        System.gc();

        System.in.read();
    }

}
