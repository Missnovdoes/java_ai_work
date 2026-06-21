package com.zhigou.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Data
@Configuration
@ConfigurationProperties(prefix = "supabase")
public class WebClientConfig {
    private String url;
    private String key;

    @Bean
    public WebClient supabaseWebClient() {
        String baseUrl = url + "/rest/v1/";
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("apikey", key)
                .defaultHeader("Authorization", "Bearer " + key)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient doubaoWebClient(AiConfig aiConfig) {
        return WebClient.builder()
                .baseUrl(aiConfig.getEndpoint())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + aiConfig.getApiKey())
                .build();
    }
}
