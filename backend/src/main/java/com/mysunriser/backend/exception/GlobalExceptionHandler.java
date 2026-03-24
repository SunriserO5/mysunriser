package com.mysunriser.backend.exception;

import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ErrorResponse> handleBizException(BizException e) {
        HttpStatus status = mapHttpStatus(e.getCode());
        ErrorResponse body = ErrorResponse.of(e.getCode(), e.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    private HttpStatus mapHttpStatus(int code) {
        return switch (code) {
            case Codes.UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case Codes.FORBIDDEN -> HttpStatus.FORBIDDEN;
            case Codes.NOT_FOUND -> HttpStatus.NOT_FOUND;
            case Codes.TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS;
            case Codes.VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
