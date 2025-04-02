package br.com.mekylei.myblog.auth.services;

import br.com.mekylei.myblog.auth.JwtUtil;
import br.com.mekylei.myblog.dtos.auth.Token;
import br.com.mekylei.myblog.exceptions.TokenException;
import br.com.mekylei.myblog.models.RefreshToken;
import br.com.mekylei.myblog.repositories.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public AuthService(AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService,
                       RefreshTokenRepository refreshTokenRepository, JwtUtil jwtUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Token login(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        UserDetails user = userDetailsService.loadUserByUsername(email);

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        refreshTokenRepository.save(new RefreshToken(refreshToken, user.getUsername(),
                LocalDateTime.now().plusSeconds(JwtUtil.REFRESH_TOKEN_EXPIRES_IN)));

        return new Token(accessToken, refreshToken);
    }

    public Token refreshToken(String refreshToken) {
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new TokenException("Refresh token expired");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new TokenException("Invalid refresh token"));

        String email = jwtUtil.validateToken(refreshToken);
        if (email == null || !email.equals(storedToken.getEmail())) {
            throw new TokenException("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.generateAccessToken(email);

        return new Token(newAccessToken, refreshToken);
    }

    @Transactional
    public void deleteToken(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

}
