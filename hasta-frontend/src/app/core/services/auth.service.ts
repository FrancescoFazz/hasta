import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DecodedToken, TokenResponse } from '../models/auth.model';
import { Injectable, computed, inject, signal } from '@angular/core';

const ACCESS_TOKEN_KEY = 'hasta_access_token';
const REFRESH_TOKEN_KEY = 'hasta_refresh_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  private tokenEndpoint =
    `${environment.keycloak.url}/realms/${environment.keycloak.realm}/protocol/openid-connect/token`;

  readonly currentToken = signal<DecodedToken | null>(this.readStoredToken());
  readonly isLoggedIn = computed(() => this.currentToken() !== null);

  login(username: string, password: string): Observable<TokenResponse> {
    const body = new URLSearchParams();
    body.set('grant_type', 'password');
    body.set('client_id', environment.keycloak.clientId);
    body.set('username', username);
    body.set('password', password);

    return this.http
      .post<TokenResponse>(this.tokenEndpoint, body.toString(), {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      })
      .pipe(
        tap((res) => this.storeSession(res)),
        catchError((err: HttpErrorResponse) => {
          const message =
            err.status === 401 || err.status === 400
              ? 'Username o password non corretti'
              : 'Errore di connessione, riprova più tardi';
          return throwError(() => new Error(message));
        })
      );
  }

  logout(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    this.currentToken.set(null);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  private storeSession(res: TokenResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, res.access_token);
    localStorage.setItem(REFRESH_TOKEN_KEY, res.refresh_token);
    this.currentToken.set(this.decode(res.access_token));
  }

  private readStoredToken(): DecodedToken | null {
    const token = localStorage.getItem(ACCESS_TOKEN_KEY);
    if (!token) return null;

    const decoded = this.decode(token);
    if (!decoded || decoded.exp * 1000 < Date.now()) {
      localStorage.removeItem(ACCESS_TOKEN_KEY);
      localStorage.removeItem(REFRESH_TOKEN_KEY);
      return null;
    }
    return decoded;
  }

  private decode(token: string): DecodedToken | null {
    try {
      const payload = token.split('.')[1];
      const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(json) as DecodedToken;
    } catch {
      return null;
    }
  }
}
