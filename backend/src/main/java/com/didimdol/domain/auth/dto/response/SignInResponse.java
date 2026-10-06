package com.didimdol.domain.auth.dto.response;

import java.time.LocalDateTime;

public record SignInResponse(
        Long memberId,
        String accessToken,
        String refreshToken,
        long expiresIn,
        LocalDateTime expiresAt
) {
    public static SignInResponse of(Long memberId, TokenResponse tokens) {
        return new SignInResponse(memberId, tokens.accessToken(), tokens.refreshToken(),
                tokens.expiresIn(), tokens.expiresAt());
    }
}