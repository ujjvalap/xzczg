package com.neuromart.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ShippingInfo shippingInfo;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Embedded
    private PaymentInfo paymentInfo;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Builder.Default
    @Column(name = "item_price", nullable = false)
    private Double itemPrice = 0.0;

    @Builder.Default
    @Column(name = "tax_price", nullable = false)
    private Double taxPrice = 0.0;

    @Builder.Default
    @Column(name = "shipping_price", nullable = false)
    private Double shippingPrice = 0.0;

    @Builder.Default
    @Column(name = "total_price", nullable = false)
    private Double totalPrice = 0.0;

    @Builder.Default
    @Column(name = "order_status", nullable = false)
    private String orderStatus = "Processing";

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonProperty("_id")
    public String get_id() {
        return id != null ? String.valueOf(id) : null;
    }

    @JsonProperty("user")
    public Map<String, Object> getUserDetails() {
        if (user == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("_id", String.valueOf(user.getId()));
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        return map;
    }
}
