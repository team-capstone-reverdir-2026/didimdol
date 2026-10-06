package com.didimdol.domain.message.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "fake", matchIfMissing = true)
public class FakeClientReplyProvider implements ClientReplyProvider {

    @Override
    public void stream(Long sessionId, Consumer<String> onToken) {
        String[] tokens = {"[EMO", "TION:SAD|CUE:NONE]", "\n요즘 ", "마음이 ", "복잡해요", "... ", "잘 모르겠어요."};
        for (String token : tokens) {
            onToken.accept(token);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
    }
}