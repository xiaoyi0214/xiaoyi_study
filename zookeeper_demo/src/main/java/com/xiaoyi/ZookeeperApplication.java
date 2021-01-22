package com.xiaoyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created on 2021/1/23.
 *
 * @author 小逸
 * @description
 */
@SpringBootApplication(scanBasePackages = {"com.xiaoyi.*"})
public class ZookeeperApplication {
    public static void main( String[] args ) {
        SpringApplication.run(ZookeeperApplication.class, args);

    }

}