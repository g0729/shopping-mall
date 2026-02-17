package com.shopping.main.global.config;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnProperty(name = "payment.gateway", havingValue = "toss")
public class TossPaymentConfig {

    @Value("${toss.secret-key}")
    private String secretKey;

    @Bean
    public RestClient tossRestClient() {
        String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        return RestClient.builder()
                .baseUrl("https://api.tosspayments.com")
                .defaultHeader("Authorization", "Basic " + encoded)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
