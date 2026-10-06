package com.didimdol.domain.session.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record SessionCompleteRequest(
        @Size(max = 5000) String memo,
        @NotNull LocalDateTime endAt
) {}