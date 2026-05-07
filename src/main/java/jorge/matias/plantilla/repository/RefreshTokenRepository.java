package jorge.matias.plantilla.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jorge.matias.plantilla.model.entity.Account;
import jorge.matias.plantilla.model.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID>{
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.account.id = :accountId AND rt.revoked = false")
    void revokeAllByAccount(UUID accountId);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true " +
           "WHERE rt.account.id = :accountId AND rt.deviceId = :deviceId AND rt.revoked = false")
    void revokeByAccountAndDeviceId(UUID accountId, String deviceId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.account.id = :accountId AND (rt.revoked = true OR rt.expiryDate < :now)")
    void cleanUpTokens(UUID accountId, Instant now);
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.revoked = false AND rt.account  = :acc")
    List<RefreshToken> findValidTokensByUser(@Param("acc") Account acc);
}
