package com.vectortech.backend.controller;

import com.vectortech.backend.dto.OrderResponse;
import com.vectortech.backend.model.User;
import com.vectortech.backend.service.OrderService;
import com.vectortech.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders/checkout
     * Create an order from the current cart.
     */
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @AuthenticationPrincipal User user) {
        OrderResponse order = orderService.checkout(user.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }

    /**
     * GET /api/orders
     * Get the authenticated user's order history.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrderHistory(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderHistory(user.getId())));
    }

    /**
     * GET /api/orders/{id}
     * Get a specific order by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id, user.getId())));
    }
}
