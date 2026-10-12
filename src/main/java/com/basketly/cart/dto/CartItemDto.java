package com.basketly.cart.dto;

import com.basketly.cart.domain.CartItem;
import com.basketly.catalog.dto.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private UUID id;
    private ProductDto product;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public static CartItemDto fromEntity(CartItem item) {
        BigDecimal unitPrice = item.getProduct().getPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);

        return CartItemDto.builder()
                .id(item.getId())
                .product(ProductDto.fromEntity(item.getProduct()))
                .quantity(item.getQuantity())
                .unitPrice(unitPrice)
                .lineTotal(lineTotal)
                .build();
    }
}
