package com.ecommerce.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for a single item inside an order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long orderItemId;
    private Long productId;
    private String productName;
    private BigDecimal priceAtPurchase;
    private Integer quantity;
    private BigDecimal subtotal;
}
