package com.didimdol.domain.session.event;

public record SessionCompletedEvent(Long sessionId, boolean counselCompleted) {}