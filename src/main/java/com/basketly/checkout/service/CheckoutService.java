package com.basketly.checkout.service;

import com.basketly.address.domain.Address;
import com.basketly.address.repository.AddressRepository;
import com.basketly.cart.domain.CartItem;
import com.basketly.cart.repository.CartItemRepository;
import com.basketly.catalog.domain.Product;
import com.basketly.checkout.domain.Coupon;
import com.basketly.checkout.domain.IdempotencyKey;
import com.basketly.checkout.dto.CheckoutRequest;
import com.basketly.checkout.repository.CouponRepository;
import com.basketly.checkout.repository.IdempotencyKeyRepository;
import com.basketly.common.exception.BadRequestException;
import com.basketly.common.exception.ResourceNotFoundException;
import com.basketly.delivery.domain.DeliveryReservation;
import com.basketly.delivery.repository.DeliveryReservationRepository;
import com.basketly.notification.service.NotificationService;
import com.basketly.order.domain.Order;
import com.basketly.order.domain.OrderItem;
import com.basketly.order.domain.OrderStatus;
import com.basketly.order.dto.OrderDto;
import com.basketly.order.repository.OrderItemRepository;
import com.basketly.order.repository.OrderRepository;
import com.basketly.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final DeliveryReservationRepository deliveryReservationRepository;
    private final com.basketly.delivery.service.DeliveryService deliveryService;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final NotificationService notificationService;

    private final SecureRandom secureRandom = new SecureRandom();

    private String generateOrderNumber() {
        int randomSuffix = 1000 + secureRandom.nextInt(9000);
        return "BSK-" + System.currentTimeMillis() % 100000000L + "-" + randomSuffix;
    }

    @Transactional
    public OrderDto checkout(User user, CheckoutRequest request, String idempotencyKeyHeader) {
        // Idempotency check: If key already processed, return existing order
        if (idempotencyKeyHeader != null && !idempotencyKeyHeader.isBlank()) {
            Optional<IdempotencyKey> existing = idempotencyKeyRepository.findByUserAndKey(user, idempotencyKeyHeader.trim());
            if (existing.isPresent() && existing.get().getOrder() != null) {
                return OrderDto.fromEntity(existing.get().getOrder());
            }
        }

        List<CartItem> cartItems = cartItemRepository.findByUserOrderByCreatedAtAsc(user);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Your cart is empty. Please add items before checking out.");
        }

        // Validate products and recalculate authoritative subtotal
        BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (CartItem item : cartItems) {
            Product p = item.getProduct();
            if (!p.isAvailable()) {
                throw new BadRequestException("Item is no longer available: " + p.getName());
            }
            BigDecimal lineTotal = p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineTotal);
        }

        // Validate address
        Address address = addressRepository.findByIdAndUser(request.getAddressId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Selected delivery address not found"));

        String addressSnapshot = String.format("%s, Phone: %s | %s, %s, %s - %s (%s)",
                address.getRecipientName(),
                address.getPhone(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getLabel());

        // Validate delivery reservation or reserve slot
        DeliveryReservation reservation;
        if (request.getReservationId() != null) {
            reservation = deliveryReservationRepository.findByIdAndUser(request.getReservationId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Selected delivery reservation not found"));
        } else if (request.getDeliverySlotId() != null) {
            String note = request.getDeliveryInstructions() != null ? request.getDeliveryInstructions() : request.getNotes();
            reservation = deliveryService.reserveSlotEntity(user, request.getDeliverySlotId(), note);
        } else {
            throw new BadRequestException("Either reservationId or deliverySlotId must be provided");
        }

        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new BadRequestException("The selected delivery slot reservation has been cancelled");
        }

        LocalDate deliveryDate = reservation.getSlot().getSlotDate();
        String slotSnapshot = String.format("%s (%s - %s)",
                deliveryDate,
                reservation.getSlot().getStartTime(),
                reservation.getSlot().getEndTime());

        // Calculate coupon discount
        BigDecimal discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        String appliedCouponCode = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCaseAndActiveTrue(request.getCouponCode().trim());
            if (couponOpt.isPresent()) {
                Coupon c = couponOpt.get();
                if (subtotal.compareTo(c.getMinOrderAmount()) >= 0) {
                    appliedCouponCode = c.getCode();
                    if (c.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal calculated = subtotal.multiply(c.getDiscountPercent())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        if (c.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0 && calculated.compareTo(c.getMaxDiscountAmount()) > 0) {
                            discount = c.getMaxDiscountAmount();
                        } else {
                            discount = calculated;
                        }
                    } else if (c.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                        discount = c.getDiscountAmount().min(subtotal);
                    }
                }
            }
        }

        // Delivery fee: 0 if subtotal >= 500, else 40
        BigDecimal deliveryFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (subtotal.compareTo(new BigDecimal("500.00")) < 0) {
            deliveryFee = new BigDecimal("40.00");
        }

        // Tax: 5% GST
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.subtract(discount).add(deliveryFee).add(tax)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        // Create Order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .status(OrderStatus.CONFIRMED)
                .subtotal(subtotal)
                .discountAmount(discount)
                .deliveryFee(deliveryFee)
                .taxAmount(tax)
                .totalAmount(total)
                .couponCode(appliedCouponCode)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "COD")
                .paymentStatus("CONFIRMED")
                .shippingAddressSnapshot(addressSnapshot)
                .deliveryDate(deliveryDate)
                .deliverySlotSnapshot(slotSnapshot)
                .deliveryReservationId(reservation.getId())
                .instructions(request.getDeliveryInstructions())
                .items(new ArrayList<>())
                .build();

        order = orderRepository.save(order);

        // Create line items with snapshots
        for (CartItem ci : cartItems) {
            Product p = ci.getProduct();
            BigDecimal lineTotal = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())).setScale(2, RoundingMode.HALF_UP);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .productNameSnapshot(p.getName())
                    .packageSizeSnapshot(p.getPackageSize())
                    .unitSnapshot(p.getUnit())
                    .unitPriceSnapshot(p.getPrice())
                    .quantity(ci.getQuantity())
                    .lineTotal(lineTotal)
                    .build();

            orderItem = orderItemRepository.save(orderItem);
            order.getItems().add(orderItem);
        }

        // Save idempotency key if provided
        if (idempotencyKeyHeader != null && !idempotencyKeyHeader.isBlank()) {
            IdempotencyKey keyRecord = IdempotencyKey.builder()
                    .user(user)
                    .key(idempotencyKeyHeader.trim())
                    .order(order)
                    .build();
            idempotencyKeyRepository.save(keyRecord);
        }

        // Mark reservation as confirmed
        reservation.setStatus("CONFIRMED");
        deliveryReservationRepository.save(reservation);

        // Clear cart only after successful order placement
        cartItemRepository.deleteByUser(user);

        // Trigger notification
        notificationService.createNotification(
                user,
                "Order Confirmed: " + order.getOrderNumber(),
                "Your grocery order for ₹" + order.getTotalAmount() + " is confirmed and scheduled for delivery on " + order.getDeliveryDate() + ".",
                "ORDER",
                "/orders/" + order.getId()
        );

        return OrderDto.fromEntity(order);
    }
}
