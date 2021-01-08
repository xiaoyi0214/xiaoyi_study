package com.xiaoyi.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

/**
 * Created on 2021/1/6.
 *
 * @author 小逸
 * @description
 */

//@Configuration
public class RedissonConfig {

    @Autowired
    private RedisProperties redisProperties;


    @Bean
    public RedissonClient redissonClient() {

        Logger logger = LoggerFactory.getLogger(RedissonConfig.class);
        RedissonClient redissonClient;

        Config config = new Config();
        String url = "redis://" + redisProperties.getHost() + ":" + redisProperties.getPort();
        logger.info("Redis host:{} port:{} pwd:{} database:{} ",url,redisProperties.getPort(),redisProperties.getPassword(),redisProperties.getDatabase());
        config.useSingleServer().setAddress(url)
                .setPassword(redisProperties.getPassword())
                .setDatabase(redisProperties.getDatabase());
        Codec codec = new JsonJacksonCodec();
        config.setCodec(codec);

        try {
            redissonClient = Redisson.create(config);
            return redissonClient;
        } catch (Exception e) {
            logger.error("RedissonClient init redis url:[{}], Exception:", url, e);
            return null;
        }
    }
}


