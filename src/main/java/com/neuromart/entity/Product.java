package com.neuromart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Builder.Default
    @Column(nullable = false)
    private Double ratings = 0.0;

    @Column(nullable = false)
    private String category;

    @Builder.Default
    @Column(nullable = false)
    private Integer stock = 1;

    @Builder.Default
    @Column(name = "num_of_reviews", nullable = false)
    private Integer numOfReviews = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductReview> reviews = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonProperty("_id")
    public String get_id() {
        return id != null ? String.valueOf(id) : null;
    }

    // Node backend called it "image", so provide "image" mapping for React
    @JsonProperty("image")
    public List<ProductImage> getImage() {
        return images != null ? images : new ArrayList<>();
    }

    @JsonProperty("user")
    public String getUserId() {
        return user != null && user.getId() != null ? String.valueOf(user.getId()) : null;
    }
}
