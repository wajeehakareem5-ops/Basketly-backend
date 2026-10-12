package com.basketly.catalog.repository;

import com.basketly.catalog.domain.Category;
import com.basketly.catalog.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.normalizedName) LIKE LOWER(CONCAT(:query, '%'))) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:availableOnly = false OR p.available = true)")
    Page<Product> searchProducts(
            @Param("category") Category category,
            @Param("query") String query,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("availableOnly") boolean availableOnly,
            Pageable pageable
    );

    List<Product> findTop8ByCategoryAndIdNotAndAvailableTrue(Category category, UUID productId);

    List<Product> findTop10ByAvailableTrueOrderByCreatedAtDesc();

    long countByAvailableTrue();

    long countByStockQuantityLessThanEqual(int stock);
}
