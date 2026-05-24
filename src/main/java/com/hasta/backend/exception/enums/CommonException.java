package com.hasta.backend.exception.enums;

import com.hasta.backend.exception.ApplicationExceptionEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonException implements ApplicationExceptionEnum {

    FORBIDDEN("common.forbidden", "You do not have permission to perform this action", 403),
    VALIDATION_ERROR("common.validation-error", "Validation error", 400),
    INTERNAL_SERVER_ERROR("common.internal-server-error", "Internal server error", 500);

    private final String code;
    private final String message;
    private final int httpStatusCode;
}
