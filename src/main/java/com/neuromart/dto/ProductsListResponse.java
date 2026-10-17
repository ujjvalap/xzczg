package com.neuromart.dto;

import com.neuromart.entity.Product;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductsListResponse {
    @Builder.Default
    private boolean success = true;
    private List<Product> products;
    private long productCount;
    private int resultsPerPage;
    private int totalPages;
    private int currentPage;
}
