package com.didimdol.domain.session.ai;

import com.didimdol.domain.persona.enums.DisclosureStage;
import com.didimdol.domain.persona.enums.ImpressionDirection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "fake", matchIfMissing = true)
public class FakeSessionSummarizer implements SessionSummarizer {

    @Override
    public SessionSummary summarize(Long sessionId) {
        return new SessionSummary(
                DisclosureStage.SURFACE,
                List.of("요즘 마음이 복잡하다"),
                "아직 꺼내지 않은 속마음이 있다",
                ImpressionDirection.SAME,
                "(fake) 인상 변화 없음",
                "(fake) 전반적으로 조심스러운 태도를 유지했다",
                "지난 회기에서 요즘 마음이 복잡하다는 이야기를 조금 했다. 아직 깊은 이야기는 하지 않았다.",
                "(fake) 상담 총평 자리표시자",
                "(fake) 한 줄 조언 자리표시자");
    }
}
