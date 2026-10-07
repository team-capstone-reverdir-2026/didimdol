package com.didimdol.domain.client.entity;

import com.didimdol.domain.persona.entity.PersonaType;
import com.didimdol.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "clients"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Client extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_type_id", nullable = false)
    private PersonaType personaType;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = 100)
    private String job;

    @Column(length = 500)
    private String imageUrl;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String referralReason;      // 신청 경위

    @Column(nullable = false, columnDefinition = "TEXT")
    private String problemArea;         // 호소 문제 영역

    @Column(nullable = false, columnDefinition = "TEXT")
    private String counselingReason;    // 상담 신청 이유



    @Column(nullable = false, columnDefinition = "TEXT")
    private String familyBackground;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String socialRelationships;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recentEvents;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String growthHistory;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String undisclosedCore;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String speechQuirks;

    /** 말투 예시 발화 (프롬프트 [말투 예시]로 들어감) */
    @Column(columnDefinition = "TEXT")
    private String speechExamples;

    @Builder
    private Client(PersonaType personaType, String name, String gender, int age, String job,
                   String imageUrl, String referralReason, String problemArea,
                   String counselingReason, String familyBackground, String socialRelationships,
                   String recentEvents, String growthHistory, String undisclosedCore,
                   String speechQuirks, String speechExamples) {
        this.personaType = personaType;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.job = job;
        this.imageUrl = imageUrl;
        this.referralReason = referralReason;
        this.problemArea = problemArea;
        this.counselingReason = counselingReason;
        this.familyBackground = familyBackground;
        this.socialRelationships = socialRelationships;
        this.recentEvents = recentEvents;
        this.growthHistory = growthHistory;
        this.undisclosedCore = undisclosedCore;
        this.speechQuirks = speechQuirks;
        this.speechExamples = speechExamples;
    }

    /** 시드(소스 코드)가 기준값이므로 서사/말투 설정 변경을 기존 행에 반영한다 */
    public void syncFromSeed(Client seed) {
        this.referralReason = seed.referralReason;
        this.problemArea = seed.problemArea;
        this.counselingReason = seed.counselingReason;
        this.familyBackground = seed.familyBackground;
        this.socialRelationships = seed.socialRelationships;
        this.recentEvents = seed.recentEvents;
        this.growthHistory = seed.growthHistory;
        this.undisclosedCore = seed.undisclosedCore;
        this.speechQuirks = seed.speechQuirks;
        this.speechExamples = seed.speechExamples;
    }
}
