import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Credentials } from '../shared/components/auth/credentials';
import { BehaviorSubject, Observable } from 'rxjs';
import { Token } from '../shared/components/auth/token';


// The standard API URL
const API = environment.apiURL;

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }), };
    private readonly tokenKey = 'authToken';
    private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.checkAuthentication());
    public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

    constructor(private httpClient: HttpClient) { }


    login(credentials: Credentials): Observable<Token> {
        const response = this.httpClient.post<Token>(`${API}/auth/login`, credentials, this.httpOptions);

        response.subscribe(
            payload => {
                if (!payload?.token) {
                    console.log('Token não encontrado na resposta');
                }
                this.storeToken(payload.token);
                this.isAuthenticatedSubject.next(true);
            },
            error => {
                console.error('Erro no login:', error.error);
            }
        );

        return response;
    }

    logout(): void {
        this.clearAuthData();
        this.clearAllSessionData();
        this.isAuthenticatedSubject.next(false);
    }

    private checkAuthentication(): boolean {
        const token = this.getToken();
        return !!token && !this.isTokenExpired(token);
    }

    private storeToken(token: string): void {
        if (token.split('.').length !== 3) {
            console.error('Token JWT inválido recebido:', token);
        }
        sessionStorage.setItem(this.tokenKey, token);
    }

    private clearAuthData(): void {
        sessionStorage.removeItem(this.tokenKey);
    }

    private clearAllSessionData(): void {
        sessionStorage.clear();
    }

    getToken(): string | null {
        return sessionStorage.getItem(this.tokenKey);
    }

    isAuthenticated(): boolean {
        return this.isAuthenticatedSubject.value;
    }

    private isTokenExpired(token: string): boolean {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.exp < (Date.now() / 1000);
        } catch {
            return true;
        }
    }

}