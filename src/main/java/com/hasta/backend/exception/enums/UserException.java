package com.hasta.backend.exception.enums;

import com.hasta.backend.exception.ApplicationExceptionEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserException implements ApplicationExceptionEnum {
    ALREADY_EXISTS("user.already-exists", "User already exists", 409),
    NOT_FOUND("user.not-found", "User not found", 404);

    private final String code;
    private final String message;
    private final int httpStatusCode;

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public int getHttpStatusCode() { return httpStatusCode; }
}