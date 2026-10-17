package com.neuromart.controller;

import com.neuromart.dto.*;
import com.neuromart.security.UserPrincipal;
import com.neuromart.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = userService.registerUser(request, response);
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = userService.loginUser(request, response);
        return ResponseEntity.ok(authResponse);
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<ApiResponse> logout(HttpServletResponse response) {
        return ResponseEntity.ok(userService.logout(response));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<ApiResponse> requestPasswordReset(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(userService.requestPasswordReset(request, httpRequest));
    }

    @PostMapping("/reset/{token}")
    public ResponseEntity<AuthResponse> resetPassword(
            @PathVariable String token,
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(userService.resetPassword(token, request, response));
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserDetails(@AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("user", userService.getUserDetails(principal.getId()));
        return ResponseEntity.ok(body);
    }

    @PutMapping("/password/update")
    public ResponseEntity<AuthResponse> updatePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdatePasswordRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(userService.updatePassword(principal.getId(), request, response));
    }

    @PutMapping("/profile/update")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(principal.getId(), request));
    }

    // ================= ADMIN ROUTES =================

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUsersList() {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("users", userService.getUsersList());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/admin/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSingleUser(@PathVariable Long id) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("user", userService.getSingleUser(id));
        return ResponseEntity.ok(body);
    }

    @PutMapping("/admin/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("user", userService.updateUserRole(id, request));
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/admin/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }
}
