import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Auction } from '../models/auction.model';

@Injectable({ providedIn: 'root' })
export class AuctionService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/auctions`;

  getAll(): Observable<Auction[]> {
    return this.http.get<Auction[]>(this.baseUrl);
  }

  getById(id: number): Observable<Auction> {
    return this.http.get<Auction>(`${this.baseUrl}/${id}`);
  }

  placeBid(auctionId: number, userId: number, amount: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${auctionId}/bids`, { userId, amount });
  }
}
