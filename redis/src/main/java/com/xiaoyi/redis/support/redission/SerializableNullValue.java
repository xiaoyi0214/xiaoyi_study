package com.xiaoyi.redis.support.redission;

/**
 * Created on 2021/1/6.
 *
 * @author 小逸
 * @description
 */
import java.io.Serializable;
import org.springframework.cache.Cache.ValueWrapper;

public class SerializableNullValue implements ValueWrapper, Serializable {
    public static final SerializableNullValue INSTANCE = new SerializableNullValue();

    public SerializableNullValue() {
    }

    @Override
    public Object get() {
        return null;
    }
}