package com.neuromart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String role = "user";

    @Column(name = "avatar_public_id")
    private String avatarPublicId;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @JsonIgnore
    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @JsonIgnore
    @Column(name = "reset_password_expire")
    private LocalDateTime resetPasswordExpire;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Compatibility getter so both id and _id exist in JSON for React Client
    @JsonProperty("_id")
    public String get_id() {
        return id != null ? String.valueOf(id) : null;
    }

    // Avatar map getter for client compatibility
    @JsonProperty("avatar")
    public Map<String, String> getAvatar() {
        Map<String, String> map = new HashMap<>();
        map.put("public_id", avatarPublicId != null ? avatarPublicId : "");
        map.put("url", avatarUrl != null ? avatarUrl : "");
        return map;
    }
}
