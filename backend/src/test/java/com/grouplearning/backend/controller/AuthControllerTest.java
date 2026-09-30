package com.grouplearning.backend.controller;

import com.grouplearning.backend.exception.UnauthorizedException;
import com.grouplearning.backend.dto.request.LoginRequest;
import com.grouplearning.backend.dto.request.RegisterRequest;
import com.grouplearning.backend.dto.response.UserResponse;
import com.grouplearning.backend.exception.GlobalExceptionHandler;
import com.grouplearning.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;

    private AuthService authService;

    @BeforeEach
    void setUp() {

        authService = mock(AuthService.class);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        AuthController authController =
                new AuthController(authService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void register_shouldReturnCreated_whenRequestIsValid()
            throws Exception {

        RegisterRequest request = new RegisterRequest(
                "hieu",
                "hieu@example.com",
                "12345678"
        );

        UserResponse response = new UserResponse(
                UUID.randomUUID(),
                "hieu",
                "hieu@example.com",
                Instant.now()
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
        {
            "username": "hieu",
            "email": "hieu@example.com",
            "password": "12345678"
        }
        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("hieu"))
                .andExpect(jsonPath("$.email")
                        .value("hieu@example.com"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(authService)
                .register(any(RegisterRequest.class));
    }

    @Test
    void register_shouldReturnBadRequest_whenRequestIsInvalid()
            throws Exception {

        String requestBody = """
            {
                "username": "",
                "email": "invalid-email",
                "password": "123"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void login_shouldReturnOk_whenCredentialsAreValid()
            throws Exception {

        UserResponse response = new UserResponse(
                UUID.randomUUID(),
                "hieu",
                "hieu@example.com",
                Instant.now()
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "email": "hieu@example.com",
                                        "password": "12345678"
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("hieu"))
                .andExpect(jsonPath("$.email")
                        .value("hieu@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(authService)
                .login(any(LoginRequest.class));
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid()
            throws Exception {

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(
                        new UnauthorizedException(
                                "Invalid email or password"
                        )
                );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "email": "hieu@example.com",
                                        "password": "wrong-password"
                                    }
                                    """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error")
                        .value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/login"));
    }
}