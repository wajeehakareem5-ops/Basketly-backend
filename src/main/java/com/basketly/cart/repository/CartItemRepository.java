package com.basketly.cart.repository;

import com.basketly.cart.domain.CartItem;
import com.basketly.catalog.domain.Product;
import com.basketly.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByUserOrderByCreatedAtAsc(User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    Optional<CartItem> findByIdAndUser(UUID id, User user);
    void deleteByUser(User user);
}
