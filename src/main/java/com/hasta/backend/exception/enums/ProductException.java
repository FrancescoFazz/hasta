package com.hasta.backend.exception.enums;

import com.hasta.backend.exception.ApplicationExceptionEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductException implements ApplicationExceptionEnum {
    NOT_FOUND("product.not-found", "Product not found", 404),
    ALREADY_EXISTS("product.already-exists", "Product already exists", 409);

    private final String code;
    private final String message;
    private final int httpStatusCode;
}
