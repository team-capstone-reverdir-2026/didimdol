package com.didimdol.domain.message.repository;

import com.didimdol.domain.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findTopBySessionIdOrderBySeqDesc(Long sessionId);
    List<Message> findBySessionIdOrderBySeqAsc(Long sessionId);
}
