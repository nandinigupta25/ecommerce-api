package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.PlaceOrderRequest;
import com.ecommerce.api.dto.response.OrderResponse;
import com.ecommerce.api.dto.response.PagedResponse;
import com.ecommerce.api.entity.Order.OrderStatus;

import java.util.List;

/**
 * Service interface for order management operations.
 */
public interface OrderService {

    /**
     * Place an order from the current user's cart contents.
     */
    OrderResponse placeOrder(String username, PlaceOrderRequest request);

    /**
     * Cancel an order (only allowed when status is PENDING).
     */
    OrderResponse cancelOrder(String username, Long orderId);

    /**
     * Get a specific order by ID (validates it belongs to the requesting user).
     */
    OrderResponse getOrderById(String username, Long orderId);

    /**
     * Get all orders for the authenticated user.
     */
    List<OrderResponse> getMyOrders(String username);

    /**
     * Admin: get all orders with pagination.
     */
    PagedResponse<OrderResponse> getAllOrders(int page, int size);

    /**
     * Admin: update the status of any order.
     */
    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus);
}
