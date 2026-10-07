package com.didimdol.domain.session.event;

import com.didimdol.domain.session.service.SessionSummaryService;
import com.didimdol.global.sse.SessionClosedPayload;
import com.didimdol.global.sse.SseStreamRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SessionEventListener {

    private final SseStreamRegistry registry;
    private final SessionSummaryService summaryService;

    @TransactionalEventListener
    public void onSessionCompleted(SessionCompletedEvent event) {
        String reason = event.counselCompleted() ? "COUNSEL_COMPLETED" : "SESSION_COMPLETED";
        registry.close(event.sessionId(), new SessionClosedPayload(reason));
        summaryService.summarizeAsync(event.sessionId());
    }
}