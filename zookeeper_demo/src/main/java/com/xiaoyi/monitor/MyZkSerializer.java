package com.xiaoyi.monitor;

import com.google.common.base.Charsets;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

/**
 * 自定义zk序列化
 *
 * @author 小逸
 * @description
 */
public class MyZkSerializer implements ZkSerializer {
    @Override
    public Object deserialize(byte[] bytes) throws ZkMarshallingError {
        return new String(bytes, Charsets.UTF_8);
    }

    @Override
    public byte[] serialize(Object obj) throws ZkMarshallingError {
        return String.valueOf(obj).getBytes(Charsets.UTF_8);
    }
}
