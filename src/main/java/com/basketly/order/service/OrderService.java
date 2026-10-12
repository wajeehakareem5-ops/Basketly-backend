package com.basketly.order.service;

import com.basketly.cart.domain.CartItem;
import com.basketly.cart.repository.CartItemRepository;
import com.basketly.common.exception.BadRequestException;
import com.basketly.common.exception.ResourceNotFoundException;
import com.basketly.delivery.domain.DeliveryReservation;
import com.basketly.delivery.repository.DeliveryReservationRepository;
import com.basketly.delivery.repository.DeliverySlotRepository;
import com.basketly.notification.service.NotificationService;
import com.basketly.order.domain.Order;
import com.basketly.order.domain.OrderItem;
import com.basketly.order.domain.OrderStatus;
import com.basketly.order.dto.OrderDto;
import com.basketly.order.repository.OrderRepository;
import com.basketly.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final DeliveryReservationRepository deliveryReservationRepository;
    private final DeliverySlotRepository deliverySlotRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrders(User user, OrderStatus status, Pageable pageable) {
        Page<Order> orders = (status != null)
                ? orderRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status, pageable)
                : orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return orders.map(OrderDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(User user, UUID id) {
        Order order = orderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        return OrderDto.fromEntity(order);
    }

    @Transactional
    public OrderDto cancelOrder(User user, UUID id) {
        Order order = orderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Delivered orders cannot be cancelled.");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled.");
        }
        if (order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
            throw new BadRequestException("Order is out for delivery and cannot be cancelled.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        // Release reserved delivery capacity
        if (order.getDeliveryReservationId() != null) {
            deliveryReservationRepository.findById(order.getDeliveryReservationId()).ifPresent(res -> {
                res.setStatus("CANCELLED");
                deliveryReservationRepository.save(res);
                deliverySlotRepository.decrementBookedCount(res.getSlot().getId());
            });
        }

        notificationService.createNotification(
                user,
                "Order Cancelled: " + order.getOrderNumber(),
                "Your order #" + order.getOrderNumber() + " has been cancelled. Any pre-booked delivery slots have been released.",
                "ORDER",
                "/orders/" + order.getId()
        );

        return OrderDto.fromEntity(order);
    }

    @Transactional
    public void reorder(User user, UUID id) {
        Order order = orderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        for (OrderItem item : order.getItems()) {
            if (item.getProduct().isAvailable()) {
                CartItem cartItem = cartItemRepository.findByUserAndProduct(user, item.getProduct())
                        .orElseGet(() -> CartItem.builder()
                                .user(user)
                                .product(item.getProduct())
                                .quantity(0)
                                .build());

                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                cartItemRepository.save(cartItem);
            }
        }
    }

    @Transactional
    public OrderDto updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(newStatus);
        order = orderRepository.save(order);

        notificationService.createNotification(
                order.getUser(),
                "Order Status Updated: " + order.getOrderNumber(),
                "Your order status is now: " + newStatus.name().replace('_', ' '),
                "ORDER",
                "/orders/" + order.getId()
        );

        return OrderDto.fromEntity(order);
    }
}
