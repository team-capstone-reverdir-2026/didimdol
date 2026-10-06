package com.didimdol.domain.message.dto;

import com.didimdol.domain.message.enums.Emotion;

public record ClientMessagePayload(Long messageId, Emotion emotion, String content) {}