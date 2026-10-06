package com.didimdol.domain.session.controller;

import com.didimdol.domain.session.service.SessionStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionStreamController {

    private final SessionStreamService streamService;

    @GetMapping("/{sessionId}/stream")
    public SseEmitter stream(@PathVariable Long sessionId,
                             @RequestParam(required = false) String ticket,
                             @RequestHeader(value = "Last-Event-ID", required = false) Long lastEventId) {
        return streamService.connect(sessionId, ticket, lastEventId);
    }
}