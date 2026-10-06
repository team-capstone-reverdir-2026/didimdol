package com.didimdol.global.security;

import com.didimdol.global.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private static final String TYPE_CLAIM = "type";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtProvider(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long memberId) {
        return createToken(memberId, TokenType.ACCESS,
                Duration.ofSeconds(properties.accessTokenExpirationSeconds()));
    }

    public String createRefreshToken(Long memberId) {
        return createToken(memberId, TokenType.REFRESH,
                Duration.ofDays(properties.refreshTokenExpirationDays()));
    }

    public Long parseMemberId(String token, TokenType expectedType) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!expectedType.name().equals(claims.get(TYPE_CLAIM, String.class))) {
            throw new JwtException("Unexpected token type");
        }
        return Long.valueOf(claims.getSubject());
    }

    private String createToken(Long memberId, TokenType type, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(memberId))
                .claim(TYPE_CLAIM, type.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }
}