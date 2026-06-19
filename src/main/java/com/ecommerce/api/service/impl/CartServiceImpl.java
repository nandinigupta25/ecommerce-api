package com.ecommerce.api.service.impl;

import com.ecommerce.api.dto.request.AddToCartRequest;
import com.ecommerce.api.dto.request.UpdateCartItemRequest;
import com.ecommerce.api.dto.response.CartItemResponse;
import com.ecommerce.api.dto.response.CartResponse;
import com.ecommerce.api.entity.*;
import com.ecommerce.api.exception.BadRequestException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.CartItemRepository;
import com.ecommerce.api.repository.CartRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of CartService for managing user shopping carts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public CartResponse getCart(String username) {
        Cart cart = getOrCreateCart(username);
        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(String username, AddToCartRequest request) {
        Cart cart = getOrCreateCart(username);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        // Guard: product must be active
        if (!product.isActive()) {
            throw new BadRequestException("Product '" + product.getName() + "' is not available");
        }

        // Guard: sufficient stock
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        // If item already in cart → increase quantity; otherwise create new CartItem
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQty = item.getQuantity() + request.getQuantity();

            if (product.getStockQuantity() < newQty) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getCartItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        // Reload to reflect updated state
        cart = cartRepository.findById(cart.getId()).orElseThrow();
        log.info("Item added to cart: userId={}, productId={}", cart.getUser().getId(), product.getId());
        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(String username, Long cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(username);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        // Security check: the item must belong to this user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        // Stock check for updated quantity
        if (cartItem.getProduct().getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " +
                    cartItem.getProduct().getStockQuantity());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        cart = cartRepository.findById(cart.getId()).orElseThrow();
        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItemFromCart(String username, Long cartItemId) {
        Cart cart = getOrCreateCart(username);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        cart = cartRepository.findById(cart.getId()).orElseThrow();
        log.info("Item removed from cart: cartItemId={}", cartItemId);
        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart(String username) {
        Cart cart = getOrCreateCart(username);
        cart.getCartItems().clear();
        cartRepository.save(cart);
        log.info("Cart cleared for user: {}", username);
    }

    // ---- Private helpers ----

    /**
     * Fetch the user's cart or create a new empty one if it doesn't exist yet.
     */
    private Cart getOrCreateCart(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .totalItems(items.size())
                .totalPrice(cart.getTotalPrice())
                .build();
    }

    private CartItemResponse mapItemToResponse(CartItem item) {
        return CartItemResponse.builder()
                .cartItemId(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImageUrl(item.getProduct().getImageUrl())
                .unitPrice(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }
}
