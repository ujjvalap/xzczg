package com.neuromart.controller;

import com.neuromart.dto.*;
import com.neuromart.entity.Product;
import com.neuromart.entity.ProductReview;
import com.neuromart.security.UserPrincipal;
import com.neuromart.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public ResponseEntity<ProductsListResponse> getAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(name = "price[gte]", required = false) Double priceGte,
            @RequestParam(name = "price[lte]", required = false) Double priceLte,
            @RequestParam(name = "ratings[gte]", required = false) Double ratingsGte,
            @RequestParam(required = false) Integer page
    ) {
        ProductsListResponse response = productService.getAllProducts(
                keyword, category, priceGte, priceLte, ratingsGte, page
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<Map<String, Object>> getSingleProduct(@PathVariable Long id) {
        Product product = productService.getSingleProduct(id);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("product", product);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/admin/product/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateProductRequest request
    ) {
        Product product = productService.createProduct(request, principal.getId());
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("product", product);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PutMapping("/admin/product/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable Long id,
            @RequestBody UpdateProductRequest request
    ) {
        Product product = productService.updateProduct(id, request);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("product", product);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/admin/product/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }

    @GetMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminProducts() {
        List<Product> products = productService.getAdminProducts();
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("products", products);
        return ResponseEntity.ok(body);
    }

    @PutMapping("/review")
    public ResponseEntity<Map<String, Object>> createReviewForProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReviewRequest request
    ) {
        Product product = productService.createOrUpdateReview(request, principal.getId(), principal.getName());
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("product", product);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/admin/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getProductReviews(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long productId
    ) {
        Long targetId = id != null ? id : productId;
        List<ProductReview> reviews = productService.getProductReviews(targetId);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("reviews", reviews);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/admin/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteReview(
            @RequestParam Long productId,
            @RequestParam Long id
    ) {
        return ResponseEntity.ok(productService.deleteReview(productId, id));
    }
}
