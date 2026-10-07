package com.didimdol.domain.persona.repository;

import com.didimdol.domain.persona.entity.PersonaMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PersonaMemoryRepository extends JpaRepository<PersonaMemory, Long> {

    boolean existsBySessionId(Long sessionId);

    @Modifying
    @Query("delete from PersonaMemory m where m.session.counsel.id = :counselId")
    void deleteAllByCounselId(@Param("counselId") Long counselId);

    /** 같은 상담(counsel)의 특정 회차 기억 */
    Optional<PersonaMemory> findBySessionCounselIdAndSessionSessionRound(Long counselId, int sessionRound);

    /** 주어진 회기의 바로 앞 회기 기억 (1회기면 비어 있음) */
    @Query("""
            select m from PersonaMemory m
            where m.session.counsel.id = (select s.counsel.id from CounselSession s where s.id = :sessionId)
              and m.session.sessionRound = (select s2.sessionRound - 1 from CounselSession s2 where s2.id = :sessionId)
            """)
    Optional<PersonaMemory> findPreviousOf(@Param("sessionId") Long sessionId);
}
