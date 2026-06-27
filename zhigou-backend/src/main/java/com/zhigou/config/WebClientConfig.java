package com.zhigou.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private final SupabaseProperties supabase;

    public WebClientConfig(SupabaseProperties supabase) {
        this.supabase = supabase;
    }

    @Bean
    public WebClient supabaseWebClient() {
        return WebClient.builder()
                .baseUrl(supabase.getUrl() + "/rest/v1/")
                .defaultHeader("apikey", supabase.getKey())
                .defaultHeader("Authorization", "Bearer " + supabase.getKey())
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
