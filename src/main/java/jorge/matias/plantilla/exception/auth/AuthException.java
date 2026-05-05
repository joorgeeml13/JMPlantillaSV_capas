package jorge.matias.plantilla.exception.auth;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException{

    private final String messageKey;
    private final Object[] args;

    public AuthException(String messageKey, Object... args){
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }
}