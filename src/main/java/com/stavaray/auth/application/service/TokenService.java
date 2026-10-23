package com.stavaray.auth.application.service;

import com.stavaray.auth.application.dto.TokenResponse;
import com.stavaray.auth.common.exception.InvalidCredentialsException;
import com.stavaray.auth.domain.model.RefreshToken;
import com.stavaray.auth.domain.model.User;
import com.stavaray.auth.domain.repository.RefreshTokenRepository;
import com.stavaray.auth.infrastructure.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public TokenResponse createTokenPair(User user) {
        String accessToken = jwtService.generateToken(user);
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshExpirationMs()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        log.info("Created token pair for user={} refreshExpMs={}", user.getEmail(), jwtService.getRefreshExpirationMs());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .refreshExpiresIn(jwtService.getRefreshExpirationMs())
                .build();
    }

    public TokenResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidCredentialsException("Refresh token inválido"));

        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.deleteByToken(refreshTokenValue);
            throw new InvalidCredentialsException("Refresh token expirado o revocado");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshTokenValue = UUID.randomUUID().toString();

        refreshToken.setToken(newRefreshTokenValue);
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtService.getRefreshExpirationMs()));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .refreshExpiresIn(jwtService.getRefreshExpirationMs())
                .build();
    }

    public void revoke(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }
}
