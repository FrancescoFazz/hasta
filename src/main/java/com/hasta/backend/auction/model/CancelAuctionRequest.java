package com.hasta.backend.auction.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancelAuctionRequest {
    @NotNull
    private Long sellerId;
}