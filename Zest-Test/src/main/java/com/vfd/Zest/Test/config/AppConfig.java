package com.vfd.Zest.Test.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate(){
        /*return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(300))
                .setReadTimeout(Duration.ofSeconds(300))
                .build();*/
        return new RestTemplate();
    }
}
