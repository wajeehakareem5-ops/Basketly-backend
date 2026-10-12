package com.basketly.order.dto;

import com.basketly.order.domain.OrderItem;
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
public class OrderItemDto {
    private UUID id;
    private UUID productId;
    private String productName;
    private String packageSize;
    private String unit;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;

    public static OrderItemDto fromEntity(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProductNameSnapshot())
                .packageSize(item.getPackageSizeSnapshot())
                .unit(item.getUnitSnapshot())
                .unitPrice(item.getUnitPriceSnapshot())
                .quantity(item.getQuantity())
                .lineTotal(item.getLineTotal())
                .build();
    }
}
