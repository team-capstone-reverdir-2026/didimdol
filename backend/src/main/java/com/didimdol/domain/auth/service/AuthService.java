package com.didimdol.domain.auth.service;

import com.didimdol.domain.auth.dto.request.RefreshRequest;
import com.didimdol.domain.auth.dto.request.SignInRequest;
import com.didimdol.domain.auth.dto.request.SignUpRequest;
import com.didimdol.domain.auth.dto.response.SignInResponse;
import com.didimdol.domain.auth.dto.response.TokenResponse;
import com.didimdol.domain.auth.entity.RefreshToken;
import com.didimdol.domain.auth.repository.RefreshTokenRepository;
import com.didimdol.domain.member.entity.Member;
import com.didimdol.domain.member.repository.MemberRepository;
import com.didimdol.global.config.properties.JwtProperties;
import com.didimdol.global.exception.BusinessException;
import com.didimdol.global.exception.ErrorCode;
import com.didimdol.global.security.JwtProvider;
import com.didimdol.global.security.TokenType;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void signUp(SignUpRequest request) {
        if (memberRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }
        memberRepository.save(
                Member.builder()
                        .username(request.username())
                        .encodedPassword(passwordEncoder.encode(request.password()))
                        .nickname(request.nickname())
                        .build()
        );
    }

    @Transactional
    public SignInResponse signIn(SignInRequest request) {
        Member member = memberRepository.findByUsername(request.username())
                .filter(m -> passwordEncoder.matches(request.password(), m.getPassword()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        return SignInResponse.of(member.getId(), issueTokens(member));
    }

    private TokenResponse issueTokens(Member member) {
        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime refreshExpiresAt = now.plusDays(jwtProperties.refreshTokenExpirationDays());

        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        saved -> saved.rotate(refreshToken, refreshExpiresAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.create(member, refreshToken, refreshExpiresAt))
                );

        long expiresIn = jwtProperties.accessTokenExpirationSeconds();
        return new TokenResponse(
                accessToken,
                refreshToken,
                expiresIn,
                now.plusSeconds(expiresIn).truncatedTo(ChronoUnit.SECONDS)
        );
    }

    @Transactional
    public void signOut(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();

        try {
            jwtProvider.parseMemberId(token, TokenType.REFRESH);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken saved = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        return issueTokens(saved.getMember());
    }
}