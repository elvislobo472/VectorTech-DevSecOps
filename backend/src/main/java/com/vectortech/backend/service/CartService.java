package com.vectortech.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vectortech.backend.dto.CartItemDto;
import com.vectortech.backend.dto.CartItemRequest;
import com.vectortech.backend.dto.CartResponse;
import com.vectortech.backend.dto.UpdateCartRequest;
import com.vectortech.backend.exception.BadRequestException;
import com.vectortech.backend.exception.ResourceNotFoundException;
import com.vectortech.backend.model.Cart;
import com.vectortech.backend.model.CartItem;
import com.vectortech.backend.model.Product;
import com.vectortech.backend.model.User;
import com.vectortech.backend.repository.CartItemRepository;
import com.vectortech.backend.repository.CartRepository;
import com.vectortech.backend.repository.ProductRepository;
import com.vectortech.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponse addToCart(Long userId, CartItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (!product.isInStock() || product.getStock() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }

        Cart cart = getOrCreateCart(userId);

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQty = item.getQuantity() + request.getQuantity();
            if (newQty > product.getStock()) {
                throw new BadRequestException("Cannot add more than available stock");
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }

        log.info("Product {} added to cart for user {}", product.getId(), userId);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItem(Long userId, UpdateCartRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "productId", request.getProductId()));

        Product product = item.getProduct();
        if (request.getQuantity() > product.getStock()) {
            throw new BadRequestException("Requested quantity exceeds stock");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        log.info("Cart item updated for user {}", userId);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        log.info("Product {} removed from cart for user {}", productId, userId);
        return buildCartResponse(cart);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            Cart newCart = Cart.builder().user(user).build();
            return cartRepository.save(newCart);
        });
    }

    private CartResponse buildCartResponse(Cart cart) {
        // Reload to get fresh items
        Cart freshCart = cartRepository.findGraphById(cart.getId()).orElse(cart);

        List<CartItemDto> itemDtos = freshCart.getItems().stream()
                .map(this::toItemDto)
                .toList();

        BigDecimal totalAmount = itemDtos.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = itemDtos.stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();

        return CartResponse.builder()
                .cartId(freshCart.getId())
                .items(itemDtos)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }

    private CartItemDto toItemDto(CartItem item) {
        BigDecimal subtotal = item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .imageUrl(item.getProduct().getImageUrl())
                .price(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}
