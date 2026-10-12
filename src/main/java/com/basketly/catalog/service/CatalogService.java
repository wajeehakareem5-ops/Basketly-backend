package com.basketly.catalog.service;

import com.basketly.catalog.domain.Category;
import com.basketly.catalog.domain.Product;
import com.basketly.catalog.dto.CategoryDto;
import com.basketly.catalog.dto.ProductCreateUpdateRequest;
import com.basketly.catalog.dto.ProductDto;
import com.basketly.catalog.repository.CategoryRepository;
import com.basketly.catalog.repository.ProductRepository;
import com.basketly.common.exception.ResourceNotFoundException;
import com.basketly.favorite.repository.FavoriteRepository;
import com.basketly.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final FavoriteRepository favoriteRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(CategoryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(
            String categorySlug,
            String query,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            boolean availableOnly,
            Pageable pageable,
            User currentUser) {

        Category category = null;
        if (categorySlug != null && !categorySlug.isBlank()) {
            category = categoryRepository.findBySlug(categorySlug).orElse(null);
        }

        Page<Product> products = productRepository.searchProducts(
                category,
                (query != null && !query.isBlank()) ? query.trim() : null,
                minPrice,
                maxPrice,
                availableOnly,
                pageable
        );

        Set<UUID> favoriteIds = currentUser != null
                ? favoriteRepository.findFavoriteProductIdsByUser(currentUser)
                : Collections.emptySet();

        return products.map(p -> ProductDto.fromEntity(p, favoriteIds.contains(p.getId())));
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID id, User currentUser) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        boolean isFavorite = currentUser != null && favoriteRepository.existsByUserAndProduct(currentUser, product);
        return ProductDto.fromEntity(product, isFavorite);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getRelatedProducts(UUID productId, User currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Set<UUID> favoriteIds = currentUser != null
                ? favoriteRepository.findFavoriteProductIdsByUser(currentUser)
                : Collections.emptySet();

        return productRepository.findTop8ByCategoryAndIdNotAndAvailableTrue(product.getCategory(), productId).stream()
                .map(p -> ProductDto.fromEntity(p, favoriteIds.contains(p.getId())))
                .toList();
    }

    @Transactional
    public ProductDto createProduct(ProductCreateUpdateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = Product.builder()
                .category(category)
                .name(request.getName().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .packageSize(request.getPackageSize().trim())
                .unit(request.getUnit().trim())
                .imageUrl(request.getImageUrl())
                .available(request.isAvailable())
                .stockQuantity(request.getStockQuantity())
                .build();

        product = productRepository.save(product);
        return ProductDto.fromEntity(product);
    }

    @Transactional
    public ProductDto updateProduct(UUID id, ProductCreateUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        product.setCategory(category);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setPackageSize(request.getPackageSize().trim());
        product.setUnit(request.getUnit().trim());
        product.setImageUrl(request.getImageUrl());
        product.setAvailable(request.isAvailable());
        product.setStockQuantity(request.getStockQuantity());

        product = productRepository.save(product);
        return ProductDto.fromEntity(product);
    }

    @Transactional
    public ProductDto updateAvailability(UUID id, boolean isAvailable) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setAvailable(isAvailable);
        product = productRepository.save(product);
        return ProductDto.fromEntity(product);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }
}
