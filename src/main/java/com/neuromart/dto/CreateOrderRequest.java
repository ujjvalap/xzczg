package com.neuromart.dto;

import com.neuromart.entity.PaymentInfo;
import com.neuromart.entity.ShippingInfo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Shipping info is required")
    private ShippingInfo shippingInfo;

    @NotEmpty(message = "No order items provided")
    private List<OrderItemDto> orderItems;

    @NotNull(message = "Payment info is required")
    private PaymentInfo paymentInfo;

    private Double itemPrice = 0.0;
    private Double taxPrice = 0.0;
    private Double shippingPrice = 0.0;

    @NotNull(message = "Total price is required")
    private Double totalPrice;
}
