package com.stavaray.auth.infrastructure.repository;

import com.stavaray.auth.domain.model.RefreshToken;
import com.stavaray.auth.domain.model.User;
import com.stavaray.auth.domain.repository.RefreshTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springDataRefreshTokenRepository;

    public JpaRefreshTokenRepository(SpringDataRefreshTokenRepository springDataRefreshTokenRepository) {
        this.springDataRefreshTokenRepository = springDataRefreshTokenRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return springDataRefreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return springDataRefreshTokenRepository.findByToken(token);
    }

    @Override
    public void deleteByUser(User user) {
        springDataRefreshTokenRepository.findByUser(user).ifPresent(springDataRefreshTokenRepository::delete);
    }

    @Override
    public void deleteByToken(String token) {
        springDataRefreshTokenRepository.findByToken(token).ifPresent(springDataRefreshTokenRepository::delete);
    }

    @Override
    public Optional<RefreshToken> findByUser(User user) {
        return springDataRefreshTokenRepository.findByUser(user);
    }
}
