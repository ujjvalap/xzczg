package com.neuromart.service;

import com.neuromart.dto.ApiResponse;
import com.neuromart.dto.CreateOrderRequest;
import com.neuromart.dto.OrderItemDto;
import com.neuromart.dto.OrdersListResponse;
import com.neuromart.dto.UpdateOrderStatusRequest;
import com.neuromart.entity.Order;
import com.neuromart.entity.OrderItem;
import com.neuromart.entity.Product;
import com.neuromart.entity.User;
import com.neuromart.exception.HandleError;
import com.neuromart.repository.OrderRepository;
import com.neuromart.repository.ProductRepository;
import com.neuromart.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order createNewOrder(CreateOrderRequest request, Long userId) {
        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new HandleError("No order items provided", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HandleError("User not found", HttpStatus.NOT_FOUND));

        Order order = Order.builder()
                .shippingInfo(request.getShippingInfo())
                .paymentInfo(request.getPaymentInfo())
                .itemPrice(request.getItemPrice() != null ? request.getItemPrice() : 0.0)
                .taxPrice(request.getTaxPrice() != null ? request.getTaxPrice() : 0.0)
                .shippingPrice(request.getShippingPrice() != null ? request.getShippingPrice() : 0.0)
                .totalPrice(request.getTotalPrice() != null ? request.getTotalPrice() : 0.0)
                .orderStatus("Processing")
                .paidAt(LocalDateTime.now())
                .user(user)
                .orderItems(new ArrayList<>())
                .build();

        for (OrderItemDto itemDto : request.getOrderItems()) {
            OrderItem item = OrderItem.builder()
                    .name(itemDto.getName())
                    .price(itemDto.getPrice())
                    .quantity(itemDto.getQuantity())
                    .image(itemDto.getImage())
                    .productId(itemDto.getProductId() != null ? itemDto.getProductId() : 0L)
                    .order(order)
                    .build();
            order.getOrderItems().add(item);
        }

        return orderRepository.save(order);
    }

    public Order getSingleOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new HandleError("Order not found", HttpStatus.NOT_FOUND));
    }

    public List<Order> allMyOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (orders.isEmpty()) {
            throw new HandleError("No orders found", HttpStatus.NOT_FOUND);
        }
        return orders;
    }

    public OrdersListResponse getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        double totalAmount = orders.stream().mapToDouble(Order::getTotalPrice).sum();

        return OrdersListResponse.builder()
                .success(true)
                .orders(orders)
                .totalAmount(totalAmount)
                .build();
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new HandleError("Order not found", HttpStatus.NOT_FOUND));

        if ("Delivered".equalsIgnoreCase(order.getOrderStatus())) {
            throw new HandleError("This order has already been delivered", HttpStatus.BAD_REQUEST);
        }

        if ("Delivered".equalsIgnoreCase(request.getStatus())) {
            // Deduct stock for each product
            for (OrderItem item : order.getOrderItems()) {
                if (item.getProductId() != null && item.getProductId() > 0) {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new HandleError("Product not found with id: " + item.getProductId(), HttpStatus.NOT_FOUND));

                    if (product.getStock() < item.getQuantity()) {
                        throw new HandleError("Insufficient stock available for product: " + product.getName(), HttpStatus.BAD_REQUEST);
                    }

                    product.setStock(product.getStock() - item.getQuantity());
                    productRepository.save(product);
                }
            }
            order.setDeliveredAt(LocalDateTime.now());
        }

        order.setOrderStatus(request.getStatus());
        return orderRepository.save(order);
    }

    @Transactional
    public ApiResponse deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new HandleError("Order not found", HttpStatus.NOT_FOUND));

        if (!"Delivered".equalsIgnoreCase(order.getOrderStatus())) {
            throw new HandleError("Only delivered orders can be deleted (processing orders cannot be deleted)", HttpStatus.BAD_REQUEST);
        }

        orderRepository.delete(order);
        return ApiResponse.ok("Order deleted successfully");
    }
}
