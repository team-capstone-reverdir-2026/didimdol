package com.didimdol.domain.session.dto.response;

import com.didimdol.domain.client.entity.Client;
import java.util.List;

public record SessionClientResponse(Long clientId, String clientName, List<String> tags) {

    public static SessionClientResponse from(Client client) {
        return new SessionClientResponse(
                client.getId(),
                client.getName(),
                List.of(client.getPersonaType().getType().name()));
    }
}