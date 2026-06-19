package com.ecommerce.api.controller;

import com.ecommerce.api.dto.request.PlaceOrderRequest;
import com.ecommerce.api.dto.response.ApiResponse;
import com.ecommerce.api.dto.response.OrderResponse;
import com.ecommerce.api.dto.response.PagedResponse;
import com.ecommerce.api.entity.Order.OrderStatus;
import com.ecommerce.api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for order management.
 * Customer endpoints are under /orders.
 * Admin endpoints are under /admin/orders.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order placement and management")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    // ---- Customer endpoints ----

    @PostMapping("/orders")
    @Operation(summary = "Place a new order from the current cart")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PlaceOrderRequest request) {

        OrderResponse order = orderService.placeOrder(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }

    @GetMapping("/orders")
    @Operation(summary = "Get all orders for the current user")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<OrderResponse> orders = orderService.getMyOrders(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Get a specific order by ID (must belong to current user)")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {

        OrderResponse order = orderService.getOrderById(userDetails.getUsername(), orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PutMapping("/orders/{orderId}/cancel")
    @Operation(summary = "Cancel a PENDING order")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {

        OrderResponse order = orderService.cancelOrder(userDetails.getUsername(), orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }

    // ---- Admin endpoints ----

    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Get all orders with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<OrderResponse> orders = orderService.getAllOrders(page, size);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PutMapping("/admin/orders/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Update order status (PENDING→CONFIRMED→SHIPPED→DELIVERED)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        OrderResponse order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }
}
