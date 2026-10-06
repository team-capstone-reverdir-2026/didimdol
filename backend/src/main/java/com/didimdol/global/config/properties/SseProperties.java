package com.didimdol.global.config.properties;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "sse")
public record SseProperties(
        @Positive long ticketTtlSeconds
) {}