package com.didimdol.domain.counsel.dto.response;

import java.time.LocalDateTime;

public record CounselCreateResponse(
        String nickname,
        Long counselId,
        int counselNo,
        Long sessionId,
        int sessionRound,
        String imageUrl,
        String previousMemo,   // 1회기는 항상 null
        String sseTicket,
        LocalDateTime createdAt
) {}