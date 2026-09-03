package com.ambulance.dispatch_system.common.exception;

import java.time.LocalDateTime;

/** The JSON body returned for any exception GlobalExceptionHandler handles. */
public record ErrorResponse(
        String errorCode,
        String message,
        int status,
        LocalDateTime timestamp
) {

    public static ErrorResponse of(BaseException ex) {
        return new ErrorResponse(ex.getErrorCode(), ex.getMessage(), ex.getHttpStatus().value(), LocalDateTime.now());
    }
}
