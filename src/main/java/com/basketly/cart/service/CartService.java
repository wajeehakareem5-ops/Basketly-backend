package com.basketly.cart.service;

import com.basketly.cart.domain.CartItem;
import com.basketly.cart.dto.AddToCartRequest;
import com.basketly.cart.dto.CartDto;
import com.basketly.cart.dto.CartItemDto;
import com.basketly.cart.repository.CartItemRepository;
import com.basketly.catalog.domain.Product;
import com.basketly.catalog.repository.ProductRepository;
import com.basketly.checkout.domain.Coupon;
import com.basketly.checkout.repository.CouponRepository;
import com.basketly.common.exception.BadRequestException;
import com.basketly.common.exception.ResourceNotFoundException;
import com.basketly.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public CartDto getCart(User user, String couponCode) {
        List<CartItem> items = cartItemRepository.findByUserOrderByCreatedAtAsc(user);
        List<CartItemDto> itemDtos = items.stream()
                .map(CartItemDto::fromEntity)
                .toList();

        BigDecimal subtotal = itemDtos.stream()
                .map(CartItemDto::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        String appliedCoupon = null;

        if (couponCode != null && !couponCode.isBlank()) {
            Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCaseAndActiveTrue(couponCode.trim());
            if (couponOpt.isPresent()) {
                Coupon coupon = couponOpt.get();
                if (subtotal.compareTo(coupon.getMinOrderAmount()) >= 0) {
                    appliedCoupon = coupon.getCode();
                    if (coupon.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal calculated = subtotal.multiply(coupon.getDiscountPercent())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        if (coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0 && calculated.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                            discount = coupon.getMaxDiscountAmount();
                        } else {
                            discount = calculated;
                        }
                    } else if (coupon.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                        discount = coupon.getDiscountAmount().min(subtotal);
                    }
                }
            }
        }

        // Delivery fee: Free delivery over 500 INR, else 40 INR
        BigDecimal deliveryFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (subtotal.compareTo(BigDecimal.ZERO) > 0 && subtotal.compareTo(new BigDecimal("500.00")) < 0) {
            deliveryFee = new BigDecimal("40.00");
        }

        // Tax: 5% GST
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.subtract(discount).add(deliveryFee).add(tax)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        int totalCount = itemDtos.stream().mapToInt(CartItemDto::getQuantity).sum();

        return CartDto.builder()
                .items(itemDtos)
                .totalItemCount(totalCount)
                .subtotal(subtotal)
                .discountAmount(discount)
                .deliveryFee(deliveryFee)
                .taxAmount(tax)
                .totalAmount(total)
                .appliedCouponCode(appliedCoupon)
                .build();
    }

    @Transactional
    public CartDto addItem(User user, AddToCartRequest request, String couponCode) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        if (!product.isAvailable()) {
            throw new BadRequestException("Product is currently unavailable: " + product.getName());
        }

        CartItem item = cartItemRepository.findByUserAndProduct(user, product)
                .orElseGet(() -> CartItem.builder()
                        .user(user)
                        .product(product)
                        .quantity(0)
                        .build());

        item.setQuantity(item.getQuantity() + request.getQuantity());
        cartItemRepository.save(item);

        return getCart(user, couponCode);
    }

    @Transactional
    public CartDto updateItemQuantity(User user, UUID itemId, int quantity, String couponCode) {
        CartItem item = cartItemRepository.findByIdAndUser(itemId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return getCart(user, couponCode);
    }

    @Transactional
    public CartDto removeItem(User user, UUID itemId, String couponCode) {
        CartItem item = cartItemRepository.findByIdAndUser(itemId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        cartItemRepository.delete(item);
        return getCart(user, couponCode);
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }
}
