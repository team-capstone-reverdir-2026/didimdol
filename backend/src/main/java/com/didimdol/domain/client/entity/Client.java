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

    @Builder
    private Client(PersonaType personaType, String name, String gender, int age, String job,
                   String imageUrl, String referralReason, String problemArea,
                   String counselingReason, String familyBackground, String socialRelationships,
                   String recentEvents, String growthHistory, String undisclosedCore,
                   String speechQuirks) {
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
    }
}
