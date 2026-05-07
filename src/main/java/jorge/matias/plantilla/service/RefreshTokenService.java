package jorge.matias.plantilla.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import jorge.matias.plantilla.exception.auth.AuthException;
import jorge.matias.plantilla.model.entity.Account;
import jorge.matias.plantilla.model.entity.RefreshToken;
import jorge.matias.plantilla.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public String createRefreshToken(Account acc, String deviceId){
        refreshTokenRepository.revokeByAccountAndDeviceId(acc.getId(), deviceId);

        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
            .token(tokenValue)
            .account(acc)
            .deviceId(deviceId)
            .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
            .revoked(false)
            .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String oldTokenValue, String deviceId) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(oldTokenValue)
            .orElseThrow(() -> new AuthException("auth.error.refresh-token-not-found"));

        if (oldToken.isRevoked()) {
            refreshTokenRepository.revokeByAccountAndDeviceId(oldToken.getAccount().getId(), deviceId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.error.refresh-token-compromised");
        }

        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(oldToken);
            throw new AuthException("auth.error.refresh-token-expired");
        }

        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        String newTokenValue = UUID.randomUUID().toString();
        RefreshToken newToken = RefreshToken.builder()
            .token(newTokenValue)
            .account(oldToken.getAccount())
            .deviceId(deviceId)
            .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
            .revoked(false)
            .build();

        return refreshTokenRepository.save(newToken);
    }
}