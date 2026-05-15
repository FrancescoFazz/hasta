package com.hasta.backend.exception;

public interface ApplicationExceptionEnum {
    String getCode();
    String getMessage();
    int getHttpStatusCode();
}
