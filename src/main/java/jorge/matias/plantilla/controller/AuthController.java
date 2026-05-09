package jorge.matias.plantilla.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jorge.matias.plantilla.config.Constantes;
import jorge.matias.plantilla.controller.dto.request.auth.LoginRequest;
import jorge.matias.plantilla.controller.dto.request.auth.RefreshRequest;
import jorge.matias.plantilla.controller.dto.request.auth.RegisterRequest;
import jorge.matias.plantilla.controller.dto.response.AuthResponse;
import jorge.matias.plantilla.exception.auth.RefreshTokenNotFoundException;
import jorge.matias.plantilla.service.AuthService;
import jorge.matias.plantilla.vo.TokenPair;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping(value="/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService accountService;

    @Value("${security.jwt.refresh-cookie-name}")
    private String refreshCookieName;

    @Value("${security.jwt.refresh-expiration-days}")
    private long refreshCookieMaxAgeDays;

    @Value("${security.jwt.refresh-cookie.secure}")
    private boolean refreshCookieSecure;

    @Value("${security.jwt.refresh-path}")
    private String refreshPath;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request) {

        accountService.registerAccount(request.email(), request.password());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @RequestBody LoginRequest request,
        @RequestHeader(value = "X-Client-Type", defaultValue = "WEB") String clientType,
        @RequestHeader(value = "X-Device-ID", defaultValue = "web-browser") String deviceId,
        HttpServletResponse response
    ) {
        TokenPair tokens =  accountService.login(request.email(), request.password(), deviceId);

        if(Constantes.CLIENT_MOBILE.equalsIgnoreCase(clientType))
            return ResponseEntity.ok(new AuthResponse(tokens.accessToken(), tokens.refreshToken()));

        ResponseCookie cookie = createRefreshCookie(tokens.refreshToken());
        
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(new AuthResponse(tokens.accessToken(), null));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
        @RequestHeader(value = "X-Client-Type", defaultValue = "WEB") String clientType,
        @RequestHeader(value = "X-Device-ID", defaultValue = "web-browser") String deviceId,
        @CookieValue(name = "${security.jwt.refresh-cookie-name}", required = false) String refreshTokenCookie,
        @RequestBody(required = false) RefreshRequest refreshBody
    ) {
        String tokenToRefresh = null;

        if (Constantes.CLIENT_MOBILE.equalsIgnoreCase(clientType)) {
            if (refreshBody == null || refreshBody.refreshToken() == null || refreshBody.refreshToken().isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.refresh.token.missing.body");
            }
            tokenToRefresh = refreshBody.refreshToken();
        } else {
            if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
                throw new RefreshTokenNotFoundException();
            }
            tokenToRefresh = refreshTokenCookie;
        }

        TokenPair newTokens = accountService.refreshToken(tokenToRefresh, deviceId);

        if (Constantes.CLIENT_MOBILE.equalsIgnoreCase(clientType)) {
            return ResponseEntity.ok(new AuthResponse(newTokens.accessToken(), newTokens.refreshToken()));
        }

        ResponseCookie cookie = createRefreshCookie(newTokens.refreshToken());

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(new AuthResponse(newTokens.accessToken(), null));
    }

    

    private ResponseCookie createRefreshCookie(String token) {
        return ResponseCookie.from(refreshCookieName, token)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .path(refreshPath)                           
                .maxAge(refreshCookieMaxAgeDays * 24 * 60 * 60) 
                .sameSite("Strict")                         
                .build();
    }
}