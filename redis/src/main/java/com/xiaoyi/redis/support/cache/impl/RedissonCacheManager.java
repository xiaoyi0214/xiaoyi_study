package com.xiaoyi.redis.support.cache.impl;

/**
 * Created on 2021/1/6.
 *
 * @author 小逸
 * @description
 */
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public class RedissonCacheManager implements CacheManager {
    public final RedissonClient redisson;
    private final ConcurrentMap<String, Cache> instanceMap = new ConcurrentHashMap();
    private final Set<String> cacheNames = Sets.newConcurrentHashSet();

    public RedissonCacheManager(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public Cache getCache(String cacheName) {
        Cache cache = (Cache)this.instanceMap.get(cacheName);
        return cache != null ? cache : this.createMap(cacheName);
    }

    private Cache createMap(String name) {
        this.cacheNames.add(name);
        RMap<Object, Object> map = this.redisson.getMap(name);
        Cache cache = new RedissonBaseCache(map);
        Cache oldCache = (Cache)this.instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        }

        return (Cache)cache;
    }

    @Override
    public Collection<String> getCacheNames() {
        return this.cacheNames;
    }
}