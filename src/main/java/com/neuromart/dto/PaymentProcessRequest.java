package com.neuromart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessRequest {
    @NotNull(message = "Amount is required")
    private Double amount;
}
