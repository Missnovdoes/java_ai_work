package com.zhigou.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "doubao")
public class AiConfig {
    private String apiKey;
    private String endpoint;
    private String model;
    private double temperature = 0.7;
    private int maxTokens = 300;
}
