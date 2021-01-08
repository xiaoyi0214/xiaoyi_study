package com.xiaoyi.redis.support;

import com.xiaoyi.redis.support.redission.RedissonConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * Created on 2021/1/6.
 *
 * @author 小逸
 * @description
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedissionTest {

    @Resource
    private RedissonConfig redissonConfig;




}
