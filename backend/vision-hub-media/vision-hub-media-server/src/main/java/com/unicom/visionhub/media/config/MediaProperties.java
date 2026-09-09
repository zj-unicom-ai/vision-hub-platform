package com.unicom.visionhub.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "vision-hub.media")
public class MediaProperties {


    private int connectTimeout = 5000;


    private int readTimeout = 10000;


    private long idleTimeout = 30000;


    private long healthCheckInterval = 10000;


    private int grabberCorePoolSize = 50;


    private int grabberMaxPoolSize = 200;


    private long frameExpireTime = 30000;


    private int frameFetchTimeout = 5000;


    private int maxRetryAttempts = 3;


    private long retryBaseInterval = 3000;


    private int retryIntervalMultiplier = 1;
}
