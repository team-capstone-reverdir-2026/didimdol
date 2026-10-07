package com.didimdol.domain.client.repository;

import com.didimdol.domain.client.entity.Client;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client,Long> {
    @EntityGraph(attributePaths = "personaType")
    List<Client> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);
    boolean existsByName(String name);
    Optional<Client> findByName(String name);
}
