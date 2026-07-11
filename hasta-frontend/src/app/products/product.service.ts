import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Product } from './product.model';
import { Purchase } from './purchase.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/products`;

  getAll(): Observable<Product[]> {
    return this.http.get<Product[]>(this.baseUrl);
  }

  buyNow(productId: number, buyerId: number): Observable<Purchase> {
    return this.http.post<Purchase>(`${this.baseUrl}/${productId}/purchase`, { buyerId });
  }
}
