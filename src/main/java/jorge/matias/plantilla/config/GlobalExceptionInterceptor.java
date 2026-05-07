package jorge.matias.plantilla.config;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jorge.matias.plantilla.exception.auth.AuthException;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionInterceptor  {
    
    private final MessageSource messageSource;

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex) {
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getArgs(), LocaleContextHolder.getLocale());
        ErrorResponse response = ErrorResponse.create(ex, HttpStatus.UNAUTHORIZED, message);

         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        String messageKey = "auth.error.invalid-credentials";
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        if (ex instanceof BadCredentialsException) {
            messageKey = "auth.error.bad-credentials";
        } else if (ex instanceof DisabledException) {
            messageKey = "auth.error.account-disabled";
        } else if (ex instanceof LockedException) {
            messageKey = "auth.error.account-locked";
        }
        
        String message = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        
        ErrorResponse response = ErrorResponse.create(ex, status, message);

        return ResponseEntity.status(status).body(response);
    }
}
