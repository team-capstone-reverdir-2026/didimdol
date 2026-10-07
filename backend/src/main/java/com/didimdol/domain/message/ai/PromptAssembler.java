package com.didimdol.domain.message.ai;

import com.didimdol.domain.client.entity.Client;
import com.didimdol.domain.persona.entity.PersonaType;
import com.didimdol.domain.persona.enums.PersonaTypeCode;
import org.springframework.stereotype.Component;

@Component
public class PromptAssembler {

    private static final String ROLE_DIRECTIVE = """
            당신은 상담 훈련 시뮬레이션의 내담자 역할을 연기한다.
            아래 제공되는 인물 설정과 상태 정보를 벗어나지 않는다.
            상담자(사용자)의 발화에 대해 내담자로서만 응답하며, 상담자 역할을 겸하지 않는다.
            구어체로 응답한다. 모든 내용을 매끄럽게 완결된 문장으로 정리하지 않는다. 한 번의 응답에는 한 가지 사실이나 감정 정도만 담고, 근거나 사례를 여러 개 나열하지 않는다. 동의를 구하는 표현이나 감사 인사 같은 반복적 어구는 매 턴 쓰지 않고 필요한 순간에만 선택적으로 사용한다.
            """;

    private static final String OUTPUT_FORMAT = """
            [출력 형식]
            모든 응답은 다음 형식의 태그로 시작한다: [EMOTION:X|CUE:Y]
            X는 NEUTRAL, HAPPY, SAD, ANGRY 중 하나.
            Y는 NONE, SILENCE, RESISTANCE, CRYING, LAUGHING 중 하나이며, 해당 사항이 없으면 NONE으로 쓴다. SILENCE는 응답 안에 실제로 (침묵 n초) 표현이 포함된 경우에만 사용한다.
            이 지시문에 쓰인 X, Y는 값의 종류를 설명하기 위한 자리표시자이다. 응답에는 "[EMOTION:X|CUE:Y]"라는 문자열을 그대로 출력하지 말고, 반드시 실제 값으로 치환한 태그를 한 번만 출력한다.
            태그 다음 줄부터 대사만 작성한다. 비언어적 정보가 대사만으로 전달되지 않을 때에 한해, 짧은 소리·침묵 표기만 괄호로 쓸 수 있다. 허용 표기는 (침묵 n초), (한숨), (웃음), (울먹임), (흐느낌), (목소리가 떨린다), (말이 빨라진다)뿐이며, 대부분의 응답에는 괄호를 쓰지 않는다. 1회 응답에 최대 1개까지만 쓴다. 표정, 시선, 몸짓, 동작 묘사와 내면 서술은 쓰지 않는다. 말이 끊기거나 멈추는 순간은 항상 (침묵 n초) 형태로만 쓰고 '잠깐 멈춤' 같은 다른 표현은 쓰지 않는다. CUE 대응: (침묵 n초)는 SILENCE, (울먹임)·(흐느낌)은 CRYING, (웃음)은 LAUGHING.
            1회 응답은 2~4문장을 기본으로 하되, 정서가 고양되거나 저항이 강한 구간은 예외적으로 길어질 수 있다.
            존댓말을 유지한다. 1회 응답 500자 상한.
            """;

    /** carryForwardText: 직전 회기 요약 (1회기는 null) */
    public String assemble(PersonaType persona, Client client, int sessionRound, String carryForwardText) {
        StringBuilder sb = new StringBuilder();

        // [A] 역할 고정 지시문
        sb.append(ROLE_DIRECTIVE).append('\n');

        // [B] 설정집 6칸
        sb.append("[인물 유형: ").append(label(persona.getType())).append("]\n");
        section(sb, "핵심 정체성", persona.getCoreBelief());
        section(sb, "반응 규칙", persona.getReactionRules());
        section(sb, "말하는 방식", persona.getSpeechStyle());
        section(sb, "마음의 문", persona.getDisclosureLogic());
        section(sb, "금지사항", persona.getProhibitions());
        section(sb, "유형별 출력 규칙", persona.getOutputFormatRules());

        // [C] 사례카드
        sb.append("[개별 인물: ")
                .append(client.getName()).append(", ")
                .append(client.getAge()).append("세, ")
                .append(client.getGender()).append(", ")
                .append(client.getJob()).append("]\n");
        line(sb, "상담 신청 경위", client.getReferralReason());
        line(sb, "호소 문제", client.getProblemArea());
        line(sb, "상담 동기", client.getCounselingReason());
        line(sb, "가족 관계", client.getFamilyBackground());
        line(sb, "친구 관계", client.getSocialRelationships());
        line(sb, "최근 상황", client.getRecentEvents());
        line(sb, "성장 과거력", client.getGrowthHistory());
        line(sb, "미공개 핵심(공개 3단계에서만 드러남)", client.getUndisclosedCore());
        line(sb, "개인 말투 특징", client.getSpeechQuirks());
        sb.append('\n');

        // [D] 이전 회기 정보
        sb.append("[이전 회기 정보]\n").append(previousInfo(sessionRound, carryForwardText)).append("\n\n");

        // [E] 출력 형식 최종 규칙
        sb.append(OUTPUT_FORMAT);
        return sb.toString();
    }

    private String previousInfo(int sessionRound, String carryForwardText) {
        if (sessionRound == 1) {
            return "없음 (1회기)";
        }
        if (carryForwardText == null || carryForwardText.isBlank()) {
            return "이전 회기 요약 정보가 없다.";
        }
        return carryForwardText.strip();
    }

    private String label(PersonaTypeCode type) {
        return switch (type) {
            case SILENCE_RESISTANT -> "침묵저항형";
            case APPROVAL_SEEKING -> "인정요구형";
        };
    }

    private void section(StringBuilder sb, String label, String value) {
        if (value == null || value.isBlank()) return;
        sb.append(label).append(":\n").append(value.strip()).append("\n\n");
    }

    private void line(StringBuilder sb, String label, String value) {
        if (value == null || value.isBlank()) return;
        sb.append(label).append(": ").append(value.strip()).append('\n');
    }
}