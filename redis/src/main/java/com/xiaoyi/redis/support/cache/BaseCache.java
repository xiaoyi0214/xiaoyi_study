package com.xiaoyi.redis.support.cache;

/**
 * Created on 2021/1/6.
 *
 * @author 小逸
 * @description
 */
import java.util.Collection;
import org.springframework.cache.Cache;

public interface BaseCache extends Cache {
    Collection<String> getKeyNames();
}
