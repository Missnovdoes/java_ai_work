package com.zhigou;

import com.zhigou.config.AiConfig;
import com.zhigou.config.NegotiationProperties;
import com.zhigou.config.SupabaseProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableConfigurationProperties({AiConfig.class, SupabaseProperties.class, NegotiationProperties.class})
public class ZhigouApplication {

    private static final Logger log = LoggerFactory.getLogger(ZhigouApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ZhigouApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("========================================");
        log.info("  智购平台启动成功！");
        log.info("  访问地址: http://localhost:5000");
        log.info("========================================");
    }
}
