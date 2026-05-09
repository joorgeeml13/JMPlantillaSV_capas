package jorge.matias.plantilla.exception.auth;

import org.springframework.http.HttpStatus;

public class AccountAlreadyExistException extends AuthException{

    public AccountAlreadyExistException(){
        super("auth.error.account_already_exist", HttpStatus.BAD_REQUEST);
    }
    protected AccountAlreadyExistException(Object... args) {
        super("auth.error.account_already_exist", HttpStatus.BAD_REQUEST, args);
    }
    
}
