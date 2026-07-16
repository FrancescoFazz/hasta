import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
}


interface DecodedToken {
  sub: string;
  preferred_username: string;
  email?: string;
  exp: number;
  [key: string]: unknown;
}

const STORAGE_KEY = 'hasta_access_token';
const REFRESH_STORAGE_KEY = 'hasta_refresh_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private tokenUrl = `${environment.keycloakUrl}/realms/${environment.keycloakRealm}/protocol/openid-connect/token`;

  readonly accessToken = signal<string | null>(sessionStorage.getItem(STORAGE_KEY));

  login(username: string, password: string): Observable<TokenResponse> {
    const body = new URLSearchParams();
    body.set('grant_type', 'password');
    body.set('client_id', environment.keycloakClientId);
    body.set('username', username);
    body.set('password', password);

    return this.http
      .post<TokenResponse>(this.tokenUrl, body.toString(), {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      })
      .pipe(
        tap((res) => {
          sessionStorage.setItem(STORAGE_KEY, res.access_token);
          sessionStorage.setItem(REFRESH_STORAGE_KEY, res.refresh_token);
          this.accessToken.set(res.access_token);
        }),

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
    sessionStorage.removeItem(STORAGE_KEY);
    sessionStorage.removeItem(REFRESH_STORAGE_KEY);
    this.accessToken.set(null);
  }

  isLoggedIn(): boolean {
    return !!this.accessToken() && !this.isTokenExpired();
  }

  getDecodedToken(): DecodedToken | null {
    const token = this.accessToken();
    if (!token) return null;
    return this.decode(token);
  }

  isTokenExpired(): boolean {
    const decoded = this.getDecodedToken();
    if (!decoded) return false;
    return decoded.exp * 1000 < Date.now();
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
