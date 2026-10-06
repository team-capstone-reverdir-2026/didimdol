package com.didimdol.domain.persona.repository;

import com.didimdol.domain.persona.entity.PersonaType;
import com.didimdol.domain.persona.enums.PersonaTypeCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonaTypeRepository extends JpaRepository<PersonaType, Long> {
    Optional<PersonaType> findByType(PersonaTypeCode type);
}