package com.didimdol.global.exception;

import com.didimdol.global.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외
    @ExceptionHandler (BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException e
    ) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("BusinessException: code={}, message={}",
                errorCode.name(), errorCode.getMessage()
        );
        return ResponseEntity.status(errorCode.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail(errorCode.getMessage()));
    }

    // Valid 요청 DTO 검증 실패 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        log.warn("Validation failed - field: {}, reason: {}",
                (fieldError != null) ? fieldError.getField() : "unknown",
                (fieldError != null) ? fieldError.getDefaultMessage() : "unknown");
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail("잘못된 요청 값입니다."));
    }

    // @RequestParam, @PathVariable 등의 메서드 검증 실패
    @ExceptionHandler (HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidationException(
            HandlerMethodValidationException e
    ) {
        log.warn("Handler method validation failed: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail("잘못된 요청 값입니다."));
    }

    // @Validated 기반 constraint violation
    @ExceptionHandler (ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException e
    ) {
        String message = e.getConstraintViolations().stream()
                        .findFirst().map(ConstraintViolation::getMessage)
                        .orElse("잘못된 요청 값입니다.");
        log.warn("Validation failed - reason: {}", message);
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail(message));
    }

    // 존재하지 않는 경로 → 404 (500으로 덮이지 않게)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail("존재하지 않는 API 경로입니다."));
    }

    // 지원하지 않는 HTTP 메서드 → 405
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail("지원하지 않는 HTTP 메서드입니다."));
    }

    // 유니크 제약 경합 (동시에 같은 회기/메시지 번호 생성 등) → 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolation: {}", e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(ErrorCode.DUPLICATE_REQUEST.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail(ErrorCode.DUPLICATE_REQUEST.getMessage()));
    }

    // SSE 클라이언트가 먼저 끊긴 경우 (탭 닫기 등): 정상 상황이므로 응답을 쓰지 않고 조용히 넘어간다
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleClientDisconnected(AsyncRequestNotUsableException e) {
        log.debug("SSE client disconnected: {}", e.getMessage());
    }

    // 예상치 못한 서버 오류
    @ExceptionHandler (Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail("서버 내부 오류가 발생했습니다."));
    }

}
