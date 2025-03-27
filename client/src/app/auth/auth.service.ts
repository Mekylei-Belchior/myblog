import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';

// The standard API URL
const API = environment.apiURL;

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }), };
    private tokenKey = 'authToken';

    constructor(private httpClient: HttpClient, private router: Router) { }

    login(email: string, password: string) {
        return this.httpClient.post<{ token: string }>(`${API}/auth/login`, { email, password }, this.httpOptions)
            .subscribe(response => {
                localStorage.setItem(this.tokenKey, response.token);
                this.router.navigate(['/postagem']);
            });
    }

    logout() {
        localStorage.removeItem(this.tokenKey);
        localStorage.clear();
        this.router.navigate(['/postagem']);
    }

    getToken(): string {
        return localStorage.getItem(this.tokenKey)!;
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }
}