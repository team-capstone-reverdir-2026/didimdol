package com.didimdol.global.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "anthropic")
public record AnthropicProperties(
        String apiKey,
        @NotBlank String baseUrl,
        @NotBlank String version,
        @NotBlank String model,
        @Positive int maxTokens
) {}