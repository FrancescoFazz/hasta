package com.hasta.backend.auction.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAuctionRequest {
    @NotNull
    private BigDecimal startingPrice;

    @NotNull
    private Integer quantitySold;

    @NotNull
    private Long sellerId;

    @NotNull
    private Long productId;

    @NotNull
    private java.time.Instant endTime;
}
