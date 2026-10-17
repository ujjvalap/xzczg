package com.neuromart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    @NotNull(message = "Rating is required")
    private Double rating;

    @NotNull(message = "Comment is required")
    private String comment;

    @NotNull(message = "Product ID is required")
    private Long productId;
}
