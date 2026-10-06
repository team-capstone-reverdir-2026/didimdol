package com.didimdol.domain.client.dto.response;

import com.didimdol.domain.client.entity.Client;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record ClientDetailResponse(
        String nickname,
        Long clientId,
        String clientName,
        int age,
        String job,
        List<String> tags,
        String imageUrl,
        String referralReason,
        String problemArea,
        String counselingReason,
        LocalDateTime createdAt
) {
    public static ClientDetailResponse of(String nickname, Client client) {
        return new ClientDetailResponse(
                nickname,
                client.getId(),
                client.getName(),
                client.getAge(),
                client.getJob(),
                List.of(client.getPersonaType().getType().name()),
                client.getImageUrl(),
                client.getReferralReason(),
                client.getProblemArea(),
                client.getCounselingReason(),
                client.getCreatedAt().truncatedTo(ChronoUnit.SECONDS)
        );
    }
}