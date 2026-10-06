package com.didimdol.domain.session.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SessionListResponse(
        String nickname,
        List<Item> sessions,
        Long nextCursor,
        boolean hasNext
) {
    public record Item(
            Long counselId,
            int counselNo,
            Long sessionId,
            int sessionRound,
            SessionClientResponse client,
            Long duration,
            LocalDateTime createdAt
    ) {}
}