package com.didimdol.domain.session.ai;

/** Job 2: 종료된 회기의 축어록을 읽고 구조화된 요약을 만든다 */
public interface SessionSummarizer {
    SessionSummary summarize(Long sessionId);
}
