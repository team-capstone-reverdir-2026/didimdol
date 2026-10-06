package com.didimdol.domain.session.service;

import com.didimdol.domain.client.entity.Client;
import com.didimdol.domain.counsel.entity.Counsel;
import com.didimdol.domain.member.entity.Member;
import com.didimdol.domain.member.repository.MemberRepository;
import com.didimdol.domain.message.service.ClientReplyService;
import com.didimdol.domain.session.dto.request.SessionCompleteRequest;
import com.didimdol.domain.session.dto.response.*;
import com.didimdol.domain.session.entity.CounselSession;
import com.didimdol.domain.session.enums.SessionStatus;
import com.didimdol.domain.session.event.SessionCompletedEvent;
import com.didimdol.domain.session.repository.CounselSessionRepository;
import com.didimdol.global.exception.BusinessException;
import com.didimdol.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final int FINAL_ROUND = 3;

    private final SessionAccessor sessionAccessor;
    private final CounselSessionRepository sessionRepository;
    private final MemberRepository memberRepository;
    private final ClientReplyService clientReplyService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SessionCompleteResponse complete(Long memberId, Long sessionId, SessionCompleteRequest request) {
        CounselSession session = sessionAccessor.getOwnedSession(memberId, sessionId);

        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.SESSION_NOT_IN_PROGRESS);
        }
        if (clientReplyService.isGenerating(sessionId)) {
            throw new BusinessException(ErrorCode.CLIENT_RESPONDING);
        }
        if (request.endAt().isBefore(session.getStartAt())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        long duration = Duration.between(session.getStartAt(), request.endAt()).getSeconds();
        session.complete(request.memo(), request.endAt(), duration);

        boolean counselCompleted = session.getSessionRound() >= FINAL_ROUND;
        if (counselCompleted) {
            session.getCounsel().complete();
        }

        eventPublisher.publishEvent(new SessionCompletedEvent(sessionId, counselCompleted));
        return new SessionCompleteResponse(duration, counselCompleted);
    }

    @Transactional(readOnly = true)
    public SessionDetailResponse getDetail(Long memberId, Long sessionId) {
        CounselSession session = sessionAccessor.getOwnedSession(memberId, sessionId);
        Counsel counsel = session.getCounsel();
        Client client = counsel.getClient();

        return new SessionDetailResponse(
                counsel.getMember().getNickname(),
                counsel.getCounselNo(),
                session.getSessionRound(),
                SessionClientResponse.from(client),
                session.getDuration(),
                session.getAiSummary(),
                session.getAiAdvice(),
                session.getId(),   // 축어록 = 회기 메시지 뷰 → 회기 ID 재사용
                null,              // reportId: 11월
                session.getStartAt());
    }

    @Transactional(readOnly = true)
    public SessionMemoResponse getMemo(Long memberId, Long sessionId) {
        return new SessionMemoResponse(sessionAccessor.getOwnedSession(memberId, sessionId).getMemo());
    }

    @Transactional(readOnly = true)
    public SessionListResponse getSessions(Long memberId, Long idAfter, int limit) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        long cursor = idAfter == null ? Long.MAX_VALUE : idAfter;
        List<CounselSession> found = sessionRepository.findPageByMember(
                memberId, SessionStatus.COMPLETED, cursor, PageRequest.of(0, limit + 1));

        boolean hasNext = found.size() > limit;
        List<CounselSession> page = hasNext ? found.subList(0, limit) : found;
        Long nextCursor = hasNext ? page.get(page.size() - 1).getId() : null;

        List<SessionListResponse.Item> items = page.stream()
                .map(s -> new SessionListResponse.Item(
                        s.getCounsel().getId(),
                        s.getCounsel().getCounselNo(),
                        s.getId(),
                        s.getSessionRound(),
                        SessionClientResponse.from(s.getCounsel().getClient()),
                        s.getDuration(),
                        s.getStartAt()))
                .toList();

        return new SessionListResponse(member.getNickname(), items, nextCursor, hasNext);
    }
}