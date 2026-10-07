package com.didimdol.domain.session.entity;

import com.didimdol.domain.counsel.entity.Counsel;
import com.didimdol.domain.session.enums.SessionStatus;
import com.didimdol.global.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "sessions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_sessions_counsel_round", columnNames = {"counsel_id", "session_round"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselSession extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "counsel_id", nullable = false)
    private Counsel counsel;

    @Column(name = "session_round", nullable = false)
    private int sessionRound;   // 1~3

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    @Column(nullable = false)
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Long duration;   // 초

    @Column(columnDefinition = "TEXT")
    private String memo;     // 상담자가 쓰는 메모

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @Column(columnDefinition = "TEXT")
    private String aiAdvice;

    @Builder
    private CounselSession(Counsel counsel, int sessionRound, LocalDateTime startAt) {
        this.counsel = counsel;
        this.sessionRound = sessionRound;
        this.startAt = startAt;
        this.status = SessionStatus.IN_PROGRESS;
    }

    public void complete(String memo, LocalDateTime endAt, Long duration) {
        this.memo = memo;
        this.endAt = endAt;
        this.duration = duration;
        this.status = SessionStatus.COMPLETED;
    }

    public void applyAiFeedback(String aiSummary, String aiAdvice) {
        this.aiSummary = aiSummary;
        this.aiAdvice = aiAdvice;
    }
}
