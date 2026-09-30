package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.request.LoginRequest;
import com.grouplearning.backend.dto.request.RegisterRequest;
import com.grouplearning.backend.dto.response.CurrentUserResponse;
import com.grouplearning.backend.dto.response.LoginResponse;
import com.grouplearning.backend.dto.response.UserResponse;
import com.grouplearning.backend.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Authentication",
        description = "Operations for user registration, login and authentication"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService
    ) {
        this.authService = authService;
    }

    @Operation(
            summary = "Get current authenticated user",
            description = """
                    Returns basic information about the currently authenticated user.

                    The user ID is extracted from the JWT subject
                    and the username is extracted from the username claim.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Current user retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser(

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {

        CurrentUserResponse response =
                new CurrentUserResponse(
                        UUID.fromString(jwt.getSubject()),
                        jwt.getClaimAsString("username")
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Register a new user",
            description = """
                    Creates a new user account.

                    The endpoint is public and does not require authentication.
                    Username and email must be unique.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    ref = "#/components/responses/BadRequest"
            ),
            @ApiResponse(
                    responseCode = "409",
                    ref = "#/components/responses/Conflict"
            )
    })
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

    @Operation(
            summary = "Login",
            description = """
                    Authenticates a user using email and password.

                    If the credentials are valid, the server returns
                    a JWT access token that can be used for protected endpoints.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful"
            ),
            @ApiResponse(
                    responseCode = "400",
                    ref = "#/components/responses/BadRequest"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        LoginResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }
}