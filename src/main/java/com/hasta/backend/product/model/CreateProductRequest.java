package com.hasta.backend.product.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {

    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private Integer quantity;
    @Positive
    private BigDecimal price;
    @NotNull
    private Categories category;
    @NotNull
    private Long sellerId;
}
