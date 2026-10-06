package com.didimdol.domain.session.controller;

import com.didimdol.domain.session.dto.request.SessionCompleteRequest;
import com.didimdol.domain.session.dto.response.SessionCompleteResponse;
import com.didimdol.domain.session.dto.response.SessionDetailResponse;
import com.didimdol.domain.session.dto.response.SessionListResponse;
import com.didimdol.domain.session.dto.response.SessionMemoResponse;
import com.didimdol.domain.session.service.SessionService;
import com.didimdol.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<ApiResponse<SessionCompleteResponse>> complete(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionCompleteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("상담 세션 종료 성공",
                        sessionService.complete(memberId, sessionId, request)));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<SessionDetailResponse>> detail(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success("상담 상세 조회 성공",
                sessionService.getDetail(memberId, sessionId)));
    }

    @GetMapping("/{sessionId}/memo")
    public ResponseEntity<ApiResponse<SessionMemoResponse>> memo(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success("상담 메모 조회 성공",
                sessionService.getMemo(memberId, sessionId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SessionListResponse>> list(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) @Min(1) Long idAfter,
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int limit) {
        return ResponseEntity.ok(ApiResponse.success("상담 목록 조회 성공",
                sessionService.getSessions(memberId, idAfter, limit)));
    }
}