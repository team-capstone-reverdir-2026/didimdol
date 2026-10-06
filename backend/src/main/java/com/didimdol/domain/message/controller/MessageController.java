package com.didimdol.domain.message.controller;

import com.didimdol.domain.message.dto.request.MessageCreateRequest;
import com.didimdol.domain.message.service.MessageService;
import com.didimdol.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/{sessionId}/messages")
    public ResponseEntity<ApiResponse<Void>> create(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long sessionId,
            @Valid @RequestBody MessageCreateRequest request) {
        messageService.createMessage(memberId, sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("메시지 생성 성공"));
    }
}