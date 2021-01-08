package com.xiaoyi.redis.support;

/**
 * Created on 2021/1/6.
 *
 * @author 小逸
 * @description
 */

import java.util.function.Supplier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache.ValueWrapper;

public final class CacheUtil {
    public static final String CONNECTOR = "_";
    private static CacheManager cacheManager;

    private CacheUtil() {
    }

    public static void setCacheManager(CacheManager tempCacheManager) {
        cacheManager = tempCacheManager;
    }

    public static <T> T getCache(String cacheName, String key, Supplier<T> supplier) {
        T t = getCache(cacheName, key);
        if (t != null) {
            return t;
        } else if (supplier != null) {
            t = supplier.get();
            if (t == null) {
                return null;
            } else {
                putCache(cacheName, key, t);
                return t;
            }
        } else {
            return null;
        }
    }

    public static <T> T getCache(String cacheName, String key) {
        ValueWrapper valueWrapper = cacheManager.getCache(cacheName).get(key);
        return valueWrapper != null ? (T) valueWrapper.get() : null;
    }

    public static <T> void putCache(String cacheName, String key, T value) {
        cacheManager.getCache(cacheName).put(key, value);
    }

    public static void clearCache(String cacheName) {
        cacheManager.getCache(cacheName).clear();
    }

    public static void evictCache(String cacheName, String key) {
        cacheManager.getCache(cacheName).evict(key);
    }

    public static boolean existCache(String cacheName, String key) {
        return cacheManager.getCache(cacheName).get(key) != null;
    }
}
