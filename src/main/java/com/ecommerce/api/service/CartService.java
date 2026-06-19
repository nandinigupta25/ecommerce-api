package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.AddToCartRequest;
import com.ecommerce.api.dto.request.UpdateCartItemRequest;
import com.ecommerce.api.dto.response.CartResponse;

/**
 * Service interface for shopping cart operations.
 */
public interface CartService {

    /**
     * Get the current user's cart. Creates one if it doesn't exist.
     */
    CartResponse getCart(String username);

    /**
     * Add a product to the cart. If the product already exists, increases quantity.
     */
    CartResponse addItemToCart(String username, AddToCartRequest request);

    /**
     * Update the quantity of a specific item in the cart.
     */
    CartResponse updateCartItem(String username, Long cartItemId, UpdateCartItemRequest request);

    /**
     * Remove a specific item from the cart.
     */
    CartResponse removeItemFromCart(String username, Long cartItemId);

    /**
     * Clear all items from the cart.
     */
    void clearCart(String username);
}
