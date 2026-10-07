package com.didimdol.domain.message.ai;

import com.didimdol.domain.client.entity.Client;
import com.didimdol.domain.message.entity.Message;
import com.didimdol.domain.message.enums.Emotion;
import com.didimdol.domain.message.enums.Speaker;
import com.didimdol.domain.message.repository.MessageRepository;
import com.didimdol.domain.persona.entity.PersonaMemory;
import com.didimdol.domain.persona.repository.PersonaMemoryRepository;
import com.didimdol.domain.session.entity.CounselSession;
import com.didimdol.domain.session.repository.CounselSessionRepository;
import com.didimdol.global.ai.ChatTurn;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplyContextLoader {

    private final CounselSessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final PersonaMemoryRepository memoryRepository;
    private final PromptAssembler promptAssembler;

    @Transactional(readOnly = true)
    public ReplyContext load(Long sessionId) {
        CounselSession session = sessionRepository.findById(sessionId).orElseThrow();
        Client client = session.getCounsel().getClient();

        String carryForward = session.getSessionRound() > 1
                ? memoryRepository.findBySessionCounselIdAndSessionSessionRound(
                                session.getCounsel().getId(), session.getSessionRound() - 1)
                        .map(PersonaMemory::getCarryForwardText)
                        .orElse(null)
                : null;
        String systemPrompt = promptAssembler.assemble(
                client.getPersonaType(), client, session.getSessionRound(), carryForward);

        List<Message> messages = messageRepository.findBySessionIdOrderBySeqAsc(sessionId);
        return new ReplyContext(systemPrompt, toTurns(messages));
    }

    private List<ChatTurn> toTurns(List<Message> messages) {
        List<ChatTurn> turns = new ArrayList<>();
        for (Message m : messages) {
            if (m.getContent() == null || m.getContent().isBlank()) {
                continue; // 생성 중인 빈 틀
            }
            String role;
            String content;
            if (m.getSpeaker() == Speaker.COUNSELOR) {
                role = "user";
                content = m.getContent();
            } else {
                role = "assistant";
                content = toTaggedText(m);
            }
            appendMerged(turns, role, content);
        }
        return turns;
    }

    /** 내담자 발화는 태그를 포함한 원문 형태로 되돌려 보낸다 (모델이 자기 감정 흐름을 추적하도록) */
    private String toTaggedText(Message m) {
        Emotion emotion = m.getEmotion() != null ? m.getEmotion() : Emotion.NEUTRAL;
        String cue = m.getNotableCue() != null ? m.getNotableCue().name() : "NONE";
        return "[EMOTION:" + emotion.name() + "|CUE:" + cue + "]\n" + m.getContent();
    }

    private void appendMerged(List<ChatTurn> turns, String role, String content) {
        if (!turns.isEmpty() && turns.get(turns.size() - 1).role().equals(role)) {
            ChatTurn last = turns.remove(turns.size() - 1);
            turns.add(new ChatTurn(role, last.content() + "\n" + content));
        } else {
            turns.add(new ChatTurn(role, content));
        }
    }
}