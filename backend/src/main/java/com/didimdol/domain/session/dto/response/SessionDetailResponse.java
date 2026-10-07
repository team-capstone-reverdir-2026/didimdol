package com.didimdol.domain.session.dto.response;

import java.time.LocalDateTime;

public record SessionDetailResponse(
        String nickname,
        Long counselId,
        int counselNo,
        int sessionRound,
        SessionClientResponse client,
        Long duration,
        String aiSummary,
        String aiAdvice,
        Long transcriptId,
        Long reportId,
        LocalDateTime createdAt
) {}