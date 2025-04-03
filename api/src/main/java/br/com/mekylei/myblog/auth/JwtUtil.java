package br.com.mekylei.myblog.auth;

import br.com.mekylei.myblog.utils.DateUtil;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${JWT_SECRET}")
    private String secretKey;

    // 900 sec (15 minutes)
    public static final long ACCESS_TOKEN_EXPIRES_IN = 900;
    // 604800 sec (7 days)
    public static final long REFRESH_TOKEN_EXPIRES_IN = 604800;
    // 900000 milliseconds (15 minutes)
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15;
    // 604800000 milliseconds (7 days)
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;


    SecretKey getSigningKey() {
        byte[] key = Base64.getDecoder().decode(secretKey);
        return new SecretKeySpec(key, 0, key.length, "HmacSHA256");
    }

    public String generateAccessToken(String email) {
        return buildToken(email, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, REFRESH_TOKEN_EXPIRATION);
    }

    private String buildToken(String email, long expiration) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expiration);

        return Jwts.builder()
                .subject(email)
                .issuedAt(DateUtil.toDate(now))
                .expiration(DateUtil.toDate(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public String validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();

            Instant expiration = expirationDate.toInstant();
            return expiration.isBefore(Instant.now());
        } catch (Exception e) {
            return true;
        }
    }

}