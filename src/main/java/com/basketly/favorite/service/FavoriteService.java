package com.basketly.favorite.service;

import com.basketly.catalog.domain.Product;
import com.basketly.catalog.dto.ProductDto;
import com.basketly.catalog.repository.ProductRepository;
import com.basketly.common.exception.ConflictException;
import com.basketly.common.exception.ResourceNotFoundException;
import com.basketly.favorite.domain.Favorite;
import com.basketly.favorite.repository.FavoriteRepository;
import com.basketly.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductDto> getFavorites(User user) {
        return favoriteRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(f -> ProductDto.fromEntity(f.getProduct(), true))
                .toList();
    }

    @Transactional
    public void addFavorite(User user, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (favoriteRepository.existsByUserAndProduct(user, product)) {
            return; // Idempotent add
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .product(product)
                .build();
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(User user, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        favoriteRepository.deleteByUserAndProduct(user, product);
    }
}
