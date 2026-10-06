package com.didimdol.domain.session.event;

import com.didimdol.global.sse.SessionClosedPayload;
import com.didimdol.global.sse.SseStreamRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SessionEventListener {

    private final SseStreamRegistry registry;

    @TransactionalEventListener
    public void onSessionCompleted(SessionCompletedEvent event) {
        String reason = event.counselCompleted() ? "COUNSEL_COMPLETED" : "SESSION_COMPLETED";
        registry.close(event.sessionId(), new SessionClosedPayload(reason));
        // TODO(Step 7): 여기서 Job 2(요약 추출 → PersonaMemory 저장)를 비동기로 시작
    }
}