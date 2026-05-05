package jorge.matias.plantilla.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jorge.matias.plantilla.controller.dto.request.LoginRequest;
import jorge.matias.plantilla.controller.dto.request.RegisterRequest;
import jorge.matias.plantilla.controller.dto.response.AuthResponse;
import jorge.matias.plantilla.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping(value="/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request) {

        accountService.registerAccount(request.email(), request.password());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @RequestBody LoginRequest request,
        @RequestHeader(value = "X-Client-Type", defaultValue = "WEB") String clientType
    ) {
        

        
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, "")
            .body(null);
    }
    
}
