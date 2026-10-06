package com.didimdol.domain.auth.dto.response;

import java.time.LocalDateTime;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        LocalDateTime expiresAt
) {}