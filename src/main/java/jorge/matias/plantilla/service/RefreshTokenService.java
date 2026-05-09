package jorge.matias.plantilla.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jorge.matias.plantilla.exception.auth.RefreshTokenCompromisedException;
import jorge.matias.plantilla.exception.auth.RefreshTokenExpiredException;
import jorge.matias.plantilla.exception.auth.RefreshTokenNotFoundException;
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
        // Token no existe → 404 NOT_FOUND
        RefreshToken oldToken = refreshTokenRepository.findByToken(oldTokenValue)
            .orElseThrow(RefreshTokenNotFoundException::new);

        // Token revocado → 401 UNAUTHORIZED (ataque potencial)
        if (oldToken.isRevoked()) {
            refreshTokenRepository.revokeByAccountAndDeviceId(oldToken.getAccount().getId(), deviceId);
            throw new RefreshTokenCompromisedException(deviceId);
        }

        // Token expirado → 401 UNAUTHORIZED (sesión expirada)
        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(oldToken);
            throw new RefreshTokenExpiredException(oldToken.getExpiryDate().toString());
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

    @Transactional
    public void revokeAllTokens(Account account){
        List<RefreshToken> validTokens = refreshTokenRepository.findAllByAccountAndRevokedFalse(account);

        if (validTokens.isEmpty()) return;

        validTokens.forEach(token -> token.setRevoked(true));

        refreshTokenRepository.saveAll(validTokens);
    }
}
