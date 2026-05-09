package jorge.matias.plantilla.exception.auth;

import org.springframework.http.HttpStatus;

public class IncorrectPasswordException extends AuthException {

    public IncorrectPasswordException() {
        super("auth.error.incorrect_password", HttpStatus.BAD_REQUEST);
    }

    public IncorrectPasswordException(Object... args) {
        super("auth.error.incorrect_password", HttpStatus.BAD_REQUEST, args);
    }
}
