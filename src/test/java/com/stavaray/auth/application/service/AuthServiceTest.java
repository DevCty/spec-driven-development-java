package com.stavaray.auth.application.service;

import com.stavaray.auth.application.dto.AuthResponse;
import com.stavaray.auth.application.dto.LoginRequest;
import com.stavaray.auth.application.dto.RegisterRequest;
import com.stavaray.auth.common.exception.InvalidCredentialsException;
import com.stavaray.auth.common.exception.UserAlreadyExistsException;
import com.stavaray.auth.application.dto.TokenResponse;
import com.stavaray.auth.domain.model.User;
import com.stavaray.auth.domain.repository.UserRepository;
import com.stavaray.auth.infrastructure.config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Ana");
        registerRequest.setEmail("ana@test.com");
        registerRequest.setPassword("12345678");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("ana@test.com");
        loginRequest.setPassword("12345678");
    }

    @Test
    void register_shouldCreateUserSuccessfully() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(tokenService.createTokenPair(any(User.class))).thenReturn(TokenResponse.builder()
                .accessToken("jwt-token-test")
                .refreshToken("refresh-token-test")
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .refreshExpiresIn(604800000L)
                .build());

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("ana@test.com", response.getEmail());
        assertEquals("Ana", response.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsAreValid() {
        User user = User.builder()
                .id(1L)
                .name("Ana")
                .email("ana@test.com")
                .password("hashed-password")
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(tokenService.createTokenPair(user)).thenReturn(TokenResponse.builder()
                .accessToken("jwt-token-test")
                .refreshToken("refresh-token-test")
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .refreshExpiresIn(604800000L)
                .build());

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("ana@test.com", response.getEmail());
        assertEquals("Ana", response.getName());
    }

    @Test
    void login_shouldThrowWhenPasswordIsInvalid() {
        User user = User.builder()
                .id(1L)
                .name("Ana")
                .email("ana@test.com")
                .password("hashed-password")
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }
}
