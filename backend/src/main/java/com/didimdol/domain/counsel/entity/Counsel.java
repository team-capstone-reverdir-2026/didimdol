package com.didimdol.domain.counsel.entity;


import com.didimdol.domain.client.entity.Client;
import com.didimdol.domain.counsel.enums.CounselStatus;
import com.didimdol.domain.member.entity.Member;
import com.didimdol.global.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(
        name = "counsels",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_counsels_member_no", columnNames = {"member_id", "counsel_no"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Counsel extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "counsel_no", nullable = false)
    private int counselNo;   // 유저별 노출 순번

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CounselStatus status;

    @Builder
    private Counsel(Member member, Client client, int counselNo) {
        this.member = member;
        this.client = client;
        this.counselNo = counselNo;
        this.status = CounselStatus.IN_PROGRESS;
    }

    public void complete() {
        this.status = CounselStatus.COMPLETED;
    }
}
