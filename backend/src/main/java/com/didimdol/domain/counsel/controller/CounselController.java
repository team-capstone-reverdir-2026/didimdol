package com.didimdol.domain.counsel.controller;

import com.didimdol.domain.counsel.dto.request.CounselCreateRequest;
import com.didimdol.domain.counsel.dto.request.NextSessionCreateRequest;
import com.didimdol.domain.counsel.dto.response.CounselCreateResponse;
import com.didimdol.domain.counsel.service.CounselService;
import com.didimdol.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/counsels")
public class CounselController {

    private final CounselService counselService;

    @PostMapping
    public ResponseEntity<ApiResponse<CounselCreateResponse>> createCounsel(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CounselCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("상담 생성 성공",
                        counselService.createCounsel(memberId, request)));
    }

    @PostMapping("/{counselId}/sessions")
    public ResponseEntity<ApiResponse<CounselCreateResponse>> createNextSession(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long counselId,
            @Valid @RequestBody NextSessionCreateRequest request) {
        CounselCreateResponse response = counselService.createNextSession(memberId, counselId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("다음 회기 생성 성공", response));
    }

    @DeleteMapping("/{counselId}")
    public ResponseEntity<ApiResponse<Void>> deleteCounsel(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long counselId) {
        counselService.deleteCounsel(memberId, counselId);
        return ResponseEntity.ok(ApiResponse.success("상담 삭제 성공"));
    }
}
