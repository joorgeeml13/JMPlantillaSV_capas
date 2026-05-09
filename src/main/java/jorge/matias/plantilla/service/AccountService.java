package jorge.matias.plantilla.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jorge.matias.plantilla.exception.auth.IncorrectPasswordException;
import jorge.matias.plantilla.exception.auth.PasswordReuseException;
import jorge.matias.plantilla.exception.auth.UserNotFoundException;
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
    public void changePassword(String oldPassword, String newPassword, String userId) {
        Account acc = accountRepository.findById(UUID.fromString(userId))
            .orElseThrow(UserNotFoundException::new);

        // Validar que la contraseña antigua es correcta
        if (!passwordEncoder.matches(oldPassword, acc.getPassword())) {
            throw new IncorrectPasswordException();
        }

        // Actualizar contraseña y revocar todos los tokens
        acc.setPassword(passwordEncoder.encode(newPassword));
        refreshTokenService.revokeAllTokens(acc);
    }
}
