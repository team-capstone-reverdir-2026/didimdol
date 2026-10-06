package com.didimdol.domain.client.service;

import com.didimdol.domain.client.dto.response.ClientDetailResponse;
import com.didimdol.domain.client.dto.response.ClientListResponse;
import com.didimdol.domain.client.entity.Client;
import com.didimdol.domain.client.repository.ClientRepository;
import com.didimdol.domain.member.repository.MemberRepository;
import com.didimdol.global.exception.BusinessException;
import com.didimdol.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository clientRepository;
    private final MemberRepository memberRepository;

    public ClientListResponse getClients(Long memberId, Long idAfter, int limit) {
        String nickname = getNickname(memberId);
        long cursor = (idAfter == null) ? 0L : idAfter;

        List<Client> fetched = clientRepository
                .findByIdGreaterThanOrderByIdAsc(cursor, PageRequest.of(0, limit + 1));

        boolean hasNext = fetched.size() > limit;
        List<Client> page = hasNext ? fetched.subList(0, limit) : fetched;
        Long nextCursor = hasNext ? page.get(page.size() - 1).getId() : null;

        return new ClientListResponse(
                nickname,
                page.stream().map(ClientListResponse.ClientSummary::from).toList(),
                nextCursor,
                hasNext
        );
    }

    public ClientDetailResponse getClient(Long memberId, Long clientId) {
        String nickname = getNickname(memberId);
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLIENT_NOT_FOUND));
        return ClientDetailResponse.of(nickname, client);
    }

    private String getNickname(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED))
                .getNickname();
    }
}