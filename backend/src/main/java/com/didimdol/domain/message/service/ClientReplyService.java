package com.didimdol.domain.message.service;

import com.didimdol.domain.message.ai.ClientReplyProvider;
import com.didimdol.domain.message.ai.ReplyTagParser;
import com.didimdol.domain.message.dto.ClientMessagePayload;
import com.didimdol.domain.message.dto.MessageErrorPayload;
import com.didimdol.domain.message.event.CounselorMessageCreatedEvent;
import com.didimdol.global.sse.SseStreamRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientReplyService {

    private static final String ERROR_MESSAGE = "메시지 생성에 실패했습니다. 다시 시도해주세요.";

    private final MessageRecorder recorder;
    private final SseStreamRegistry registry;
    private final ClientReplyProvider provider;
    private final ExecutorService replyExecutor;
    private final Set<Long> generating = ConcurrentHashMap.newKeySet();

    public boolean isGenerating(Long sessionId) {
        return generating.contains(sessionId);
    }

    /** 상담자 메시지가 "커밋된 뒤"에 호출됨 (커밋 전이면 비동기 스레드가 새 메시지를 못 본다) */
    @TransactionalEventListener
    public void on(CounselorMessageCreatedEvent event) {
        triggerIfPending(event.sessionId());
    }

    /** 스트림이 연결돼 있고, 답 없는 상담자 메시지가 있고, 생성 중이 아니면 시작 */
    public void triggerIfPending(Long sessionId) {
        if (!registry.isConnected(sessionId)) return;
        if (!recorder.isReplyPending(sessionId)) return;
        if (!generating.add(sessionId)) return;

        replyExecutor.submit(() -> {
            try {
                generate(sessionId);
            } finally {
                generating.remove(sessionId);
            }
        });
    }

    private void generate(Long sessionId) {
        Long messageId = recorder.startClientMessage(sessionId);
        ReplyTagParser parser = new ReplyTagParser();
        StringBuilder content = new StringBuilder();

        try {
            provider.stream(sessionId, token -> {
                String text = parser.feed(token);
                relay(sessionId, messageId, parser, content, text);
            });
            relay(sessionId, messageId, parser, content, parser.finish());

            if (content.toString().isBlank()) {
                throw new IllegalStateException("빈 응답");
            }
            recorder.completeClientMessage(messageId, parser.emotion(), parser.cue(), content.toString());
            registry.publish(sessionId, "message-done",
                    new ClientMessagePayload(messageId, parser.emotion(), content.toString()));
        } catch (Exception e) {
            log.error("내담자 응답 생성 실패 sessionId={}", sessionId, e);
            recorder.discard(messageId);
            registry.publish(sessionId, "message-error", new MessageErrorPayload(messageId, ERROR_MESSAGE));
        }
    }

    private void relay(Long sessionId, Long messageId, ReplyTagParser parser,
                       StringBuilder content, String text) {
        if (text.isEmpty()) return;
        content.append(text);
        registry.publish(sessionId, "message-delta",
                new ClientMessagePayload(messageId, parser.emotion(), text));
    }
}
