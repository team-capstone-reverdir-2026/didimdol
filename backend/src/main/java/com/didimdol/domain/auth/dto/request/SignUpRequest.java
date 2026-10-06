package com.didimdol.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank @Size(max = 20) String username,
        @NotBlank @Size(max = 20) String password,
        @NotBlank @Size(max = 20) String nickname
) {
}
