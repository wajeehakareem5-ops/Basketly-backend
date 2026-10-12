package com.basketly.favorite.repository;

import com.basketly.catalog.domain.Product;
import com.basketly.favorite.domain.Favorite;
import com.basketly.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByUserOrderByCreatedAtDesc(User user);
    Optional<Favorite> findByUserAndProduct(User user, Product product);
    boolean existsByUserAndProduct(User user, Product product);
    void deleteByUserAndProduct(User user, Product product);

    @Query("SELECT f.product.id FROM Favorite f WHERE f.user = :user")
    Set<UUID> findFavoriteProductIdsByUser(@Param("user") User user);
}
