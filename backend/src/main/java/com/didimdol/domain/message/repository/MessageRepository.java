package com.didimdol.domain.message.repository;

import com.didimdol.domain.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findTopBySessionIdOrderBySeqDesc(Long sessionId);
    List<Message> findBySessionIdOrderBySeqAsc(Long sessionId);

    @Modifying
    @Query("delete from Message m where m.session.counsel.id = :counselId")
    void deleteAllByCounselId(@Param("counselId") Long counselId);
}
