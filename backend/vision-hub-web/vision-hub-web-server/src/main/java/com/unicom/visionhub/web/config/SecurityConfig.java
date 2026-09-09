package com.unicom.visionhub.web.config;

import com.unicom.visionhub.common.config.InternalSecretProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final InternalSecretProperties internalSecretProperties;

    @Value("${springdoc.api-docs.enabled:false}")
    private boolean swaggerEnabled;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/api/auth/login", "/api/auth/captcha").permitAll();
                if (swaggerEnabled) {
                    auth.requestMatchers(
                        "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                        "/doc.html", "/webjars/**"
                    ).permitAll();
                }
                auth.requestMatchers("/api/internal/**").access((authentication, object) -> {
                    String secret = object.getRequest().getHeader(INTERNAL_SECRET_HEADER);
                    boolean valid = secret != null && !secret.isBlank()
                            && secret.equals(internalSecretProperties.getSecret());
                    return new org.springframework.security.authorization.AuthorizationDecision(valid);
                });
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}