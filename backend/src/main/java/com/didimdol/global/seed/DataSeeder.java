package com.didimdol.global.seed;

import com.didimdol.domain.client.entity.Client;
import com.didimdol.domain.client.repository.ClientRepository;
import com.didimdol.domain.persona.entity.PersonaType;
import com.didimdol.domain.persona.repository.PersonaTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private final PersonaTypeRepository personaTypeRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        PersonaType silenceResistant = seedPersonaType(PersonaTypeSeeds.silenceResistant());
        PersonaType approvalSeeking = seedPersonaType(PersonaTypeSeeds.approvalSeeking());

        seedClient(ClientSeeds.leeJunho(silenceResistant));
        seedClient(ClientSeeds.parkSeoyeon(approvalSeeking));
    }

    private PersonaType seedPersonaType(PersonaType seed) {
        return personaTypeRepository.findByType(seed.getType())
                .map(existing -> {
                    existing.syncPromptFields(seed);   // 더티체킹으로 UPDATE
                    return existing;
                })
                .orElseGet(() -> {
                    log.info("Seeding persona type: {}", seed.getType());
                    return personaTypeRepository.save(seed);
                });
    }

    private void seedClient(Client seed) {
        clientRepository.findByName(seed.getName()).ifPresentOrElse(
                existing -> existing.syncFromSeed(seed),   // 더티체킹으로 UPDATE
                () -> {
                    log.info("Seeding client: {}", seed.getName());
                    clientRepository.save(seed);
                });
    }
}