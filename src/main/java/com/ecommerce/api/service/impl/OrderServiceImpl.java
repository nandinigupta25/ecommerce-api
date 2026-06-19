package com.ecommerce.api.service.impl;

import com.ecommerce.api.dto.request.PlaceOrderRequest;
import com.ecommerce.api.dto.response.OrderItemResponse;
import com.ecommerce.api.dto.response.OrderResponse;
import com.ecommerce.api.dto.response.PagedResponse;
import com.ecommerce.api.entity.*;
import com.ecommerce.api.entity.Order.OrderStatus;
import com.ecommerce.api.exception.BadRequestException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.CartRepository;
import com.ecommerce.api.repository.OrderRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of OrderService.
 * Handles order placement, cancellation and admin management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponse placeOrder(String username, PlaceOrderRequest request) {
        User user = findUser(username);

        // Get the user's cart
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart not found for user: " + username));

        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cannot place an order with an empty cart");
        }

        // Build order
        Order order = Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod() != null
                        ? request.getPaymentMethod() : "CASH_ON_DELIVERY")
                .notes(request.getNotes())
                .status(OrderStatus.PENDING)
                .orderItems(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        // Convert cart items → order items and reduce stock
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            // Re-check stock at order time
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName()
                        + ". Available: " + product.getStockQuantity());
            }

            // Snapshot price at purchase time
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .priceAtPurchase(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();

            order.getOrderItems().add(orderItem);

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            total = total.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(total);
        order = orderRepository.save(order);

        // Clear the cart after successful order placement
        cart.getCartItems().clear();
        cartRepository.save(cart);

        log.info("Order placed: id={}, user={}, total={}", order.getId(), username, total);
        return mapToResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(String username, Long orderId) {
        User user = findUser(username);

        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(
                    "Order cannot be cancelled. Current status: " + order.getStatus() +
                    ". Only PENDING orders can be cancelled.");
        }

        // Restore stock for each item
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        log.info("Order cancelled: id={}, user={}", orderId, username);
        return mapToResponse(order);
    }

    @Override
    public OrderResponse getOrderById(String username, Long orderId) {
        User user = findUser(username);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToResponse(order);
    }

    @Override
    public List<OrderResponse> getMyOrders(String username) {
        User user = findUser(username);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponse<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findAll(pageable);

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<OrderResponse>builder()
                .content(content)
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .first(orderPage.isFirst())
                .build();
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        order = orderRepository.save(order);

        log.info("Order status updated: id={}, newStatus={}", orderId, newStatus);
        return mapToResponse(order);
    }

    // ---- Private helpers ----

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    /**
     * Validates that the requested status transition is legal.
     * Legal flow: PENDING → CONFIRMED → SHIPPED → DELIVERED
     * CANCELLED can only be set from PENDING (via customer) or PENDING/CONFIRMED (by admin here).
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING    -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED  -> next == OrderStatus.SHIPPED   || next == OrderStatus.CANCELLED;
            case SHIPPED    -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };

        if (!valid) {
            throw new BadRequestException(
                    "Invalid status transition: " + current + " → " + next);
        }
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .items(items)
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .orderItemId(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .priceAtPurchase(item.getPriceAtPurchase())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }
}
