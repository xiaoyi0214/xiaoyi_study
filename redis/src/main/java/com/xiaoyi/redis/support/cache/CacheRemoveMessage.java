package com.xiaoyi.redis.support.cache;

/**
 * Created on 2021/1/6.
 *
 * @author 小逸
 * @description
 */
import java.io.Serializable;

public class CacheRemoveMessage implements Serializable {
    private String cacheName;
    private Object key;

    public CacheRemoveMessage(String cacheName, Object key) {
        this.cacheName = cacheName;
        this.key = key;
    }

    public String getCacheName() {
        return this.cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public Object getKey() {
        return this.key;
    }

    public void setKey(Object key) {
        this.key = key;
    }
}
