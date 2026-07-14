import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/users`;


  readonly currentUser = signal<User | null>(null);

  loadCurrentUser(): Observable<User> {
    return this.http
      .get<User>(`${this.baseUrl}/me`)
      .pipe(tap((user) => this.currentUser.set(user)));
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${id}`);
  }
}
