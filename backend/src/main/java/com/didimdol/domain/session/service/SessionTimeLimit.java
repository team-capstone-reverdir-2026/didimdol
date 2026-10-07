package com.didimdol.domain.session.service;

import com.didimdol.domain.session.entity.CounselSession;

import java.time.Duration;
import java.time.LocalDateTime;

/** 회기 시간 제한 규칙 (서버 기준 시각 = 회기 생성 시각) */
public final class SessionTimeLimit {

    public static final Duration LIMIT = Duration.ofMinutes(30);
    /** 네트워크 지연/마지막 발화 여유 */
    public static final Duration GRACE = Duration.ofMinutes(2);
    /** 이 시간이 지나도 종료되지 않은 회기는 서버가 자동 종료한다 */
    public static final Duration AUTO_CLOSE_AFTER = Duration.ofMinutes(35);

    private SessionTimeLimit() {}

    public static boolean isExceeded(CounselSession session) {
        return LocalDateTime.now().isAfter(session.getCreatedAt().plus(LIMIT).plus(GRACE));
    }
}
