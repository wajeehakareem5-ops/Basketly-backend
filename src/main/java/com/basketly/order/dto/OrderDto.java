package com.basketly.order.dto;

import com.basketly.order.domain.Order;
import com.basketly.order.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private UUID id;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFee;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String couponCode;
    private String paymentMethod;
    private String paymentStatus;
    private String shippingAddress;
    private LocalDate deliveryDate;
    private String deliverySlot;
    private UUID deliveryReservationId;
    private String instructions;
    private List<OrderItemDto> items;
    private Instant createdAt;

    public static OrderDto fromEntity(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .deliveryFee(order.getDeliveryFee())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .couponCode(order.getCouponCode())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .shippingAddress(order.getShippingAddressSnapshot())
                .deliveryDate(order.getDeliveryDate())
                .deliverySlot(order.getDeliverySlotSnapshot())
                .deliveryReservationId(order.getDeliveryReservationId())
                .instructions(order.getInstructions())
                .items(order.getItems() != null ? order.getItems().stream().map(OrderItemDto::fromEntity).toList() : List.of())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
