package com.xiaoyi.redis.support.redission;

import java.io.File;
import java.io.IOException;
import org.redisson.Redisson;
import org.redisson.RedissonNode;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SerializationCodec;
import org.redisson.config.Config;
import org.redisson.config.RedissonNodeConfig;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableCaching
public class RedissonConfig {
    private static final Logger logger = LoggerFactory.getLogger(RedissonConfig.class);

    @Autowired
    Environment env;

    public RedissonConfig() {
    }

    private String getRedisHost() {
        return this.env.getProperty("spring.redis.host", "127.0.0.1");
    }

    private String getRedisPort() {
        return this.env.getProperty("spring.redis.port", "6379");
    }

    private String getRedisPassword() {
        return this.env.getProperty("spring.redis.password");
    }

    private Integer getRedisDb() {
        Integer database = (Integer)this.env.getProperty("spring.redis.database", Integer.class);
        return database != null ? database : (Integer)this.env.getProperty("spring.redis.db", Integer.class, 0);
    }

    private String getRedisAddress() {
        return "redis://" + this.getRedisHost() + ":" + this.getRedisPort();
    }

    private void setConfigByEnv(Config config) {
        SingleServerConfig singleServerConfig = config.useSingleServer().setAddress(this.getRedisAddress());
        if (this.getRedisPassword() != null) {
            singleServerConfig.setPassword(this.getRedisPassword());
        }

        singleServerConfig.setDatabase(this.getRedisDb()).setConnectionMinimumIdleSize(64).setConnectionPoolSize(500).setSubscriptionConnectionMinimumIdleSize(20).setSubscriptionConnectionPoolSize(50);
    }

    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson() throws IOException {
        logger.info("redission create by user config");
        Config config = new Config();
        File configFile = new File("./config/redisson.json");
        if (configFile.exists() && configFile.canRead()) {
            config = (Config)CustomConfig.fromJSON(configFile, Config.class);
        } else {
            this.setConfigByEnv(config);
        }

        config.setLockWatchdogTimeout(600000L);
        config.setCodec(new SerializationCodec());
        RedissonClient client = Redisson.create(config);
        return client;
    }

    @Bean(destroyMethod = "shutdown")
    @Conditional({RedissonNodeCondition.class})
    RedissonNode redissonNode(RedissonClient redisson) {
        RedissonNodeConfig redissonNodeConfig = new RedissonNodeConfig(redisson.getConfig());
        redissonNodeConfig.setMapReduceWorkers(-1);
        RedissonNode node = RedissonNode.create(redissonNodeConfig, redisson);
        return node;
    }
}
