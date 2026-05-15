package com.hasta.backend.exception;

import lombok.Getter;

@Getter
public enum ProductException implements ApplicationExceptionEnum{
    NOT_FOUND("product.not-found", "Product not found", 404);
    private final String code;
    private final String message;
    private final int status;
    ProductException(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public int getHttpStatusCode() { return httpStatusCode; }
}
