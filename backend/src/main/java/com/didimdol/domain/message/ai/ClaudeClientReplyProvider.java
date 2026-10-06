package com.didimdol.domain.message.ai;

import com.didimdol.global.ai.AnthropicClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "claude")
@RequiredArgsConstructor
public class ClaudeClientReplyProvider implements ClientReplyProvider {

    private final ReplyContextLoader contextLoader;
    private final AnthropicClient anthropicClient;

    @Override
    public void stream(Long sessionId, Consumer<String> onToken) {
        ReplyContext context = contextLoader.load(sessionId);
        anthropicClient.streamText(context.systemPrompt(), context.turns(), onToken);
    }
}