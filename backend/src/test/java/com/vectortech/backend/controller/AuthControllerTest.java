package com.vectortech.backend.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectortech.backend.dto.AuthResponse;
import com.vectortech.backend.dto.LoginRequest;
import com.vectortech.backend.dto.RegisterRequest;
import com.vectortech.backend.security.JwtAuthenticationFilter;
import com.vectortech.backend.service.AuthService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings({"null", "unused"})
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
        @MockBean private StringRedisTemplate redisTemplate;

    @Test
    void register_WithValidData_ShouldReturn201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt.token.here")
                .tokenType("Bearer")
                .userId(1L)
                .name("Test User")
                .email("test@example.com")
                .role("USER")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void register_WithInvalidEmail_ShouldReturn400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("not-an-email");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithValidCredentials_ShouldReturn200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt.token.here")
                .tokenType("Bearer")
                .userId(1L)
                .name("Test User")
                .email("test@example.com")
                .role("USER")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt.token.here"));
    }
}
