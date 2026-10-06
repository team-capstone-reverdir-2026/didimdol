package com.didimdol.domain.message.entity;

import com.didimdol.domain.message.enums.Emotion;
import com.didimdol.domain.message.enums.NotableCue;
import com.didimdol.domain.message.enums.Speaker;
import com.didimdol.domain.session.entity.CounselSession;

import com.didimdol.global.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "messages",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_messages_session_seq", columnNames = {"session_id", "seq"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private CounselSession session;

    @Column(nullable = false)
    private int seq;   // 세션 내 턴 순서 (1부터)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Speaker speaker;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private long spokenAt;   // 세션 시작 기준 초

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Emotion emotion;   // CLIENT 발화만

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NotableCue notableCue;   // 없으면 null

    @Builder
    private Message(CounselSession session, int seq, Speaker speaker, String content,
                    long spokenAt, Emotion emotion, NotableCue notableCue) {
        this.session = session;
        this.seq = seq;
        this.speaker = speaker;
        this.content = content;
        this.spokenAt = spokenAt;
        this.emotion = emotion;
        this.notableCue = notableCue;
    }

    public void completeClientReply(Emotion emotion, NotableCue notableCue, String content) {
        this.emotion = emotion;
        this.notableCue = notableCue;
        this.content = content;
    }
}