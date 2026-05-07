package jorge.matias.plantilla.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jorge.matias.plantilla.exception.auth.AuthException;
import jorge.matias.plantilla.model.entity.Account;
import jorge.matias.plantilla.model.entity.AccountPrincipal;
import jorge.matias.plantilla.model.entity.RefreshToken;
import jorge.matias.plantilla.model.enums.AccountStatus;
import jorge.matias.plantilla.repository.AccountRepository;
import jorge.matias.plantilla.security.jwt.JwtService;
import jorge.matias.plantilla.vo.TokenPair;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void registerAccount(String email, String password){
        if(accountRepository.existsByEmail(email))
            throw new AuthException("auth.error.account-already-exists");

        Account account = Account.builder()
            .email(email)
            .password(passwordEncoder.encode(password))
            .build();

        accountRepository.save(account);
    }

    @Transactional
    public TokenPair login(String email, String password, String deviceId){
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password));

        AccountPrincipal principal = (AccountPrincipal) auth.getPrincipal();
        Account account = accountRepository.findById(principal.getId())
            .orElseThrow(() -> new AuthException("auth.user.not_found"));

        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = refreshTokenService.createRefreshToken(account, deviceId);

        return TokenPair.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    @Transactional
    public TokenPair refreshToken(String oldRefreshToken, String deviceId) {
        RefreshToken newRefreshTokenEntity = refreshTokenService.rotateRefreshToken(oldRefreshToken, deviceId);
        Account account = newRefreshTokenEntity.getAccount();
        AccountPrincipal principal = buildPrincipal(account);
        String newAccessToken = jwtService.generateAccessToken(principal);

        return TokenPair.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshTokenEntity.getToken())
            .build();
    }

    private AccountPrincipal buildPrincipal(Account account) {
        return AccountPrincipal.builder()
            .id(account.getId())
            .email(account.getEmail())
            .password(account.getPassword())
            .authorities(account.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList()))
            .isEnabled(account.getStatus() == AccountStatus.ACTIVE)
            .isAccountNonLocked(account.getStatus() != AccountStatus.BANNED)
            .build();
    }
}