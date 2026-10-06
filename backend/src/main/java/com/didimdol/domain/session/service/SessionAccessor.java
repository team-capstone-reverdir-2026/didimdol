package com.didimdol.domain.session.service;

import com.didimdol.domain.session.entity.CounselSession;
import com.didimdol.domain.session.repository.CounselSessionRepository;
import com.didimdol.global.exception.BusinessException;
import com.didimdol.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionAccessor {

    private final CounselSessionRepository sessionRepository;

    /** 반드시 트랜잭션 안에서 호출할 것 (lazy 연관 접근) */
    public CounselSession getOwnedSession(Long memberId, Long sessionId) {
        CounselSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUNSEL_NOT_FOUND));
        if (!session.getCounsel().getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return session;
    }
}