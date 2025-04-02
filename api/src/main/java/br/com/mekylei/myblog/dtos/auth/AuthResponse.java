package br.com.mekylei.myblog.dtos.auth;

import br.com.mekylei.myblog.auth.JwtUtil;

public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {

    public AuthResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, "Bearer", JwtUtil.ACCESS_TOKEN_EXPIRES_IN);
    }

}
