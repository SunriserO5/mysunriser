package com.mysunriser.backend.exception;

import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? "validation error" : fieldError.getDefaultMessage();
        ErrorResponse body = ErrorResponse.of(Codes.VALIDATION_ERROR, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
        ErrorResponse body = ErrorResponse.of(Codes.INTERNAL_ERROR, "internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
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
