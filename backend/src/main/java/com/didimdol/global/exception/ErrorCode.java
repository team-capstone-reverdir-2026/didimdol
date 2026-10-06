package com.didimdol.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "내담자를 찾을 수 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청 값입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    COUNSEL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상담을 찾을 수 없습니다."),
    COUNSEL_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 모든 회기가 종료된 상담입니다."),
    PREVIOUS_SESSION_NOT_COMPLETED(HttpStatus.CONFLICT, "이전 회기가 아직 종료되지 않았습니다."),
    SESSION_NOT_IN_PROGRESS(HttpStatus.CONFLICT, "이미 종료된 회기입니다."),
    CLIENT_RESPONDING(HttpStatus.CONFLICT, "내담자가 응답 중입니다. 잠시 후 다시 시도해주세요."),;

    private final HttpStatus status;
    private final String message;
}
