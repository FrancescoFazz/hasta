package com.hasta.backend.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ApplicationException extends RuntimeException {
    private final String code;
    private final String message;
    private final int httpStatusCode;

    private final long timestamp = System.currentTimeMillis();

    public ApplicationException(ApplicationExceptionEnum exceptionEnum) {

        this.code = exceptionEnum.getCode();
        this.message = exceptionEnum.getMessage();
        this.httpStatusCode = exceptionEnum.getHttpStatusCode();
    }
}
