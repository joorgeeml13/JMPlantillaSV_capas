package jorge.matias.plantilla.exception.auth;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando un refresh token no existe en la base de datos.
 * 
 * Código HTTP: 404 NOT_FOUND (el token solicitado no existe)
 * Mensaje i18n: auth.error.refresh-token-not-found
 */
public class RefreshTokenNotFoundException extends AuthException {

    public RefreshTokenNotFoundException() {
        super("auth.error.refresh-token-not-found", HttpStatus.NOT_FOUND);
    }

    public RefreshTokenNotFoundException(String tokenId) {
        super("auth.error.refresh-token-not-found", HttpStatus.NOT_FOUND, tokenId);
    }
}
