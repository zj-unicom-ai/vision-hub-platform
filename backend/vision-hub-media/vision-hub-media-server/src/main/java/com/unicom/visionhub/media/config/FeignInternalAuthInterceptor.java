package com.unicom.visionhub.media.config;

import com.unicom.visionhub.common.config.InternalSecretProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class FeignInternalAuthInterceptor implements RequestInterceptor {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    private final InternalSecretProperties internalSecretProperties;

    @Override
    public void apply(RequestTemplate template) {
        String secret = internalSecretProperties.getSecret();
        if (secret != null && !secret.isBlank()) {
            template.header(INTERNAL_SECRET_HEADER, secret);
        }
    }
}