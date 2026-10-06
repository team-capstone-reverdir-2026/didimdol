package com.didimdol.domain.auth.controller;

import com.didimdol.domain.auth.dto.request.RefreshRequest;
import com.didimdol.domain.auth.dto.request.SignInRequest;
import com.didimdol.domain.auth.dto.request.SignUpRequest;
import com.didimdol.domain.auth.dto.response.SignInResponse;
import com.didimdol.domain.auth.dto.response.TokenResponse;
import com.didimdol.domain.auth.service.AuthService;
import com.didimdol.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입 성공"));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<SignInResponse>> signIn(@Valid @RequestBody SignInRequest request) {
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", authService.signIn(request)));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<ApiResponse<Void>> signOut(@AuthenticationPrincipal Long memberId) {
        authService.signOut(memberId);
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공", authService.refresh(request)));
    }
}