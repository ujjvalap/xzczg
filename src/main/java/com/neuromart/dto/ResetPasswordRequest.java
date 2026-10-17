package com.neuromart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "Password cannot be empty")
    private String password;

    @NotBlank(message = "Confirm password cannot be empty")
    private String confirmPassword;
}
