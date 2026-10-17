package com.neuromart.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Please Enter your name")
    @Size(min = 3, max = 25, message = "Name should contain between 3 and 25 characters")
    private String name;

    @NotBlank(message = "Please Enter your email")
    @Email(message = "Please enter valid email")
    private String email;

    @NotBlank(message = "Please Enter your password")
    @Size(min = 8, message = "Password should be greater than 8 characters")
    private String password;

    private String avatar; // base64 data URI or image URL
}
