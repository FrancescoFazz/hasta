package com.hasta.backend.exception.enums;

import com.hasta.backend.exception.ApplicationExceptionEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuctionException implements ApplicationExceptionEnum {
    NOT_FOUND("auction.not-found", "Auction not found", 404),
    ALREADY_CLOSED("auction.already-closed", "Auction is already closed", 400),
    PAST_END_TIME("auction.past-end-time", "Auction end time must be in the future", 400);

    private final String code;
    private final String message;
    private final int httpStatusCode;

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public int getHttpStatusCode() { return httpStatusCode; }
}