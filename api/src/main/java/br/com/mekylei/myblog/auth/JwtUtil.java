package br.com.mekylei.myblog.auth;

import br.com.mekylei.myblog.utils.DateUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

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
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
            return null;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            logger.error("Malformed JWT token: {}", e.getMessage());
            return null;
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
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
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Malformed JWT token: {}", e.getMessage());
            return true;
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
            return true;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            return true;
        }
    }

}