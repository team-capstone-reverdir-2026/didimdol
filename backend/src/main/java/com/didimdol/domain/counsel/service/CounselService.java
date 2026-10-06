package com.didimdol.domain.counsel.service;

import com.didimdol.domain.client.entity.Client;
import com.didimdol.domain.client.repository.ClientRepository;
import com.didimdol.domain.counsel.dto.request.CounselCreateRequest;
import com.didimdol.domain.counsel.dto.request.NextSessionCreateRequest;
import com.didimdol.domain.counsel.dto.response.CounselCreateResponse;
import com.didimdol.domain.counsel.entity.Counsel;
import com.didimdol.domain.counsel.enums.CounselStatus;
import com.didimdol.domain.counsel.repository.CounselRepository;
import com.didimdol.domain.member.entity.Member;
import com.didimdol.domain.member.repository.MemberRepository;
import com.didimdol.domain.message.entity.Message;
import com.didimdol.domain.message.enums.Speaker;
import com.didimdol.domain.message.repository.MessageRepository;
import com.didimdol.domain.session.entity.CounselSession;
import com.didimdol.domain.session.enums.SessionStatus;
import com.didimdol.domain.session.repository.CounselSessionRepository;
import com.didimdol.global.exception.BusinessException;
import com.didimdol.global.exception.ErrorCode;
import com.didimdol.global.sse.SseTicketStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class CounselService {

    private static final int FIRST_ROUND = 1;
    private static final int MAX_ROUND = 3;

    private final MemberRepository memberRepository;
    private final ClientRepository clientRepository;
    private final CounselRepository counselRepository;
    private final CounselSessionRepository counselSessionRepository;
    private final MessageRepository messageRepository;
    private final SseTicketStore sseTicketStore;


    @Transactional
    public CounselCreateResponse createCounsel(Long memberId, CounselCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        int counselNo = counselRepository.findMaxCounselNo(memberId) + 1;
        Counsel counsel = counselRepository.save(Counsel.builder()
                .member(member)
                .client(client)
                .counselNo(counselNo)
                .build());

        CounselSession session = counselSessionRepository.save(CounselSession.builder()
                .counsel(counsel)
                .sessionRound(FIRST_ROUND)
                .startAt(request.startAt())
                .build());

        messageRepository.save(Message.builder()
                .session(session)
                .seq(1)
                .speaker(Speaker.COUNSELOR)
                .content(request.initialMessage())
                .spokenAt(0L)
                .build());

        String sseTicket = sseTicketStore.issue(session.getId(), memberId);

        return new CounselCreateResponse(
                member.getNickname(),
                counsel.getId(),
                counsel.getCounselNo(),
                session.getId(),
                session.getSessionRound(),
                client.getImageUrl(),
                null,
                sseTicket,
                counsel.getCreatedAt().truncatedTo(ChronoUnit.SECONDS)
        );
    }

    @Transactional
    public CounselCreateResponse createNextSession(Long memberId, Long counselId,
                                                   NextSessionCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        Counsel counsel = counselRepository.findById(counselId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUNSEL_NOT_FOUND));

        if (!counsel.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        CounselSession last = counselSessionRepository
                .findTopByCounselIdOrderBySessionRoundDesc(counselId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUNSEL_NOT_FOUND));

        if (counsel.getStatus() == CounselStatus.COMPLETED || last.getSessionRound() >= MAX_ROUND) {
            throw new BusinessException(ErrorCode.COUNSEL_ALREADY_COMPLETED);
        }
        if (last.getStatus() != SessionStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.PREVIOUS_SESSION_NOT_COMPLETED);
        }

        CounselSession session = counselSessionRepository.save(CounselSession.builder()
                .counsel(counsel)
                .sessionRound(last.getSessionRound() + 1)
                .startAt(request.startAt())
                .build());

        messageRepository.save(Message.builder()
                .session(session)
                .seq(1)
                .speaker(Speaker.COUNSELOR)
                .content(request.initialMessage())
                .spokenAt(0L)
                .build());

        String sseTicket = sseTicketStore.issue(session.getId(), memberId);

        return new CounselCreateResponse(
                member.getNickname(),
                counsel.getId(),
                counsel.getCounselNo(),
                session.getId(),
                session.getSessionRound(),
                counsel.getClient().getImageUrl(),
                last.getMemo(),
                sseTicket,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }
}