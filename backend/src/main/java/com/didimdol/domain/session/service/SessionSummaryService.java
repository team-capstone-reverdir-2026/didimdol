package com.didimdol.domain.session.service;

import com.didimdol.domain.persona.entity.PersonaMemory;
import com.didimdol.domain.persona.repository.PersonaMemoryRepository;
import com.didimdol.domain.session.ai.SessionSummarizer;
import com.didimdol.domain.session.ai.SessionSummary;
import com.didimdol.domain.session.entity.CounselSession;
import com.didimdol.domain.session.repository.CounselSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ExecutorService;

/** Job 2 실행기: 회기 종료 후 비동기로 요약을 만들고 PersonaMemory / 총평·조언을 저장한다 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionSummaryService {

    private static final int MAX_ATTEMPTS = 2;

    private final SessionSummarizer summarizer;
    private final PersonaMemoryRepository memoryRepository;
    private final CounselSessionRepository sessionRepository;
    private final TransactionTemplate transactionTemplate;
    private final ExecutorService replyExecutor;

    public void summarizeAsync(Long sessionId) {
        replyExecutor.submit(() -> summarizeWithRetry(sessionId));
    }

    void summarizeWithRetry(Long sessionId) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                if (memoryRepository.existsBySessionId(sessionId)) {
                    return;
                }
                // LLM 호출은 트랜잭션 밖에서 (DB 커넥션을 오래 잡지 않도록)
                SessionSummary summary = summarizer.summarize(sessionId);
                transactionTemplate.executeWithoutResult(status -> save(sessionId, summary));
                log.info("회기 요약 저장 완료 sessionId={}", sessionId);
                return;
            } catch (Exception e) {
                log.error("회기 요약 실패 sessionId={} attempt={}/{}", sessionId, attempt, MAX_ATTEMPTS, e);
            }
        }
    }

    private void save(Long sessionId, SessionSummary s) {
        CounselSession session = sessionRepository.findById(sessionId).orElseThrow();
        session.applyAiFeedback(s.aiSummary(), s.aiAdvice());
        memoryRepository.save(PersonaMemory.builder()
                .session(session)
                .disclosureStage(s.disclosureStage())
                .disclosedTopics(String.join("\n", s.disclosedTopics()))
                .undisclosedCoreHint(s.undisclosedCoreHint())
                .counselorImpressionDirection(s.counselorImpressionDirection())
                .counselorImpressionReason(s.counselorImpressionReason())
                .emotionalArcSummary(s.emotionalArcSummary())
                .carryForwardText(s.carryForwardText())
                .build());
    }
}
