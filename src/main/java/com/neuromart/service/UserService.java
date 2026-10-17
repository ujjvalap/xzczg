package com.neuromart.service;

import com.neuromart.dto.*;
import com.neuromart.entity.User;
import com.neuromart.exception.HandleError;
import com.neuromart.repository.UserRepository;
import com.neuromart.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;

    @Value("${neuromart.jwt.cookie-expire-days:7}")
    private int cookieExpireDays;

    @Value("${neuromart.reset-password-expire-minutes:30}")
    private int resetPasswordExpireMinutes;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            CloudinaryService cloudinaryService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.cloudinaryService = cloudinaryService;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new HandleError("Email is already registered", HttpStatus.BAD_REQUEST);
        }

        Map<String, String> avatarMap = cloudinaryService.upload(request.getAvatar(), "avatars", 150, "scale");

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .avatarPublicId(avatarMap.get("public_id"))
                .avatarUrl(avatarMap.get("url"))
                .role("user")
                .build();

        user = userRepository.save(user);

        return sendTokenResponse(user, response);
    }

    public AuthResponse loginUser(LoginRequest request, HttpServletResponse response) {
        if (request.getEmail() == null || request.getEmail().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {
            throw new HandleError("Email or password cannot be empty", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new HandleError("Invalid Email or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new HandleError("Invalid Email or password", HttpStatus.UNAUTHORIZED);
        }

        return sendTokenResponse(user, response);
    }

    public ApiResponse logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return ApiResponse.ok("Successfully Logged out");
    }

    @Transactional
    public ApiResponse requestPasswordReset(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new HandleError("User doesn't exist", HttpStatus.BAD_REQUEST));

        byte[] randomBytes = new byte[20];
        new SecureRandom().nextBytes(randomBytes);
        String rawToken = HexFormat.of().formatHex(randomBytes);

        String hashedToken = sha256(rawToken);
        user.setResetPasswordToken(hashedToken);
        user.setResetPasswordExpire(LocalDateTime.now().plusMinutes(resetPasswordExpireMinutes));
        userRepository.save(user);

        String resetPasswordURL = httpRequest.getScheme() + "://" + httpRequest.getServerName() +
                (httpRequest.getServerPort() == 80 || httpRequest.getServerPort() == 443 ? "" : ":" + httpRequest.getServerPort()) +
                "/reset/" + rawToken;

        String message = "Use the following link to reset your password: " + resetPasswordURL +
                ". \n\n This link will expire in " + resetPasswordExpireMinutes + " minutes.\n\n" +
                " If you didn’t request a password reset, please ignore this message.";

        try {
            emailService.sendEmail(user.getEmail(), "Password Reset Request", message);
            return ApiResponse.ok("Email is sent to " + user.getEmail() + " successfully");
        } catch (Exception e) {
            user.setResetPasswordToken(null);
            user.setResetPasswordExpire(null);
            userRepository.save(user);
            throw new HandleError("Email couldn't be sent , please try again later", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public AuthResponse resetPassword(String token, ResetPasswordRequest request, HttpServletResponse response) {
        String hashedToken = sha256(token);
        User user = userRepository.findByResetPasswordTokenAndResetPasswordExpireAfter(hashedToken, LocalDateTime.now())
                .orElseThrow(() -> new HandleError("Reset Password token is invalid or has been expired", HttpStatus.BAD_REQUEST));

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new HandleError("Password doesn't match", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpire(null);
        userRepository.save(user);

        return sendTokenResponse(user, response);
    }

    public UserDto getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HandleError("User not found", HttpStatus.NOT_FOUND));
        return UserDto.fromEntity(user);
    }

    @Transactional
    public AuthResponse updatePassword(Long userId, UpdatePasswordRequest request, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HandleError("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new HandleError("Old password is incorrect", HttpStatus.BAD_REQUEST);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new HandleError("Password doesn't match", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return sendTokenResponse(user, response);
    }

    @Transactional
    public Map<String, Object> updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HandleError("User not found", HttpStatus.NOT_FOUND));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }

        if (request.getAvatar() != null && !request.getAvatar().isBlank()) {
            cloudinaryService.destroy(user.getAvatarPublicId());
            Map<String, String> avatarMap = cloudinaryService.upload(request.getAvatar(), "avatars", 150, "scale");
            user.setAvatarPublicId(avatarMap.get("public_id"));
            user.setAvatarUrl(avatarMap.get("url"));
        }

        user = userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Profile Updated Successfully");
        result.put("user", UserDto.fromEntity(user));
        return result;
    }

    public List<UserDto> getUsersList() {
        return userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .toList();
    }

    public UserDto getSingleUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HandleError("User doesn't exist with this id: " + id, HttpStatus.BAD_REQUEST));
        return UserDto.fromEntity(user);
    }

    @Transactional
    public UserDto updateUserRole(Long id, UpdateRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HandleError("User doesn't exist", HttpStatus.BAD_REQUEST));

        String role = request.getRole() != null ? request.getRole().trim().toLowerCase() : "user";
        user.setRole(role);
        user = userRepository.save(user);

        return UserDto.fromEntity(user);
    }

    @Transactional
    public ApiResponse deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HandleError("User doesn't exist", HttpStatus.BAD_REQUEST));

        cloudinaryService.destroy(user.getAvatarPublicId());
        userRepository.delete(user);

        return ApiResponse.ok("User Deleted Successfully");
    }

    private AuthResponse sendTokenResponse(User user, HttpServletResponse response) {
        String token = tokenProvider.generateToken(user);

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(cookieExpireDays * 24 * 60 * 60);
        cookie.setPath("/");
        response.addCookie(cookie);

        return AuthResponse.builder()
                .success(true)
                .token(token)
                .user(UserDto.fromEntity(user))
                .build();
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 error", e);
        }
    }
}
