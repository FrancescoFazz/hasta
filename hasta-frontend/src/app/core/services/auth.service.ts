import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
}

const STORAGE_KEY = 'hasta_access_token';

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
          this.accessToken.set(res.access_token);
        }),
      );
  }

  logout(): void {
    sessionStorage.removeItem(STORAGE_KEY);
    this.accessToken.set(null);
  }

  isLoggedIn(): boolean {
    return !!this.accessToken();
  }
}
