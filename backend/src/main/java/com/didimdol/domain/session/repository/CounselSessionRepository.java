package com.didimdol.domain.session.repository;

import com.didimdol.domain.session.entity.CounselSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.didimdol.domain.session.enums.SessionStatus;
import java.util.List;

import java.util.Optional;

public interface CounselSessionRepository extends JpaRepository<CounselSession, Long> {
    Optional<CounselSession> findTopByCounselIdOrderBySessionRoundDesc(Long counselId);

    List<CounselSession> findByCounselId(Long counselId);

    @Modifying
    @Query("delete from CounselSession s where s.counsel.id = :counselId")
    void deleteAllByCounselId(@Param("counselId") Long counselId);

    @Query("""
        select s from CounselSession s
        join fetch s.counsel c
        join fetch c.client cl
        join fetch cl.personaType
        where c.member.id = :memberId
          and s.status = :status
          and s.id < :cursor
        order by s.id desc
        """)
    List<CounselSession> findPageByMember(@Param("memberId") Long memberId,
                                          @Param("status") SessionStatus status,
                                          @Param("cursor") Long cursor,
                                          Pageable pageable);

    /** 제한 시간을 넘겼는데 아직 종료되지 않은 회기 (자동 종료 대상) */
    List<CounselSession> findByStatusAndCreatedAtBefore(SessionStatus status, java.time.LocalDateTime threshold);
}
