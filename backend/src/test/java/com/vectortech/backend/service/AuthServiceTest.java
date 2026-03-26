package com.vectortech.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vectortech.backend.dto.AuthResponse;
import com.vectortech.backend.dto.LoginRequest;
import com.vectortech.backend.dto.RegisterRequest;
import com.vectortech.backend.exception.BadRequestException;
import com.vectortech.backend.model.Role;
import com.vectortech.backend.model.User;
import com.vectortech.backend.repository.UserRepository;
import com.vectortech.backend.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "unused"})
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("hashed_password")
                .role(Role.USER)
                .build();
    }

    @Test
    void register_WithValidData_ShouldReturnAuthResponse() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("jwt.token.here");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo("USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithDuplicateEmail_ShouldThrowBadRequestException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt.token.here");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }
}
