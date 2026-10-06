package com.didimdol.domain.message.ai;

import com.didimdol.domain.message.enums.Emotion;
import com.didimdol.domain.message.enums.NotableCue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplyTagParser {

    private static final int MAX_HEADER = 80;
    private static final Pattern EMOTION = Pattern.compile("EMOTION\\s*:\\s*(\\w+)");
    private static final Pattern CUE = Pattern.compile("CUE\\s*:\\s*(\\w+)");

    private final StringBuilder header = new StringBuilder();
    private boolean headerDone = false;
    private boolean bodyStarted = false;
    private Emotion emotion = Emotion.NEUTRAL;
    private NotableCue cue = null;

    /** 토큰 하나를 넣으면, 클라이언트에 흘려보낼 본문 조각을 돌려준다 (없으면 빈 문자열) */
    public String feed(String token) {
        if (headerDone) {
            return body(token);
        }
        header.append(token);
        String trimmed = header.toString().stripLeading();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (trimmed.charAt(0) != '[') {
            return flushAsBody();
        }
        int end = trimmed.indexOf(']');
        if (end >= 0) {
            parseHeader(trimmed.substring(1, end));
            headerDone = true;
            return body(trimmed.substring(end + 1));
        }
        if (trimmed.length() > MAX_HEADER) {
            return flushAsBody();
        }
        return "";
    }

    /** 스트림이 끝났는데 헤더 판단이 안 끝났다면 남은 걸 본문으로 처리 */
    public String finish() {
        return headerDone ? "" : flushAsBody();
    }

    public Emotion emotion() { return emotion; }
    public NotableCue cue() { return cue; }

    private String flushAsBody() {
        headerDone = true;
        String text = header.toString();
        header.setLength(0);
        return body(text);
    }

    private String body(String text) {
        if (bodyStarted) {
            return text;
        }
        String stripped = text.stripLeading();
        if (!stripped.isEmpty()) {
            bodyStarted = true;
        }
        return stripped;
    }

    private void parseHeader(String inside) {
        Matcher e = EMOTION.matcher(inside);
        if (e.find()) {
            try {
                emotion = Emotion.valueOf(e.group(1).toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // NEUTRAL 유지
            }
        }
        Matcher c = CUE.matcher(inside);
        if (c.find()) {
            try {
                cue = NotableCue.valueOf(c.group(1).toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // NONE 포함, 그 외 값은 null
            }
        }
    }
}