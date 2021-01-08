package com.xiaoyi.redis.support.cache.config;

/**
 * Created on 2021/1/6.
 *
 * @author 小逸
 * @description
 */
public class CacheProps {
    private CacheProps.CaffeineProps caffeine;

    public CacheProps() {
    }

    public CacheProps.CaffeineProps getCaffeine() {
        return this.caffeine;
    }

    public void setCaffeine(CacheProps.CaffeineProps caffeine) {
        this.caffeine = caffeine;
    }

    public static class CaffeineProps {
        private Integer expireAfterWrite;

        public CaffeineProps() {
        }

        public Integer getExpireAfterWrite() {
            return this.expireAfterWrite;
        }

        public void setExpireAfterWrite(Integer expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
        }
    }
}
