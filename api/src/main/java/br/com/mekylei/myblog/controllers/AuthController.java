package br.com.mekylei.myblog.controllers;

import br.com.mekylei.myblog.auth.services.AuthService;
import br.com.mekylei.myblog.dtos.auth.AuthRequest;
import br.com.mekylei.myblog.dtos.auth.AuthResponse;
import br.com.mekylei.myblog.dtos.auth.RefreshRequest;
import br.com.mekylei.myblog.dtos.auth.Token;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        Token token = authService.login(request.email(), request.password());
        return new AuthResponse(token.accessToken(), token.refreshToken());
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest request) {
        Token token = authService.refreshToken(request.refreshToken());
        return new AuthResponse(token.accessToken(), token.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        authService.deleteToken(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

}
