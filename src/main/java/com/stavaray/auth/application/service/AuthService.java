package com.stavaray.auth.application.service;

import com.stavaray.auth.application.dto.AuthResponse;
import com.stavaray.auth.application.dto.LoginRequest;
import com.stavaray.auth.application.dto.RegisterRequest;
import com.stavaray.auth.application.dto.TokenResponse;
import com.stavaray.auth.common.exception.InvalidCredentialsException;
import com.stavaray.auth.common.exception.UserAlreadyExistsException;
import com.stavaray.auth.domain.model.User;
import com.stavaray.auth.domain.repository.UserRepository;
import com.stavaray.auth.infrastructure.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenService tokenService;

    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting registration for email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration rejected because user already exists: email={}", request.getEmail());
            throw new UserAlreadyExistsException("El usuario ya existe");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        TokenResponse tokenResponse = tokenService.createTokenPair(savedUser);

        AuthResponse response = new AuthResponse();
        response.setToken(tokenResponse.getAccessToken());
        response.setTokenType(tokenResponse.getTokenType());
        response.setExpiresIn(tokenResponse.getExpiresIn());
        response.setEmail(savedUser.getEmail());
        response.setName(savedUser.getName());

        log.info("User registered successfully email={} expMs={}", savedUser.getEmail(), tokenResponse.getExpiresIn());
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for email={}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found email={}", request.getEmail());
                    return new InvalidCredentialsException("Credenciales inválidas");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid password email={}", request.getEmail());
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        TokenResponse tokenResponse = tokenService.createTokenPair(user);

        AuthResponse response = new AuthResponse();
        response.setToken(tokenResponse.getAccessToken());
        response.setTokenType(tokenResponse.getTokenType());
        response.setExpiresIn(tokenResponse.getExpiresIn());
        response.setEmail(user.getEmail());
        response.setName(user.getName());

        log.info("User logged in successfully email={} expMs={}", user.getEmail(), tokenResponse.getExpiresIn());
        return response;
    }
}

