package com.didimdol.domain.counsel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record NextSessionCreateRequest(
        @NotBlank @Size(max = 500) String initialMessage,
        @NotNull LocalDateTime startAt
) {}
