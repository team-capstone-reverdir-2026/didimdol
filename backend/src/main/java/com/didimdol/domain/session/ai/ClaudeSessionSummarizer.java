package com.didimdol.domain.session.ai;

import com.didimdol.domain.message.entity.Message;
import com.didimdol.domain.message.enums.Speaker;
import com.didimdol.domain.message.repository.MessageRepository;
import com.didimdol.domain.persona.entity.PersonaMemory;
import com.didimdol.domain.persona.enums.DisclosureStage;
import com.didimdol.domain.persona.enums.ImpressionDirection;
import com.didimdol.domain.persona.repository.PersonaMemoryRepository;
import com.didimdol.global.ai.AnthropicClient;
import com.didimdol.global.ai.ChatTurn;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "claude")
@RequiredArgsConstructor
public class ClaudeSessionSummarizer implements SessionSummarizer {

    private static final int MAX_TOKENS = 4000;

    private static final String SYSTEM_PROMPT = """
            너는 상담 수련 시뮬레이션의 기록 담당자다. 아래 축어록은 상담자(수련생)와 AI 내담자의 한 회기 대화다.
            축어록을 읽고 반드시 아래 JSON 객체 하나만 출력하라. 설명, 마크다운, 코드블록은 쓰지 마라.

            {
              "disclosureStage": "SURFACE | EVENT | CORE_EMOTION",
              "disclosedTopics": ["내담자가 이번 회기에 실제로 말한 주제", "..."],
              "undisclosedCoreHint": "내담자가 아직 말하지 않았지만 대화 흐름상 숨기고 있는 핵심 (없으면 빈 문자열)",
              "counselorImpressionDirection": "IMPROVED | SAME | WORSENED",
              "counselorImpressionReason": "내담자가 상담자에게 받은 인상이 그렇게 변한 이유 (1~2문장)",
              "emotionalArcSummary": "내담자 감정이 회기 동안 어떻게 흘렀는지 (1~2문장)",
              "carryForwardText": "다음 회기에서 내담자가 기억하고 있어야 할 내용. 내담자 1인칭 시점의 기억 메모, 4~8문장, 아래 규칙 준수",
              "aiSummary": "상담자(수련생)가 읽을 이번 회기 총평 (3~4문장, 존댓말)",
              "aiAdvice": "상담자(수련생)에게 주는 한 줄 조언 (1문장, 존댓말)"
            }

            규칙:
            - disclosureStage: SURFACE=겉도는 이야기만, EVENT=구체적 사건까지, CORE_EMOTION=핵심 감정까지 드러냄.
            - 축어록에 없는 사실을 지어내지 마라. 내담자가 실제로 한 말만 disclosedTopics 와 carryForwardText 에 쓴다.
            - carryForwardText 에는 undisclosedCoreHint 의 내용을 절대 넣지 마라. 말하지 않은 것을 기억으로 만들면 안 된다.
            - carryForwardText 에는 상담자가 어떤 태도였는지(예: 재촉했다, 잘 들어줬다)와 그로 인한 내담자의 느낌을 포함하라.
            - [직전까지의 기억]이 주어지면 carryForwardText 는 그 기억과 이번 회기 내용을 합쳐서 누적해 써라. 이전 회기에서 있었던 일을 빠뜨리지 마라. disclosedTopics 도 이전 주제를 포함해 누적한다.
            - disclosureStage 는 [직전까지의 기억]의 단계보다 낮출 수 없다. 이번 회기에서 더 열렸을 때만 올린다.
            - disclosedTopics 는 최대 12개, 각 항목 25자 이내의 짧은 구절로 쓰고, 비슷한 주제는 합쳐라. 사소한 일상(점심 메뉴 등)은 넣지 마라.
            - carryForwardText 는 8문장 이내로 쓴다. aiSummary 는 4문장 이내, aiAdvice 는 1문장이다.
            - 모든 값은 한국어. 감정/지문 태그 문법([EMOTION:...] 등)은 쓰지 마라.
            """;

    private final MessageRepository messageRepository;
    private final PersonaMemoryRepository memoryRepository;
    private final AnthropicClient anthropicClient;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Override
    public SessionSummary summarize(Long sessionId) {
        List<Message> messages = messageRepository.findBySessionIdOrderBySeqAsc(sessionId);
        String transcript = toTranscript(messages);

        PersonaMemory previous = memoryRepository.findPreviousOf(sessionId).orElse(null);
        StringBuilder user = new StringBuilder();
        if (previous != null) {
            user.append("[직전까지의 기억]\n")
                    .append("공개 단계: ").append(previous.getDisclosureStage().name()).append('\n')
                    .append("이미 말한 주제:\n").append(nullToEmpty(previous.getDisclosedTopics())).append('\n')
                    .append("기억 메모: ").append(previous.getCarryForwardText()).append("\n\n");
        }
        user.append("[이번 회기 축어록]\n").append(transcript);

        String raw = anthropicClient.complete(SYSTEM_PROMPT,
                List.of(new ChatTurn("user", user.toString())), MAX_TOKENS);
        return enforceMonotonic(parse(raw), previous);
    }

    private String toTranscript(List<Message> messages) {
        StringBuilder sb = new StringBuilder();
        for (Message m : messages) {
            if (m.getContent() == null || m.getContent().isBlank()) {
                continue;
            }
            if (m.getSpeaker() == Speaker.COUNSELOR) {
                sb.append("상담자: ").append(m.getContent()).append('\n');
            } else {
                sb.append("내담자");
                if (m.getEmotion() != null) {
                    sb.append('(').append(m.getEmotion().name()).append(')');
                }
                sb.append(": ").append(m.getContent()).append('\n');
            }
        }
        return sb.toString();
    }

    /** 모델이 단계를 낮추거나 이전 주제를 빠뜨려도 코드에서 보정한다 */
    private SessionSummary enforceMonotonic(SessionSummary s, PersonaMemory previous) {
        if (previous == null) {
            return s;
        }
        DisclosureStage stage = s.disclosureStage().ordinal() < previous.getDisclosureStage().ordinal()
                ? previous.getDisclosureStage() : s.disclosureStage();
        java.util.LinkedHashSet<String> topics = new java.util.LinkedHashSet<>();
        for (String t : nullToEmpty(previous.getDisclosedTopics()).split("\n")) {
            if (!t.isBlank()) {
                topics.add(t.strip());
            }
        }
        topics.addAll(s.disclosedTopics());
        return new SessionSummary(stage, List.copyOf(topics), s.undisclosedCoreHint(),
                s.counselorImpressionDirection(), s.counselorImpressionReason(), s.emotionalArcSummary(),
                s.carryForwardText(), s.aiSummary(), s.aiAdvice());
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }

    SessionSummary parse(String raw) {
        String json = extractJson(raw);
        Draft d = jsonMapper.readValue(json, Draft.class);
        if (d.carryForwardText() == null || d.carryForwardText().isBlank()) {
            throw new IllegalStateException("요약 결과에 carryForwardText 가 없습니다.");
        }
        return new SessionSummary(
                parseEnum(DisclosureStage.class, d.disclosureStage(), DisclosureStage.SURFACE),
                d.disclosedTopics() == null ? List.of() : d.disclosedTopics(),
                d.undisclosedCoreHint(),
                parseEnum(ImpressionDirection.class, d.counselorImpressionDirection(), ImpressionDirection.SAME),
                d.counselorImpressionReason(),
                d.emotionalArcSummary(),
                d.carryForwardText().strip(),
                d.aiSummary(),
                d.aiAdvice());
    }

    /** 모델이 코드펜스나 앞뒤 설명을 붙였을 때를 대비해 첫 '{' ~ 마지막 '}' 를 잘라낸다 */
    private String extractJson(String raw) {
        if (raw == null) {
            throw new IllegalStateException("요약 응답이 비어 있습니다.");
        }
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("요약 응답에서 JSON 을 찾지 못했습니다: " + raw);
        }
        return raw.substring(start, end + 1);
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, E fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 {} 값: {}", type.getSimpleName(), value);
            return fallback;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Draft(String disclosureStage, List<String> disclosedTopics, String undisclosedCoreHint,
                 String counselorImpressionDirection, String counselorImpressionReason,
                 String emotionalArcSummary, String carryForwardText, String aiSummary, String aiAdvice) {}
}
