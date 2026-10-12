package com.basketly.order;

import com.basketly.cart.repository.CartItemRepository;
import com.basketly.common.exception.BadRequestException;
import com.basketly.delivery.domain.DeliveryReservation;
import com.basketly.delivery.domain.DeliverySlot;
import com.basketly.delivery.repository.DeliveryReservationRepository;
import com.basketly.delivery.repository.DeliverySlotRepository;
import com.basketly.notification.service.NotificationService;
import com.basketly.order.domain.Order;
import com.basketly.order.domain.OrderStatus;
import com.basketly.order.dto.OrderDto;
import com.basketly.order.repository.OrderRepository;
import com.basketly.order.service.OrderService;
import com.basketly.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private DeliveryReservationRepository deliveryReservationRepository;

    @Mock
    private DeliverySlotRepository deliverySlotRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void cancelOrder_SuccessfullyCancelsAndReleasesCapacity() {
        User user = User.builder().id(UUID.randomUUID()).build();
        UUID orderId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        Order order = Order.builder()
                .id(orderId)
                .orderNumber("BSK-12345")
                .user(user)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("450.00"))
                .deliveryReservationId(reservationId)
                .deliveryDate(LocalDate.now().plusDays(1))
                .build();

        DeliverySlot slot = DeliverySlot.builder().id(slotId).build();
        DeliveryReservation reservation = DeliveryReservation.builder()
                .id(reservationId)
                .slot(slot)
                .status("CONFIRMED")
                .build();

        when(orderRepository.findByIdAndUser(orderId, user)).thenReturn(Optional.of(order));
        when(deliveryReservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(orderRepository.save(order)).thenReturn(order);

        OrderDto result = orderService.cancelOrder(user, orderId);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(deliverySlotRepository).decrementBookedCount(slotId);
        verify(notificationService).createNotification(eq(user), anyString(), anyString(), eq("ORDER"), anyString());
    }

    @Test
    void cancelOrder_DeliveredOrder_ThrowsBadRequestException() {
        User user = User.builder().id(UUID.randomUUID()).build();
        UUID orderId = UUID.randomUUID();

        Order order = Order.builder()
                .id(orderId)
                .user(user)
                .status(OrderStatus.DELIVERED)
                .build();

        when(orderRepository.findByIdAndUser(orderId, user)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.cancelOrder(user, orderId));
    }
}
