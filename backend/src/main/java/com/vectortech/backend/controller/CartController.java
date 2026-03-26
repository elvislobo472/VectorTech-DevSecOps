package com.vectortech.backend.controller;

import com.vectortech.backend.dto.CartItemRequest;
import com.vectortech.backend.dto.CartResponse;
import com.vectortech.backend.dto.UpdateCartRequest;
import com.vectortech.backend.model.User;
import com.vectortech.backend.service.CartService;
import com.vectortech.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * GET /api/cart
     * Get the authenticated user's cart.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(user.getId())));
    }

    /**
     * POST /api/cart/add
     * Add a product to the cart.
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CartItemRequest request) {
        CartResponse cart = cartService.addToCart(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    /**
     * PUT /api/cart/update
     * Update the quantity of a cart item.
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateCartRequest request) {
        CartResponse cart = cartService.updateCartItem(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cart));
    }

    /**
     * DELETE /api/cart/remove/{productId}
     * Remove a product from the cart.
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        CartResponse cart = cartService.removeFromCart(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }
}
