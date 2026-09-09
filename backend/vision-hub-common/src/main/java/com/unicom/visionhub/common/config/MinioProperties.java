package com.unicom.visionhub.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {


    private String endpoint;


    private String accessKey;


    private String secretKey;


    private String bucketName;


    private String cdn;


    private Integer connectTimeout = 10000;


    private Integer writeTimeout = 60000;


    private Integer readTimeout = 10000;


    private Integer defaultExpiry = 3600;
}
