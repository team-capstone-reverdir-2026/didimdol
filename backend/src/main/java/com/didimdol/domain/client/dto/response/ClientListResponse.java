package com.didimdol.domain.client.dto.response;

import com.didimdol.domain.client.entity.Client;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record ClientListResponse(
        String nickname,
        List<ClientSummary> clients,
        Long nextCursor,
        boolean hasNext
) {
    public record ClientSummary(
            Long clientId,
            String clientName,
            List<String> tags,
            String imageUrl,
            LocalDateTime createdAt
    ) {
        public static ClientSummary from(Client client) {
            return new ClientSummary(
                    client.getId(),
                    client.getName(),
                    List.of(client.getPersonaType().getType().name()),   // tags = persona_type명
                    client.getImageUrl(),
                    client.getCreatedAt().truncatedTo(ChronoUnit.SECONDS)
            );
        }
    }
}