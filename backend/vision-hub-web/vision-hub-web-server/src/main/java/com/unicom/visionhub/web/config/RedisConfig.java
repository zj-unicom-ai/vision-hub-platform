package com.unicom.visionhub.web.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Value("${spring.data.redis.timeout:5000}")
    private int timeout;


    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        log.info("【Redis】初始化 Redisson 客户端，host: {}, port: {}, database: {}", host, port, database);

        String address = "redis://" + host + ":" + port;

        Config config = new Config();
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                .setPassword(password.isEmpty() ? null : password)
                .setTimeout(timeout)
                .setConnectionPoolSize(8)
                .setConnectionMinimumIdleSize(0)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(timeout)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        return Redisson.create(config);
    }
}
