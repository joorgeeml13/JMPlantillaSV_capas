package jorge.matias.plantilla.service;

import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jorge.matias.plantilla.exception.auth.AuthException;
import jorge.matias.plantilla.model.entity.Account;
import jorge.matias.plantilla.repository.AccountRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void changePassword(String oldPassword, String newPassword, String userId){
        Account acc = accountRepository.findById(UUID.fromString(userId))
        .orElseThrow(() -> new UsernameNotFoundException("auth.user.not_found"));

        if(!passwordEncoder.matches(oldPassword, acc.getPassword()))
            throw new AuthException("auth.error.incorrect_password");

        if (oldPassword.equals(newPassword))
            throw new AuthException("auth.error.same_password");


        acc.setPassword(passwordEncoder.encode(newPassword));

        refreshTokenService.revokeAllTokens(acc);
    }
}
