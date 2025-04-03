import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, EMPTY, Observable, of, throwError } from 'rxjs';
import { catchError, finalize, map, tap } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { Credentials } from '../shared/components/auth/credentials';
import { AuthResponse } from '../shared/interfaces/auth.interface';


// The standard API URL
const API = environment.apiURL;

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    // 300000 milliseconds (5 minutes)
    private readonly FIVE_MINUTES = 300000;

    private readonly httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };
    private readonly accessTokenKey = 'accessToken';
    private readonly refreshTokenKey = 'refreshToken';
    private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.checkAuthentication());
    public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();
    private tokenRefreshTimeout: any;

    constructor(private httpClient: HttpClient) { }

    /**
     * Authenticates user with credentials and manages JWT tokens
     *
     * @param credentials - Object containing user's email and password
     * @returns Observable emitting authentication response
     *
     * Behavior:
     * 1. Sends POST request to `/auth/login` endpoint
     * 2. Stores received tokens (access and refresh) in sessionStorage
     * 3. Updates authentication state to `true`
     * 4. Schedules automatic token refresh 5 minutes before expiration
     * 5. On error:
     *    - Updates authentication state to `false`
     *    - Propagates error to subscriber
     *
     * Example:
     * authService.login({ email, password })
     *   .subscribe({
     *     next: () => console.log('Login successful'),
     *     error: (err) => console.error('Login failed', err)
     *   });
     */
    login(credentials: Credentials): Observable<AuthResponse> {
        return this.httpClient.post<AuthResponse>(`${API}/auth/login`, credentials, this.httpOptions)
            .pipe(
                tap({
                    next: response => {
                        if (response && response.accessToken && response.refreshToken) {
                            this.storeTokens(response.accessToken, response.refreshToken);
                            this.isAuthenticatedSubject.next(true);
                            // Set a schedule to refresh the token 5 minutes before expiration
                            this.scheduleTokenRefresh((response.expiresIn * 1000) - this.FIVE_MINUTES);
                        } else {
                            console.error('Token JWT não recebido:', response);
                        }
                    },
                    error: (err) => {
                        console.error('Erro no login:', err.error);
                    }
                }),
                catchError(error => {
                    this.isAuthenticatedSubject.next(false);
                    return throwError(error);
                })
            );
    }

    /**
     * Renews access token using refresh token
     *
     * @returns Observable emitting new authentication tokens
     *
     * Behavior:
     * 1. Checks for valid refresh token
     * 2. If exists:
     *    - Sends POST to `/auth/refresh` endpoint
     *    - Stores new tokens
     *    - Schedules new automatic refresh
     * 3. If missing or on error:
     *    - Executes logout
     *    - Propagates error
     *
     * Example:
     * authService.refreshToken()
     *   .subscribe({
     *     next: (tokens) => console.log('Tokens refreshed'),
     *     error: (err) => console.error('Refresh failed', err)
     *   });
     */
    refreshToken(): Observable<AuthResponse> {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) {
            return throwError(new Error('Refresh Token não disponível'));
        }

        return this.httpClient.post<AuthResponse>(`${API}/auth/refresh`, { refreshToken }, this.httpOptions)
            .pipe(
                tap({
                    next: response => {
                        this.storeTokens(response.accessToken, response.refreshToken);
                        // Set a schedule to refresh the token 5 minutes before expiration
                        this.scheduleTokenRefresh((response.expiresIn * 1000) - this.FIVE_MINUTES);
                    },
                    error: (err) => {
                        console.error('Erro ao atualizar o token:', err.error);
                    }
                }),
                catchError(error => {
                    this.logout().subscribe({
                        complete: () => console.error('Erro ao realizar o Refresh Token', error)
                    });
                    return throwError(error);
                })
            );
    }

    /**
     * Performs user logout
     *
     * @returns Observable that completes when logout finishes
     *
     * Behavior:
     * 1. If refresh token exists:
     *    - Sends to `/auth/logout` to invalidate server-side
     * 2. Clears all authentication data:
     *    - Removes tokens from sessionStorage
     *    - Clears refresh timeout
     *    - Updates auth state to `false`
     *
     * Example:
     * authService.logout()
     *   .subscribe({
     *     complete: () => console.log('Logout completed')
     *   });
     */
    logout(): Observable<void> {
        const refreshToken = this.getRefreshToken();
        if (refreshToken) {
            return this.httpClient.post<void>(`${API}/auth/logout`, { refreshToken }, this.httpOptions)
                .pipe(
                    catchError(error => {
                        console.error('Erro no logout:', error);
                        return of(undefined);
                    }),
                    map(() => undefined),
                    finalize(() => {
                        this.clearAuthData();
                        this.clearAllSessionData();
                        this.isAuthenticatedSubject.next(false);
                        this.clearTokenRefreshTimeout();
                    })
                );
        } else {
            this.clearAuthData();
            this.clearAllSessionData();
            this.isAuthenticatedSubject.next(false);
            this.clearTokenRefreshTimeout();
            return EMPTY;
        }
    }

    /**
     * Stores tokens in sessionStorage
     * @param accessToken - JWT access token
     * @param refreshToken - JWT refresh token
     */
    private storeTokens(accessToken: string, refreshToken: string): void {
        sessionStorage.setItem(this.accessTokenKey, accessToken);
        sessionStorage.setItem(this.refreshTokenKey, refreshToken);
    }

    /**
     * Removes authentication tokens from sessionStorage
     */
    private clearAuthData(): void {
        sessionStorage.removeItem(this.accessTokenKey);
        sessionStorage.removeItem(this.refreshTokenKey);
    }

    /**
     * Clears all sessionStorage data
     */
    private clearAllSessionData(): void {
        sessionStorage.clear();
    }

    /**
     * Retrieves stored access token
     * @returns access token or null if not found
     */
    getAccessToken(): string | null {
        return sessionStorage.getItem(this.accessTokenKey);
    }

    /**
     * Retrieves stored refresh token
     * @returns refresh token or null if not found
     */
    getRefreshToken(): string | null {
        return sessionStorage.getItem(this.refreshTokenKey);
    }

    /**
     * Verifies if user is authenticated
     * @returns true if valid access token exists
     */
    private checkAuthentication(): boolean {
        const token = this.getAccessToken();
        return !!token && !this.isTokenExpired(token);
    }

    /**
     * Checks if JWT token is expired
     * @param token - JWT token to validate
     * @returns true if token is expired or invalid
     */
    private isTokenExpired(token: string): boolean {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.exp < (Date.now() / 1000);
        } catch {
            return true;
        }
    }

    /**
     * Schedules automatic token refresh
     * @param expiresIn - Milliseconds until token expiration
     */
    private scheduleTokenRefresh(expiresIn: number): void {
        this.clearTokenRefreshTimeout();
        this.tokenRefreshTimeout = setTimeout(() => {
            this.refreshToken().subscribe();
        }, expiresIn);
    }

    /**
     * Cancels pending token refresh
     */
    private clearTokenRefreshTimeout(): void {
        if (this.tokenRefreshTimeout) {
            clearTimeout(this.tokenRefreshTimeout);
            this.tokenRefreshTimeout = undefined;
        }
    }

}
