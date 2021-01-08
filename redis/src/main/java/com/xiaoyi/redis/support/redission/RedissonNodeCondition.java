package com.xiaoyi.redis.support.redission;

import com.google.common.base.Strings;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Created on 2021/1/6.
 *
 * @author 小逸
 * @description
 */


public class RedissonNodeCondition implements Condition {
    public RedissonNodeCondition() {
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        String asRedissonNode = context.getEnvironment().getProperty("xiaoyi.as-redisson-node");
        if (Strings.isNullOrEmpty(asRedissonNode)) {
            asRedissonNode = "true";
        }

        return asRedissonNode.toLowerCase().equals("true");
    }
}