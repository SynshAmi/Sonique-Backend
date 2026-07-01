package com.synshami.sonique.controller;

import com.synshami.sonique.dto.auth.RegisterRequest;
import com.synshami.sonique.dto.auth.RegisterResponse;
import com.synshami.sonique.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.synshami.sonique.dto.auth.LoginRequest;
import com.synshami.sonique.dto.auth.LoginResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {

        return userService.register(
                request.getEmail(),
                request.getUsername(),
                request.getDisplayName(),
                request.getPassword()
        );
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        return userService.login(
                request.getEmail(),
                request.getPassword()
        );
    }
}