package com.didimdol.global.config;

import com.didimdol.global.config.properties.AnthropicProperties;
import com.didimdol.global.config.properties.CorsProperties;
import com.didimdol.global.config.properties.JwtProperties;
import com.didimdol.global.config.properties.SseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class,
        CorsProperties.class, SseProperties.class, AnthropicProperties.class
})
public class PropertiesConfig {
}