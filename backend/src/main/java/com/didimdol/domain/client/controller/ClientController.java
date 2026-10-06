package com.didimdol.domain.client.controller;

import com.didimdol.domain.client.dto.response.ClientDetailResponse;
import com.didimdol.domain.client.dto.response.ClientListResponse;
import com.didimdol.domain.client.service.ClientService;
import com.didimdol.global.response.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<ApiResponse<ClientListResponse>> getClients(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) @Min(0) Long idAfter,
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "내담자 목록 조회 성공", clientService.getClients(memberId, idAfter, limit)));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ApiResponse<ClientDetailResponse>> getClient(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long clientId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "내담자 상세 조회 성공", clientService.getClient(memberId, clientId)));
    }
}