package com.didimdol.domain.message.service;

import com.didimdol.domain.message.dto.request.MessageCreateRequest;
import com.didimdol.domain.message.entity.Message;
import com.didimdol.domain.message.enums.Speaker;
import com.didimdol.domain.message.event.CounselorMessageCreatedEvent;
import com.didimdol.domain.message.repository.MessageRepository;
import com.didimdol.domain.session.entity.CounselSession;
import com.didimdol.domain.session.enums.SessionStatus;
import com.didimdol.domain.session.repository.CounselSessionRepository;
import com.didimdol.domain.session.service.SessionTimeLimit;
import com.didimdol.global.exception.BusinessException;
import com.didimdol.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final CounselSessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final ClientReplyService clientReplyService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createMessage(Long memberId, Long sessionId, MessageCreateRequest request) {
        CounselSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUNSEL_NOT_FOUND));

        if (!session.getCounsel().getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.SESSION_NOT_IN_PROGRESS);
        }
        if (SessionTimeLimit.isExceeded(session)) {
            throw new BusinessException(ErrorCode.SESSION_TIME_EXCEEDED);
        }
        if (clientReplyService.isGenerating(sessionId)) {
            throw new BusinessException(ErrorCode.CLIENT_RESPONDING);
        }

        int nextSeq = messageRepository.findTopBySessionIdOrderBySeqDesc(sessionId)
                .map(m -> m.getSeq() + 1).orElse(1);

        messageRepository.save(Message.builder()
                .session(session)
                .seq(nextSeq)
                .speaker(Speaker.COUNSELOR)
                .content(request.message())
                .spokenAt(MessageRecorder.elapsedSeconds(session))
                .build());

        eventPublisher.publishEvent(new CounselorMessageCreatedEvent(sessionId));
    }
}