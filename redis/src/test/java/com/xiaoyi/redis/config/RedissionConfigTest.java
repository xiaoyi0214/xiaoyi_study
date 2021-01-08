package com.xiaoyi.redis.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Map;

/**
 * Created on 2021/1/6.
 *
 * @author 小逸
 * @description
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedissionConfigTest {

    @Resource
    private RedissonConfig redissonConfig;
    @Test
    public void getRedisClientTest() {
        RedissonClient redisClient = redissonConfig.redissonClient();
        RMap<Object, Object> map = redisClient.getMap("map");
        map.put("a", 1);
        map.put("b", 2);

        System.out.println(redisClient.toString());

        while (true){
            Map<Object, Object> allMap = redisClient.getMap("map").readAllMap();
            allMap.forEach((key,value)->{
                System.out.println(key +" "+value);
            });
        }
    }



}
