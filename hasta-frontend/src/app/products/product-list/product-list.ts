import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ProductService } from '../product.service';
import { Product } from '../product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './product-list.html',
  styleUrl: './product-list.scss'
})
export class ProductList implements OnInit {
  private productService = inject(ProductService);

  products = signal<Product[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  buyerId: number | null = null;

  purchasingProductId = signal<number | null>(null);
  purchaseError = signal<string | null>(null);
  purchaseSuccessMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading.set(true);
    this.productService.getAll().subscribe({
      next: (data) => {
        this.products.set(data);
        this.loading.set(false);
        },
        error: (err) => {
          this.error.set('Errore nel caricamento dei prodotti.');
          this.loading.set(false);
          console.error(err);
        }
      });
  }

  buyNow(product: Product): void {
    this.purchaseError.set(null);
    this.purchaseSuccessMessage.set(null);
    if(!this.buyerId){
      this.purchaseError.set('Inserisci il tuo buyerId prima di comprare.');
      return;
    }
    this.purchasingProductId.set(product.id);
    this.productService.buyNow(product.id, this.buyerId).subscribe({
      next: () => {
        this.purchasingProductId.set(null);
        this.purchaseSuccessMessage.set(`Acquisto di "${product.name}" completato!`);
        this.loadProducts();
      },
      error: (err: HttpErrorResponse) => {
        this.purchasingProductId.set(null);
        this.purchaseError.set(this.extractErrorMessage(err));
        console.error(err);
      }
    });
  }

  private extractErrorMessage(err: HttpErrorResponse): string {
    const backendMessage = err.error?.message;
    if(backendMessage) {
      return backendMessage;
    }
    if(err.status === 409) {
      return 'Il prodotto non è più disponibile.';
    }
    if(err.status === 400){
      return 'Richiesta non valida.'
    }
    return "Errore durante l'acquisto. Riprova.";
  }
}
