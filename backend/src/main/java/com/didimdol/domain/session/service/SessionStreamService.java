package com.didimdol.domain.session.service;

import com.didimdol.domain.message.service.ClientReplyService;
import com.didimdol.domain.session.entity.CounselSession;
import com.didimdol.domain.session.repository.CounselSessionRepository;
import com.didimdol.global.exception.BusinessException;
import com.didimdol.global.exception.ErrorCode;
import com.didimdol.global.sse.SseStreamRegistry;
import com.didimdol.global.sse.SseTicketStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class SessionStreamService {

    private static final long SSE_TIMEOUT_MS = 35 * 60 * 1000L; // 회기 30분 + 여유

    private final SseTicketStore sseTicketStore;
    private final CounselSessionRepository sessionRepository;
    private final SseStreamRegistry registry;
    private final ClientReplyService clientReplyService;

    @Transactional(readOnly = true)
    public SseEmitter connect(Long sessionId, String ticket, Long lastEventId) {
        if (ticket == null || ticket.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        var info = sseTicketStore.verify(ticket)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        CounselSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUNSEL_NOT_FOUND));

        boolean sameSession = info.sessionId().equals(sessionId);
        boolean owner = session.getCounsel().getMember().getId().equals(info.memberId());
        if (!sameSession || !owner) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        registry.connect(sessionId, emitter, lastEventId);
        clientReplyService.triggerIfPending(sessionId);
        return emitter;
    }
}
