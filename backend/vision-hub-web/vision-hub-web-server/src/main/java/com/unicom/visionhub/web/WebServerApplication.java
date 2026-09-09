package com.unicom.visionhub.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@EnableFeignClients(basePackages = "com.unicom.visionhub.media.client")
@MapperScan("com.unicom.visionhub.web.mapper")
@ComponentScan(basePackages = {"com.unicom.visionhub.web", "com.unicom.visionhub.common"})
@SpringBootApplication
public class WebServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebServerApplication.class, args);
    }
}