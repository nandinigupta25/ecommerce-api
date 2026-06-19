package com.ecommerce.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * OrderItem entity — a snapshot of a product at the time an order was placed.
 * Price is stored here to preserve historical pricing even if product price changes later.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The order this item belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Reference to the product (kept for data integrity) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Snapshot of the product name at time of order */
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    /** Snapshot of the price at time of order — protects against future price changes */
    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Convenience method to compute subtotal for this line item.
     * @return priceAtPurchase * quantity
     */
    public BigDecimal getSubtotal() {
        return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }
}
