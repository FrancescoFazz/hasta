import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ProductService } from '../../products/product.service';
import { AuctionService } from '../../core/services/auction.service';
import { UserService } from '../../core/services/user.service';
import { CategoryService } from '../../core/services/category.service';
import { Category } from '../../core/models/category.model';

@Component({
  selector: 'app-sell',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './sell.html',
  styleUrl: './sell.scss',
})
export class Sell implements OnInit {
  private productService = inject(ProductService);
  private auctionService = inject(AuctionService);
  private userService = inject(UserService);
  private categoryService = inject(CategoryService);
  private router = inject(Router);

  readonly categories = this.categoryService.getAll();
  readonly currentUser = this.userService.currentUser;

  name = '';
  description = '';
  quantity = 1;
  category: Category | null = null;
  price: number | null = null;
  startingPrice: number | null = null;
  durationDays = 7;

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal(false);

  readonly canSubmit = computed(() => !!this.currentUser());

  ngOnInit(): void {
    if (!this.currentUser()) {
      this.userService.loadCurrentUser().subscribe({
        error: (err) => console.error('Impossibile caricare il profilo utente', err),
      });
    }
  }

  submit(): void {
    const user = this.currentUser();
    if (!user) {
      this.error.set('Devi essere loggato per pubblicare un\'asta.');
      return;
    }
    if (
      !this.name ||
      !this.description ||
      !this.category ||
      this.price == null ||
      this.startingPrice == null ||
      this.quantity == null
    ) {
      this.error.set('Compila tutti i campi.');
      return;
    }
    if (this.price != null && this.startingPrice != null && this.price <= this.startingPrice) {
      this.error.set('Il prezzo di acquisto diretto deve essere maggiore del prezzo di partenza dell\'asta.');
    }

    this.submitting.set(true);
    this.error.set(null);

    this.productService
      .createProduct({
        name: this.name,
        description: this.description,
        quantity: this.quantity,
        price: this.price,
        category: this.category,
        sellerId: user.id,
      })
      .subscribe({
        next: (product) => {
          const endTime = new Date(Date.now() + this.durationDays * 86_400_000).toISOString();
          this.auctionService
            .createAuction({
              startingPrice: this.startingPrice!,
              quantitySold: this.quantity,
              sellerId: user.id,
              productId: product.id,
              endTime,
            })
            .subscribe({
              next: (auction) => {
                this.submitting.set(false);
                this.success.set(true);
                this.router.navigate(['/aste', auction.id]);
              },
              error: (err: HttpErrorResponse) => {
                this.submitting.set(false);
                this.error.set("Prodotto creato ma la pubblicazione dell'asta è fallita. Riprova.");
                console.error(err);
              },
            });
        },
        error: (err: HttpErrorResponse) => {
          this.submitting.set(false);
          this.error.set('Impossibile creare il prodotto. Controlla i dati inseriti.');
          console.error(err);
        },
      });
  }
}
