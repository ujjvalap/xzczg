package com.neuromart.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    @Builder.Default
    private boolean success = true;
    private String token;
    private UserDto user;
}
