package jorge.matias.plantilla.exception.auth;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando se detecta un intento de reutilización de un refresh token revocado.
 * Indica un posible ataque (token rotation bypass o token reuse attack).
 * 
 * Código HTTP: 401 UNAUTHORIZED (token inválido/revocado)
 * Mensaje i18n: auth.error.refresh-token-compromised
 */
public class RefreshTokenCompromisedException extends AuthException {

    public RefreshTokenCompromisedException() {
        super("auth.error.refresh-token-compromised", HttpStatus.UNAUTHORIZED);
    }

    public RefreshTokenCompromisedException(String deviceId) {
        super("auth.error.refresh-token-compromised", HttpStatus.UNAUTHORIZED, deviceId);
    }
}
