package com.neuromart.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {

    @NotBlank(message = "Item name is required")
    private String name;

    @NotNull(message = "Item price is required")
    private Double price;

    @NotNull(message = "Item quantity is required")
    private Integer quantity;

    @NotBlank(message = "Item image is required")
    private String image;

    private Long productId;

    @JsonSetter("product")
    public void setProduct(Object product) {
        if (product != null) {
            try {
                this.productId = Long.valueOf(product.toString());
            } catch (NumberFormatException ignored) {}
        }
    }

    @JsonSetter("productId")
    public void setProductIdField(Long productId) {
        this.productId = productId;
    }
}
