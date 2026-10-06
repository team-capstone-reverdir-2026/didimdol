package com.didimdol.global.sse; // 누님 기본 패키지에 맞춰주세요

import com.didimdol.global.config.properties.SseProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SseTicketStore {

    public record TicketInfo(Long sessionId, Long memberId) {}

    private record Entry(TicketInfo info, Instant expiresAt) {}

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SseProperties properties;
    private final Map<String, Entry> tickets = new ConcurrentHashMap<>();

    public String issue(Long sessionId, Long memberId) {
        purgeExpired();
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tickets.put(ticket, new Entry(
                new TicketInfo(sessionId, memberId),
                Instant.now().plusSeconds(properties.ticketTtlSeconds())));
        return ticket;
    }

    /** 재연결을 위해 사용 후 폐기하지 않는다 (만료 전까지 재사용 가능) */
    public Optional<TicketInfo> verify(String ticket) {
        Entry entry = tickets.get(ticket);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAt().isBefore(Instant.now())) {
            tickets.remove(ticket);
            return Optional.empty();
        }
        return Optional.of(entry.info());
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        tickets.values().removeIf(entry -> entry.expiresAt().isBefore(now));
    }
}