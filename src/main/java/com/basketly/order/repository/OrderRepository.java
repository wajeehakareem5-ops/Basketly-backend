package com.basketly.order.repository;

import com.basketly.order.domain.Order;
import com.basketly.order.domain.OrderStatus;
import com.basketly.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status, Pageable pageable);

    Optional<Order> findByIdAndUser(UUID id, User user);

    Optional<Order> findByOrderNumberAndUser(String orderNumber, User user);

    List<Order> findTop5ByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.status IN ('CONFIRMED', 'PREPARING', 'READY_FOR_DELIVERY', 'OUT_FOR_DELIVERY') AND o.deliveryDate >= :today ORDER BY o.deliveryDate ASC")
    List<Order> findUpcomingDeliveries(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.status = :status AND o.createdAt >= :since")
    List<Order> findByUserAndStatusAndCreatedAtAfter(@Param("user") User user, @Param("status") OrderStatus status, @Param("since") Instant since);

    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.createdAt >= :since")
    List<Order> findByUserAndCreatedAtAfter(@Param("user") User user, @Param("since") Instant since);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o")
    long countTotalOrders();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != 'CANCELLED'")
    java.math.BigDecimal sumTotalRevenue();

    long countByStatusIn(List<OrderStatus> statuses);
}
