package jorge.matias.plantilla.exception.auth;

import org.springframework.http.HttpStatus;

/**
 * EJEMPLOS DE EXTENSIÓN DE LA JERARQUÍA DE EXCEPCIONES
 * 
 * Esta clase documenta patrones para agregar nuevas excepciones específicas
 * manteniendo consistencia con la arquitectura.
 */

// ============================================================================
// EJEMPLO 1: Excepción con código 401 UNAUTHORIZED
// ============================================================================
class TokenExpiredException extends AuthException {
    /**
     * Se lanza cuando un token JWT ha expirado.
     * El cliente debe solicitar un nuevo token.
     */
    public TokenExpiredException() {
        super("auth.error.token_expired", HttpStatus.UNAUTHORIZED);
    }

    public TokenExpiredException(String tokenType) {
        super("auth.error.token_expired", HttpStatus.UNAUTHORIZED, tokenType);
    }
}

// ============================================================================
// EJEMPLO 2: Excepción con código 403 FORBIDDEN
// ============================================================================
class InsufficientPermissionsException extends AuthException {
    /**
     * Se lanza cuando el usuario autenticado no tiene permisos para realizar
     * la acción. Diferente a 401 (no autenticado) - aquí el usuario
     * está autenticado pero sin autorización.
     */
    public InsufficientPermissionsException() {
        super("auth.error.insufficient_permissions", HttpStatus.FORBIDDEN);
    }

    public InsufficientPermissionsException(String requiredRole) {
        super("auth.error.insufficient_permissions", HttpStatus.FORBIDDEN, requiredRole);
    }
}

// ============================================================================
// EJEMPLO 3: Excepción con parámetros para i18n
// ============================================================================
class AccountLockedException extends AuthException {
    /**
     * Se lanza cuando una cuenta está bloqueada por múltiples intentos fallidos.
     * Puede incluir el tiempo de desbloqueo como parámetro.
     */
    public AccountLockedException(int minutesUntilUnlock) {
        super("auth.error.account_locked", HttpStatus.FORBIDDEN, minutesUntilUnlock);
    }
}