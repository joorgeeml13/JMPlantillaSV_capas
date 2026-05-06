package jorge.matias.plantilla.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
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
}
