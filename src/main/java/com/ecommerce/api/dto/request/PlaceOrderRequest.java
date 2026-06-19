package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for placing a new order from the current cart.
 */
@Data
public class PlaceOrderRequest {

    @NotBlank(message = "Shipping address is required")
    @Size(min = 10, max = 500, message = "Shipping address must be between 10 and 500 characters")
    private String shippingAddress;

    @Size(max = 50, message = "Payment method cannot exceed 50 characters")
    private String paymentMethod = "CASH_ON_DELIVERY";

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
