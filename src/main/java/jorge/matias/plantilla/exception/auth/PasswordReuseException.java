package jorge.matias.plantilla.exception.auth;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando el usuario intenta establecer una contraseña que ya ha utilizado.
 * Previene la reutilización de contraseñas por motivos de seguridad.
 * 
 * Código HTTP: 400 BAD_REQUEST (violación de regla de negocio)
 * Mensaje i18n: auth.error.same_password
 */
public class PasswordReuseException extends AuthException {

    public PasswordReuseException() {
        super("auth.error.same_password", HttpStatus.BAD_REQUEST);
    }

    public PasswordReuseException(Object... args) {
        super("auth.error.same_password", HttpStatus.BAD_REQUEST, args);
    }
}
