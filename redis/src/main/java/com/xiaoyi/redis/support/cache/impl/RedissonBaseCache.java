package com.xiaoyi.redis.support.cache.impl;

/**
 * Created on 2021/1/6.
 *
 * @author 小逸
 * @description
 */
import java.util.Collection;
import java.util.stream.Collectors;

import com.xiaoyi.redis.support.cache.BaseCache;
import com.xiaoyi.redis.support.redission.SerializableNullValue;
import org.redisson.api.RMap;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.cache.Cache.ValueWrapper;

public class RedissonBaseCache extends RedissonCache implements BaseCache {
    public RedissonBaseCache(RMap<Object, Object> map) {
        super(map, true);
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper wrapper = super.get(key);
        if (wrapper == null) {
            return null;
        } else {
            return (ValueWrapper)(wrapper.get() instanceof SerializableNullValue ? SerializableNullValue.INSTANCE : wrapper);
        }
    }

    @Override
    protected Object toStoreValue(Object userValue) {
        return userValue == null ? SerializableNullValue.INSTANCE : userValue;
    }

    @Override
    protected Object fromStoreValue(Object storeValue) {
        return storeValue instanceof SerializableNullValue ? null : storeValue;
    }

    @Override
    public Collection<String> getKeyNames() {
        return (Collection)this.getNativeCache().keySet(10000).stream().map((key) -> {
            return key.toString();
        }).collect(Collectors.toList());
    }
}
