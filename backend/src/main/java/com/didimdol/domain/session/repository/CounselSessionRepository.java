package com.didimdol.domain.session.repository;

import com.didimdol.domain.session.entity.CounselSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.didimdol.domain.session.enums.SessionStatus;
import java.util.List;

import java.util.Optional;

public interface CounselSessionRepository extends JpaRepository<CounselSession, Long> {
    Optional<CounselSession> findTopByCounselIdOrderBySessionRoundDesc(Long counselId);

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
}