package com.unicom.visionhub.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "vision-hub.internal")
public class InternalSecretProperties {


    private String secret;
}