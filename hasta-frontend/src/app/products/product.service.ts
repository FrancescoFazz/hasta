import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Product } from './product.model';
import { Purchase } from './purchase.model';

export interface CreateProductRequest {
  name: string;
  description: string;
  quantity: number;
  price: number;
  category: string;
  sellerId: number;
}

@Injectable({ providedIn: 'root' })
export class ProductService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/products`;

  getAll(): Observable<Product[]> {
    return this.http.get<Product[]>(this.baseUrl);
  }

  createProduct(request: CreateProductRequest): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, request);
  }

  buyNow(productId: number, buyerId: number): Observable<Purchase> {
    return this.http.post<Purchase>(`${this.baseUrl}/${productId}/purchase`, { buyerId });
  }

  getMyPurchases(): Observable<Purchase[]> {
    return this.http.get<Purchase[]>(`${environment.apiUrl}/purchases/me`);
  }
}
