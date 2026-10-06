package com.didimdol.domain.message.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageCreateRequest(@NotBlank @Size(max = 500) String message) {}
