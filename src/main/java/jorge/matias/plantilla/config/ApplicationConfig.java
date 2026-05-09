package jorge.matias.plantilla.config;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jorge.matias.plantilla.model.entity.AccountPrincipal;
import jorge.matias.plantilla.model.entity.Account;
import jorge.matias.plantilla.model.enums.AccountRole;
import jorge.matias.plantilla.model.enums.AccountStatus;
import jorge.matias.plantilla.repository.AccountRepository;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final AccountRepository accountRepository;


    @Bean
    public UserDetailsService userDetailsService(){
        return username -> {
            Account account;
            try {
                account = accountRepository.findById(UUID.fromString(username))
                    .orElseThrow(() -> new UsernameNotFoundException("auth.user.not_found"));
            } catch (IllegalArgumentException exception) {
                account = accountRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("auth.user.not_found"));
            }

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
        };
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}