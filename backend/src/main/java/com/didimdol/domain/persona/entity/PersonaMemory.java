package com.didimdol.domain.persona.entity;

import com.didimdol.domain.persona.enums.DisclosureStage;
import com.didimdol.domain.persona.enums.ImpressionDirection;
import com.didimdol.domain.session.entity.CounselSession;
import com.didimdol.global.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 회기 종료 후 Job 2 가 추출하는 내담자 기억. 다음 회기 프롬프트의 [이전 회기 정보]로 들어간다. */
@Getter
@Entity
@Table(
        name = "persona_memories",
        uniqueConstraints = @UniqueConstraint(name = "uk_persona_memories_session", columnNames = "session_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonaMemory extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private CounselSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisclosureStage disclosureStage;

    /** 이미 털어놓은 주제들 (줄바꿈 구분) */
    @Column(columnDefinition = "TEXT")
    private String disclosedTopics;

    /** 아직 말하지 않은 핵심 (내부 일관성용, 먼저 꺼내지 않도록 프롬프트에서 통제) */
    @Column(columnDefinition = "TEXT")
    private String undisclosedCoreHint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImpressionDirection counselorImpressionDirection;

    @Column(columnDefinition = "TEXT")
    private String counselorImpressionReason;

    @Column(columnDefinition = "TEXT")
    private String emotionalArcSummary;

    /** 다음 회기 시스템 프롬프트에 그대로 들어가는 텍스트 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String carryForwardText;

    @Builder
    private PersonaMemory(CounselSession session, DisclosureStage disclosureStage, String disclosedTopics,
                          String undisclosedCoreHint, ImpressionDirection counselorImpressionDirection,
                          String counselorImpressionReason, String emotionalArcSummary, String carryForwardText) {
        this.session = session;
        this.disclosureStage = disclosureStage;
        this.disclosedTopics = disclosedTopics;
        this.undisclosedCoreHint = undisclosedCoreHint;
        this.counselorImpressionDirection = counselorImpressionDirection;
        this.counselorImpressionReason = counselorImpressionReason;
        this.emotionalArcSummary = emotionalArcSummary;
        this.carryForwardText = carryForwardText;
    }
}
