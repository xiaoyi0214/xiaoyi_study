package com.xiaoyi.redis.support;

/**
 * Created on 2021/1/6.
 *
 * @author 小逸
 * @description
 */
import org.springframework.context.ApplicationContext;

public abstract class ApplicationContextHelper {
    private static ApplicationContext ctx;
    public static boolean enabled = false;

    public ApplicationContextHelper() {
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return ctx;
    }

    public static ApplicationContext getEnabledApplicationContext() {
        return enabled ? ctx : null;
    }
}
