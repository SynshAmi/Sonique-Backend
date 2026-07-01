package com.synshami.sonique.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.]+$",
            message = "Username can only contain letters, numbers, underscores, and dots")
    private String username;

    @NotBlank(message = "Display name is required")
    @Size(min = 2, max = 30, message = "Display name must be between 2 and 30 characters")
    private String displayName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters long")
    private String password;
}