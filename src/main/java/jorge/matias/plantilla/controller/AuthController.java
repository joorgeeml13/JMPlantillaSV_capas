package jorge.matias.plantilla.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jorge.matias.plantilla.config.Constantes;
import jorge.matias.plantilla.controller.dto.request.LoginRequest;
import jorge.matias.plantilla.controller.dto.request.RegisterRequest;
import jorge.matias.plantilla.controller.dto.response.AuthResponse;
import jorge.matias.plantilla.service.AuthService;
import jorge.matias.plantilla.vo.TokenPair;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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

    @Value("${app.security.jwt.refresh-cookie.secure}")
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
