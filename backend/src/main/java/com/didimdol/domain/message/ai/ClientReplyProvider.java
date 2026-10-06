package com.didimdol.domain.message.ai;

import java.util.function.Consumer;

public interface ClientReplyProvider {
    void stream(Long sessionId, Consumer<String> onToken);
}
