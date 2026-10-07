package com.didimdol.domain.session.ai;

import com.didimdol.domain.persona.enums.DisclosureStage;
import com.didimdol.domain.persona.enums.ImpressionDirection;

import java.util.List;

/** Job 2 결과: 내담자 기억(PersonaMemory) + 상담자용 총평/조언 */
public record SessionSummary(
        DisclosureStage disclosureStage,
        List<String> disclosedTopics,
        String undisclosedCoreHint,
        ImpressionDirection counselorImpressionDirection,
        String counselorImpressionReason,
        String emotionalArcSummary,
        String carryForwardText,
        String aiSummary,
        String aiAdvice
) {}
