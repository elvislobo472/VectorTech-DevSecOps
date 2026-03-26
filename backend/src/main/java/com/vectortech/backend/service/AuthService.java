package com.vectortech.backend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vectortech.backend.dto.AuthResponse;
import com.vectortech.backend.dto.LoginRequest;
import com.vectortech.backend.dto.RegisterRequest;
import com.vectortech.backend.exception.BadRequestException;
import com.vectortech.backend.model.User;
import com.vectortech.backend.repository.UserRepository;
import com.vectortech.backend.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email is already registered: " + normalizedEmail);
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        String token = jwtTokenProvider.generateToken(user.getEmail());
        return buildAuthResponse(token, user);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                request.getEmail().trim().toLowerCase(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(authentication);
        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(token, user);
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
