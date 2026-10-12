package com.basketly.catalog.dto;

import com.basketly.catalog.domain.Product;
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
public class ProductDto {
    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private String categorySlug;
    private String name;
    private String description;
    private BigDecimal price;
    private String packageSize;
    private String unit;
    private String imageUrl;
    private boolean isAvailable;
    private int stockQuantity;
    private boolean isFavorite;

    public static ProductDto fromEntity(Product product) {
        return fromEntity(product, false);
    }

    public static ProductDto fromEntity(Product product, boolean isFavorite) {
        return ProductDto.builder()
                .id(product.getId())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categorySlug(product.getCategory() != null ? product.getCategory().getSlug() : null)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .packageSize(product.getPackageSize())
                .unit(product.getUnit())
                .imageUrl(product.getImageUrl())
                .isAvailable(product.isAvailable())
                .stockQuantity(product.getStockQuantity())
                .isFavorite(isFavorite)
                .build();
    }
}
