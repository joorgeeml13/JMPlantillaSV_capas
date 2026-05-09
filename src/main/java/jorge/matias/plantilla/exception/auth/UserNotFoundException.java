package jorge.matias.plantilla.exception.auth;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando se intenta acceder a un usuario que no existe en el sistema.
 * 
 * Código HTTP: 404 NOT_FOUND (el recurso solicitado no existe)
 * Mensaje i18n: auth.error.user_not_found
 */
public class UserNotFoundException extends AuthException {

    public UserNotFoundException() {
        super("auth.error.user_not_found", HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String username) {
        super("auth.error.user_not_found", HttpStatus.NOT_FOUND, username);
    }

    public UserNotFoundException(Object... args) {
        super("auth.error.user_not_found", HttpStatus.NOT_FOUND, args);
    }
}
