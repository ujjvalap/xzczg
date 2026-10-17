package com.neuromart.service;

import com.neuromart.dto.CreateProductRequest;
import com.neuromart.dto.ProductsListResponse;
import com.neuromart.dto.ReviewRequest;
import com.neuromart.dto.UpdateProductRequest;
import com.neuromart.dto.ApiResponse;
import com.neuromart.entity.Product;
import com.neuromart.entity.ProductImage;
import com.neuromart.entity.ProductReview;
import com.neuromart.entity.User;
import com.neuromart.exception.HandleError;
import com.neuromart.repository.ProductRepository;
import com.neuromart.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public ProductService(
            ProductRepository productRepository,
            UserRepository userRepository,
            CloudinaryService cloudinaryService
    ) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public Product createProduct(CreateProductRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HandleError("User not found", HttpStatus.NOT_FOUND));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .stock(request.getStock() != null ? request.getStock() : 1)
                .ratings(0.0)
                .numOfReviews(0)
                .user(user)
                .images(new ArrayList<>())
                .reviews(new ArrayList<>())
                .build();

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (String imgSource : request.getImages()) {
                Map<String, String> uploadResult = cloudinaryService.upload(imgSource, "products");
                ProductImage productImage = ProductImage.builder()
                        .publicId(uploadResult.get("public_id"))
                        .url(uploadResult.get("url"))
                        .product(product)
                        .build();
                product.getImages().add(productImage);
            }
        }

        return productRepository.save(product);
    }

    public ProductsListResponse getAllProducts(
            String keyword,
            String category,
            Double priceGte,
            Double priceLte,
            Double ratingsGte,
            Integer page
    ) {
        int resultsPerPage = 4;
        int currentPage = (page != null && page > 0) ? page : 1;

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase().trim() + "%"));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase().trim()));
            }

            if (priceGte != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), priceGte));
            }

            if (priceLte != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), priceLte));
            }

            if (ratingsGte != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ratings"), ratingsGte));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        long productCount = productRepository.count(spec);
        int totalPages = (int) Math.ceil((double) productCount / resultsPerPage);

        if (currentPage > totalPages && productCount > 0) {
            throw new HandleError("This page doesn't exist", HttpStatus.NOT_FOUND);
        }

        Page<Product> productPage = productRepository.findAll(
                spec,
                PageRequest.of(currentPage - 1, resultsPerPage, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<Product> products = productPage.getContent();
        if (products.isEmpty() && productCount == 0 && (keyword != null || category != null)) {
            throw new HandleError("No Product Found", HttpStatus.NOT_FOUND);
        }

        return ProductsListResponse.builder()
                .success(true)
                .products(products)
                .productCount(productCount)
                .resultsPerPage(resultsPerPage)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .build();
    }

    public Product getSingleProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new HandleError("Product Not Found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Product updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new HandleError("Product Not Found", HttpStatus.NOT_FOUND));

        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            product.setCategory(request.getCategory());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // Destroy existing images from Cloudinary
            for (ProductImage img : product.getImages()) {
                cloudinaryService.destroy(img.getPublicId());
            }
            product.getImages().clear();

            for (String imgSource : request.getImages()) {
                Map<String, String> uploadResult = cloudinaryService.upload(imgSource, "products");
                ProductImage productImage = ProductImage.builder()
                        .publicId(uploadResult.get("public_id"))
                        .url(uploadResult.get("url"))
                        .product(product)
                        .build();
                product.getImages().add(productImage);
            }
        }

        return productRepository.save(product);
    }

    @Transactional
    public ApiResponse deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new HandleError("Product Not Found", HttpStatus.NOT_FOUND));

        for (ProductImage img : product.getImages()) {
            cloudinaryService.destroy(img.getPublicId());
        }

        productRepository.delete(product);
        return ApiResponse.ok("Product Deleted successfully");
    }

    @Transactional
    public Product createOrUpdateReview(ReviewRequest request, Long userId, String userName) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new HandleError("Product not found", HttpStatus.BAD_REQUEST));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HandleError("User not found", HttpStatus.NOT_FOUND));

        Optional<ProductReview> existingReviewOpt = product.getReviews().stream()
                .filter(r -> r.getUser() != null && r.getUser().getId().equals(userId))
                .findFirst();

        if (existingReviewOpt.isPresent()) {
            ProductReview review = existingReviewOpt.get();
            review.setRating(request.getRating());
            review.setComment(request.getComment());
        } else {
            ProductReview review = ProductReview.builder()
                    .user(user)
                    .name(userName)
                    .rating(request.getRating())
                    .comment(request.getComment())
                    .product(product)
                    .build();
            product.getReviews().add(review);
        }

        product.setNumOfReviews(product.getReviews().size());
        double sum = product.getReviews().stream().mapToDouble(ProductReview::getRating).sum();
        product.setRatings(product.getReviews().isEmpty() ? 0.0 : sum / product.getReviews().size());

        return productRepository.save(product);
    }

    public List<ProductReview> getProductReviews(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new HandleError("Product not found", HttpStatus.BAD_REQUEST));
        return product.getReviews();
    }

    @Transactional
    public ApiResponse deleteReview(Long productId, Long reviewId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new HandleError("Product not found", HttpStatus.BAD_REQUEST));

        boolean removed = product.getReviews().removeIf(r -> r.getId().equals(reviewId));
        if (!removed) {
            throw new HandleError("Review not found", HttpStatus.NOT_FOUND);
        }

        product.setNumOfReviews(product.getReviews().size());
        double sum = product.getReviews().stream().mapToDouble(ProductReview::getRating).sum();
        product.setRatings(product.getReviews().isEmpty() ? 0.0 : sum / product.getReviews().size());

        productRepository.save(product);
        return ApiResponse.ok("Review Deleted Successfully");
    }

    public List<Product> getAdminProducts() {
        return productRepository.findAll();
    }
}
