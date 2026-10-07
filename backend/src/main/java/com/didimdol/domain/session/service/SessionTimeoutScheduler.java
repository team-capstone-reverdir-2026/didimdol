package com.didimdol.domain.session.service;

import com.didimdol.domain.session.enums.SessionStatus;
import com.didimdol.domain.session.repository.CounselSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** 탭을 닫는 등으로 종료 호출이 오지 않은 회기를 주기적으로 정리한다 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionTimeoutScheduler {

    private final CounselSessionRepository sessionRepository;
    private final SessionService sessionService;

    @Scheduled(fixedDelay = 60_000)
    public void closeExpiredSessions() {
        LocalDateTime threshold = LocalDateTime.now().minus(SessionTimeLimit.AUTO_CLOSE_AFTER);
        sessionRepository.findByStatusAndCreatedAtBefore(SessionStatus.IN_PROGRESS, threshold)
                .forEach(s -> {
                    try {
                        sessionService.autoComplete(s.getId());
                    } catch (Exception e) {
                        log.error("회기 자동 종료 실패 sessionId={}", s.getId(), e);
                    }
                });
    }
}
