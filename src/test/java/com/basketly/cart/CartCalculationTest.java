package com.basketly.cart;

import com.basketly.cart.domain.CartItem;
import com.basketly.cart.dto.CartDto;
import com.basketly.cart.repository.CartItemRepository;
import com.basketly.cart.service.CartService;
import com.basketly.catalog.domain.Category;
import com.basketly.catalog.domain.Product;
import com.basketly.checkout.domain.Coupon;
import com.basketly.checkout.repository.CouponRepository;
import com.basketly.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartCalculationTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void testCartCalculationsWithDeliveryFeeAndGst() {
        User user = User.builder().id(UUID.randomUUID()).build();
        Category cat = Category.builder().id(UUID.randomUUID()).name("Produce").slug("produce").build();

        Product apples = Product.builder()
                .id(UUID.randomUUID())
                .name("Apples")
                .price(new BigDecimal("180.00"))
                .category(cat)
                .build();

        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .user(user)
                .product(apples)
                .quantity(2) // 360.00 subtotal
                .build();

        when(cartItemRepository.findByUserOrderByCreatedAtAsc(user)).thenReturn(List.of(item));

        CartDto cart = cartService.getCart(user, null);

        // Subtotal = 360.00
        assertEquals(new BigDecimal("360.00"), cart.getSubtotal());
        // Subtotal < 500 => Delivery fee = 40.00
        assertEquals(new BigDecimal("40.00"), cart.getDeliveryFee());
        // Tax = 5% of 360.00 = 18.00
        assertEquals(new BigDecimal("18.00"), cart.getTaxAmount());
        // Discount = 0.00
        assertEquals(new BigDecimal("0.00"), cart.getDiscountAmount());
        // Total = 360.00 + 40.00 + 18.00 = 418.00
        assertEquals(new BigDecimal("418.00"), cart.getTotalAmount());
    }

    @Test
    void testCouponApplicationAndFreeDeliveryThreshold() {
        User user = User.builder().id(UUID.randomUUID()).build();
        Category cat = Category.builder().id(UUID.randomUUID()).name("Dairy").slug("dairy").build();

        Product butter = Product.builder()
                .id(UUID.randomUUID())
                .name("Butter")
                .price(new BigDecimal("200.00"))
                .category(cat)
                .build();

        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .user(user)
                .product(butter)
                .quantity(3) // 600.00 subtotal
                .build();

        Coupon coupon = Coupon.builder()
                .code("SAVE10")
                .discountPercent(new BigDecimal("10.00"))
                .minOrderAmount(new BigDecimal("500.00"))
                .maxDiscountAmount(new BigDecimal("100.00"))
                .active(true)
                .build();

        when(cartItemRepository.findByUserOrderByCreatedAtAsc(user)).thenReturn(List.of(item));
        when(couponRepository.findByCodeIgnoreCaseAndActiveTrue("SAVE10")).thenReturn(Optional.of(coupon));

        CartDto cart = cartService.getCart(user, "SAVE10");

        // Subtotal = 600.00
        assertEquals(new BigDecimal("600.00"), cart.getSubtotal());
        // Subtotal >= 500 => Free delivery (0.00)
        assertEquals(new BigDecimal("0.00"), cart.getDeliveryFee());
        // Discount = 10% of 600.00 = 60.00
        assertEquals(new BigDecimal("60.00"), cart.getDiscountAmount());
        // Tax = 5% of 600.00 = 30.00
        assertEquals(new BigDecimal("30.00"), cart.getTaxAmount());
        // Total = 600.00 - 60.00 + 0.00 + 30.00 = 570.00
        assertEquals(new BigDecimal("570.00"), cart.getTotalAmount());
        assertEquals("SAVE10", cart.getAppliedCouponCode());
    }
}
