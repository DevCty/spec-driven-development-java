package com.stavaray.auth.domain.repository;

import com.stavaray.auth.domain.model.RefreshToken;
import com.stavaray.auth.domain.model.User;

import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    void deleteByToken(String token);
    Optional<RefreshToken> findByUser(User user);
}
