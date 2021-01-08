package com.xiaoyi.redis.support.redission;

/**
 * Created on 2021/1/6.
 *
 * @author 小逸
 * @description 启动时创建RedissionNode
 */
import org.redisson.RedissonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class RedissonNodeLauncher implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(RedissonNodeLauncher.class);

    public RedissonNodeLauncher() {
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        if (applicationContext.containsBean("redissonNode")) {
            logger.info("Starting redisson node");
            //TenantManager tenantManager = (TenantManager)applicationContext.getBean(TenantManager.class);
            RedissonNode redissonNode = (RedissonNode)applicationContext.getBean(RedissonNode.class);
            redissonNode.start();
        }

    }
}
