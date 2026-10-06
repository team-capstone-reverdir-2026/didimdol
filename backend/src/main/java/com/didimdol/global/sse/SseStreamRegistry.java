package com.didimdol.global.sse;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseStreamRegistry {

    private static final int MAX_BUFFER = 500;
    private final Map<Long, SessionStream> streams = new ConcurrentHashMap<>();

    public void connect(Long sessionId, SseEmitter emitter, Long lastEventId) {
        SessionStream stream = streams.computeIfAbsent(sessionId, id -> new SessionStream());
        stream.attach(emitter, lastEventId);
        emitter.onCompletion(() -> stream.detach(emitter));
        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> stream.detach(emitter));
    }

    public boolean isConnected(Long sessionId) {
        SessionStream stream = streams.get(sessionId);
        return stream != null && stream.isConnected();
    }

    public void publish(Long sessionId, String eventName, Object data) {
        SessionStream stream = streams.get(sessionId);
        if (stream != null) {
            stream.publish(eventName, data);
        }
    }

    /** session-closed 전송 후 연결 종료 (회기 종료 API에서 사용 예정) */
    public void close(Long sessionId, Object data) {
        SessionStream stream = streams.remove(sessionId);
        if (stream != null) {
            stream.publishAndComplete("session-closed", data);
        }
    }

    @Scheduled(fixedRate = 20_000)
    void heartbeat() {
        streams.values().forEach(SessionStream::heartbeat);
    }

    private record StreamEvent(long id, String name, Object data) {}

    private static final class SessionStream {
        private final Deque<StreamEvent> buffer = new ArrayDeque<>();
        private long lastId = 0;
        private SseEmitter emitter;

        synchronized void attach(SseEmitter next, Long lastEventId) {
            if (emitter != null) {
                emitter.complete();
            }
            emitter = next;
            sendComment("connected");
            if (lastEventId != null) {
                buffer.stream().filter(e -> e.id() > lastEventId).forEach(this::send);
            }
        }

        synchronized void detach(SseEmitter target) {
            if (emitter == target) {
                emitter = null;
            }
        }

        synchronized boolean isConnected() {
            return emitter != null;
        }

        synchronized void publish(String name, Object data) {
            StreamEvent event = new StreamEvent(++lastId, name, data);
            buffer.addLast(event);
            if (buffer.size() > MAX_BUFFER) {
                buffer.removeFirst();
            }
            send(event);
        }

        synchronized void publishAndComplete(String name, Object data) {
            publish(name, data);
            if (emitter != null) {
                emitter.complete();
                emitter = null;
            }
        }

        synchronized void heartbeat() {
            sendComment("hb");
        }

        private void send(StreamEvent e) {
            if (emitter == null) return;
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(e.id()))
                        .name(e.name())
                        .data(e.data(), MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException ex) {
                emitter = null;
            }
        }

        private void sendComment(String text) {
            if (emitter == null) return;
            try {
                emitter.send(SseEmitter.event().comment(text));
            } catch (IOException | IllegalStateException ex) {
                emitter = null;
            }
        }
    }
}