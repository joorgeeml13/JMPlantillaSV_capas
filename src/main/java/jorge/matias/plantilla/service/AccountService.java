package jorge.matias.plantilla.service;

import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jorge.matias.plantilla.exception.auth.AuthException;
import jorge.matias.plantilla.model.entity.Account;
import jorge.matias.plantilla.repository.AccountRepository;
import jorge.matias.plantilla.security.jwt.JwtService;
import jorge.matias.plantilla.vo.TokenPair;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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
    public TokenPair login(String email, String password){
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password));

        Account account = (Account) auth.getPrincipal();

        String accessToken = jwtService.generateAccessToken(account);
        String refreshToken = jwtService.generateRefreshToken(account);

        return TokenPair.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }
}
