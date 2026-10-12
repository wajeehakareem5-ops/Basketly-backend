package com.basketly.checkout.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    @NotNull(message = "Delivery address ID is required")
    private UUID addressId;

    private UUID reservationId;

    private UUID deliverySlotId;

    private String couponCode;

    @Builder.Default
    private String paymentMethod = "COD";

    private String deliveryInstructions;

    private String notes;
}
