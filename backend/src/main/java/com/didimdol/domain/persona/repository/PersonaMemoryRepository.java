package com.didimdol.domain.persona.repository;

import com.didimdol.domain.persona.entity.PersonaMemory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonaMemoryRepository extends JpaRepository<PersonaMemory, Long> {

    boolean existsBySessionId(Long sessionId);

    /** 같은 상담(counsel)의 특정 회차 기억 */
    Optional<PersonaMemory> findBySessionCounselIdAndSessionSessionRound(Long counselId, int sessionRound);
}
