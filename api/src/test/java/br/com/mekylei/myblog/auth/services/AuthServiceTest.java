package br.com.mekylei.myblog.auth.services;

import br.com.mekylei.myblog.auth.JwtUtil;
import br.com.mekylei.myblog.dtos.auth.Token;
import br.com.mekylei.myblog.exceptions.TokenException;
import br.com.mekylei.myblog.models.RefreshToken;
import br.com.mekylei.myblog.repositories.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "secret";
    private static final String ACCESS_TOKEN = "access.token.here";
    private static final String REFRESH_TOKEN = "refresh.token.here";

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = User.builder()
                .username(EMAIL)
                .password(PASSWORD)
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void login_whenValidCredentials_shouldReturnToken() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
        when(jwtUtil.generateAccessToken(EMAIL)).thenReturn(ACCESS_TOKEN);
        when(jwtUtil.generateRefreshToken(EMAIL)).thenReturn(REFRESH_TOKEN);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        Token result = authService.login(EMAIL, PASSWORD);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(REFRESH_TOKEN);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_whenInvalidCredentials_shouldThrowException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(EMAIL, "wrongpassword"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refreshToken_whenValidToken_shouldReturnNewAccessToken() {
        RefreshToken storedToken = new RefreshToken(REFRESH_TOKEN, EMAIL,
                LocalDateTime.now().plusDays(7));
        when(jwtUtil.isTokenExpired(REFRESH_TOKEN)).thenReturn(false);
        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(storedToken));
        when(jwtUtil.validateToken(REFRESH_TOKEN)).thenReturn(EMAIL);
        when(jwtUtil.generateAccessToken(EMAIL)).thenReturn(ACCESS_TOKEN);

        Token result = authService.refreshToken(REFRESH_TOKEN);

        assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    void refreshToken_whenExpiredToken_shouldThrowTokenException() {
        when(jwtUtil.isTokenExpired(REFRESH_TOKEN)).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void refreshToken_whenTokenNotInRepository_shouldThrowTokenException() {
        when(jwtUtil.isTokenExpired(REFRESH_TOKEN)).thenReturn(false);
        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void refreshToken_whenEmailMismatch_shouldThrowTokenException() {
        RefreshToken storedToken = new RefreshToken(REFRESH_TOKEN, "other@example.com",
                LocalDateTime.now().plusDays(7));
        when(jwtUtil.isTokenExpired(REFRESH_TOKEN)).thenReturn(false);
        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(storedToken));
        when(jwtUtil.validateToken(REFRESH_TOKEN)).thenReturn(EMAIL);

        assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void refreshToken_whenValidateTokenReturnsNull_shouldThrowTokenException() {
        RefreshToken storedToken = new RefreshToken(REFRESH_TOKEN, EMAIL,
                LocalDateTime.now().plusDays(7));
        when(jwtUtil.isTokenExpired(REFRESH_TOKEN)).thenReturn(false);
        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(storedToken));
        when(jwtUtil.validateToken(REFRESH_TOKEN)).thenReturn(null);

        assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void deleteToken_whenCalled_shouldDelegateToRepository() {
        authService.deleteToken(REFRESH_TOKEN);

        verify(refreshTokenRepository).deleteByToken(REFRESH_TOKEN);
    }
}
