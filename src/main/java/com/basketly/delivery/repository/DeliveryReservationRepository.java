package com.basketly.delivery.repository;

import com.basketly.delivery.domain.DeliveryReservation;
import com.basketly.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryReservationRepository extends JpaRepository<DeliveryReservation, UUID> {
    List<DeliveryReservation> findByUserOrderByCreatedAtDesc(User user);
    Optional<DeliveryReservation> findByIdAndUser(UUID id, User user);
    Optional<DeliveryReservation> findTopByUserAndStatusOrderByCreatedAtDesc(User user, String status);
}
