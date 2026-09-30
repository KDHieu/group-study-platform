package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.request.RegisterRequest;
import com.grouplearning.backend.dto.request.LoginRequest;
import com.grouplearning.backend.dto.response.UserResponse;
import com.grouplearning.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        UserResponse response =
                authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        UserResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }
}