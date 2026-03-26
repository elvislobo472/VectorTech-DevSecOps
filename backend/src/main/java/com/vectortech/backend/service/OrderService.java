package com.vectortech.backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vectortech.backend.dto.OrderItemDto;
import com.vectortech.backend.dto.OrderResponse;
import com.vectortech.backend.exception.BadRequestException;
import com.vectortech.backend.exception.ResourceNotFoundException;
import com.vectortech.backend.model.Cart;
import com.vectortech.backend.model.CartItem;
import com.vectortech.backend.model.Order;
import com.vectortech.backend.model.OrderItem;
import com.vectortech.backend.model.Product;
import com.vectortech.backend.model.User;
import com.vectortech.backend.repository.CartRepository;
import com.vectortech.backend.repository.OrderRepository;
import com.vectortech.backend.repository.ProductRepository;
import com.vectortech.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse checkout(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot checkout with an empty cart");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate stock and compute total
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = Order.builder()
                .user(user)
                .totalAmount(BigDecimal.ZERO)
                .build();
        Order savedOrder = orderRepository.save(order);

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for product: " + product.getName());
            }

            // Deduct stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();
            orderItems.add(orderItem);
        }

        savedOrder.setTotalAmount(totalAmount);
        savedOrder.setItems(orderItems);
        orderRepository.save(savedOrder);

        // Clear the cart
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Order {} placed for user {}", savedOrder.getId(), userId);
        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistory(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return toResponse(order);
    }

    // ─── Mapper ──────────────────────────────────────────────────────────

    private OrderResponse toResponse(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(this::toItemDto)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderItemDto toItemDto(OrderItem item) {
        BigDecimal subtotal = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .imageUrl(item.getProduct().getImageUrl())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(subtotal)
                .build();
    }
}
