package com.didimdol.domain.persona.entity;

import com.didimdol.domain.persona.enums.PersonaTypeCode;
import com.didimdol.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "persona_types")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonaType extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "persona_type", nullable = false, unique = true, length = 30)
    private PersonaTypeCode type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String coreBelief; // 속마음

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reactionRules; // 반응규칙표

    @Column(nullable = false, columnDefinition = "TEXT")
    private String speechStyle;         // 말하는 방식

    @Column(nullable = false, columnDefinition = "TEXT")
    private String disclosureLogic;     // 마음의 문

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prohibitions;        // 금지사항

    @Column(nullable = false, columnDefinition = "TEXT")
    private String outputFormatRules;   // 출력 형식

    @Builder
    private PersonaType(PersonaTypeCode type, String coreBelief, String reactionRules,
                        String speechStyle, String disclosureLogic, String prohibitions,
                        String outputFormatRules) {
        this.type = type;
        this.coreBelief = coreBelief;
        this.reactionRules = reactionRules;
        this.speechStyle = speechStyle;
        this.disclosureLogic = disclosureLogic;
        this.prohibitions = prohibitions;
        this.outputFormatRules = outputFormatRules;
    }

    /** 시드(소스 코드)가 기준값이므로, 프롬프트 설정 변경을 기존 행에 반영한다 */
    public void syncPromptFields(PersonaType seed) {
        this.coreBelief = seed.coreBelief;
        this.reactionRules = seed.reactionRules;
        this.speechStyle = seed.speechStyle;
        this.disclosureLogic = seed.disclosureLogic;
        this.prohibitions = seed.prohibitions;
        this.outputFormatRules = seed.outputFormatRules;
    }
}
