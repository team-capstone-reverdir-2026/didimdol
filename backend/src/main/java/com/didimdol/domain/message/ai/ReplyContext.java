package com.didimdol.domain.message.ai;

import com.didimdol.global.ai.ChatTurn;
import java.util.List;

public record ReplyContext(String systemPrompt, List<ChatTurn> turns) {}