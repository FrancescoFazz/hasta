package com.hasta.backend.product.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProductRequest {

    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private Integer quantity;
    @NotNull
    private Categories category;
    @NotNull
    private Long sellerId;
}
