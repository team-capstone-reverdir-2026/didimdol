package com.didimdol.domain.message.service;

import com.didimdol.domain.message.entity.Message;
import com.didimdol.domain.message.enums.Emotion;
import com.didimdol.domain.message.enums.NotableCue;
import com.didimdol.domain.message.enums.Speaker;
import com.didimdol.domain.message.repository.MessageRepository;
import com.didimdol.domain.session.entity.CounselSession;
import com.didimdol.domain.session.repository.CounselSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageRecorder {

    private final CounselSessionRepository sessionRepository;
    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public boolean isReplyPending(Long sessionId) {
        return messageRepository.findTopBySessionIdOrderBySeqDesc(sessionId)
                .map(m -> m.getSpeaker() == Speaker.COUNSELOR)
                .orElse(false);
    }

    /** 내담자 메시지 틀(빈 내용)을 먼저 만들어 messageId 를 확보한다 */
    @Transactional
    public Long startClientMessage(Long sessionId) {
        CounselSession session = sessionRepository.findById(sessionId).orElseThrow();
        int nextSeq = messageRepository.findTopBySessionIdOrderBySeqDesc(sessionId)
                .map(m -> m.getSeq() + 1).orElse(1);
        Message message = messageRepository.save(Message.builder()
                .session(session)
                .seq(nextSeq)
                .speaker(Speaker.CLIENT)
                .content("")
                .spokenAt(elapsedSeconds(session))
                .build());
        return message.getId();
    }

    @Transactional
    public void completeClientMessage(Long messageId, Emotion emotion, NotableCue cue, String content) {
        messageRepository.findById(messageId).orElseThrow()
                .completeClientReply(emotion, cue, content);
    }

    /** 생성 실패 시 틀을 지워서, 재연결 때 다시 "답 없는 상담자 메시지"로 잡히게 한다 */
    @Transactional
    public void discard(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    public static long elapsedSeconds(CounselSession session) {
        return Math.max(0, Duration.between(session.getStartAt(), LocalDateTime.now()).toSeconds());
    }
}