package com.neuromart.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInfo {

    @Column(name = "payment_id", nullable = false)
    private String id;

    @Column(name = "payment_status", nullable = false)
    private String status;
}
