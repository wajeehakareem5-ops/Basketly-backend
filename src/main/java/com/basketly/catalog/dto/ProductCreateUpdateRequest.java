package com.basketly.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateUpdateRequest {

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @NotBlank(message = "Package size is required (e.g. 500 g, 1 kg)")
    private String packageSize;

    @NotBlank(message = "Unit is required (e.g. pack, kg, pcs)")
    private String unit;

    private String imageUrl;

    @Builder.Default
    private boolean isAvailable = true;

    @Builder.Default
    private int stockQuantity = 100;
}
