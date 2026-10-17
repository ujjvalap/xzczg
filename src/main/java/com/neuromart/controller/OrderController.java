package com.neuromart.controller;

import com.neuromart.dto.ApiResponse;
import com.neuromart.dto.CreateOrderRequest;
import com.neuromart.dto.OrdersListResponse;
import com.neuromart.dto.UpdateOrderStatusRequest;
import com.neuromart.entity.Order;
import com.neuromart.security.UserPrincipal;
import com.neuromart.service.OrderService;
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
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/new/order")
    public ResponseEntity<Map<String, Object>> createNewOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        Order order = orderService.createNewOrder(request, principal.getId());
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("order", order);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<Map<String, Object>> getSingleOrder(@PathVariable Long id) {
        Order order = orderService.getSingleOrder(id);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("order", order);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/orders/user")
    public ResponseEntity<Map<String, Object>> allMyOrders(@AuthenticationPrincipal UserPrincipal principal) {
        List<Order> orders = orderService.allMyOrders(principal.getId());
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("orders", orders);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrdersListResponse> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/admin/order/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        Order order = orderService.updateOrderStatus(id, request);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("order", order);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/admin/order/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.deleteOrder(id));
    }
}
