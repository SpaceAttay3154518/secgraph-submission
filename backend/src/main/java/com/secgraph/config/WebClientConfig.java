package com.secgraph.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient reconWebClient(@Value("${recon.service.url}") String reconUrl) {
        return WebClient.builder()
                .baseUrl(reconUrl)
                .build();
    }

    @Bean
    public WebClient nvdWebClient(@Value("${nvd.api.url}") String nvdUrl) {
        return WebClient.builder()
                .baseUrl(nvdUrl)
                .build();
    }
}
