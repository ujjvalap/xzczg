package com.neuromart.dto;

import com.neuromart.entity.Order;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersListResponse {
    @Builder.Default
    private boolean success = true;
    private List<Order> orders;
    private Double totalAmount;
}
