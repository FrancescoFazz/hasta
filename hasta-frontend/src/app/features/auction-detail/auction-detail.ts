import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AuctionService } from '../../core/services/auction.service';
import { CategoryService } from '../../core/services/category.service';
import { CategoryIcon } from '../../shared/category-icon/category-icon';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { ProductService } from '../../products/product.service';
import { formatCurrency } from '../../core/utils/format.util';
import { Auction, formatRemaining, isAuctionActive } from '../../core/models/auction.model';

@Component({
  selector: 'app-auction-detail',
  standalone: true,
  imports: [CategoryIcon, FormsModule, RouterLink],
  templateUrl: './auction-detail.html',
  styleUrl: './auction-detail.scss',
})
export class AuctionDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private auctionService = inject(AuctionService);
  private categoryService = inject(CategoryService);
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private productService = inject(ProductService);

  readonly auction = signal<Auction | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  private readonly now = signal(Date.now());

  readonly categoryInfo = computed(() => {
    const a = this.auction();
    return a ? this.categoryService.getInfo(a.product.category) : undefined;
  });

  readonly currentUser = this.userService.currentUser;

  bidAmount: number | null = null;
  readonly bidSubmitting = signal(false);
  readonly bidError = signal<string | null>(null);

  readonly buySubmitting = signal(false);
  readonly buyError = signal<string | null>(null);
  readonly buySuccess = signal(false);

  readonly minBid = computed(() => {
    const a = this.auction();
    return a ? Number(a.currentPrice) + 1 : 0;
  });

  readonly isSeller = computed(() => {
    const a = this.auction();
    const user = this.currentUser();
    return !!a && !!user && a.seller.id === user.id;
  });

  constructor() {
    const tickId = setInterval(() => this.now.set(Date.now()), 30_000);
    inject(DestroyRef).onDestroy(() => clearInterval(tickId));
  }

  ngOnInit(): void {
    if (this.authService.isLoggedIn() && !this.currentUser()) {
      this.userService.loadCurrentUser().subscribe({
        error: (err) => console.error('Impossibile caricare il profilo utente', err),
      });
    }
    this.loadAuction();
  }

  private loadAuction(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.auctionService.getById(id).subscribe({
      next: (a) => {
        this.auction.set(a);
        this.bidAmount = Number(a.currentPrice) + 1;
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Asta non trovata.');
        this.loading.set(false);
        console.error(err);
      },
    });
  }

  isActive(auction: Auction): boolean {
    return isAuctionActive(auction, this.now());
  }

  remaining(auction: Auction): string {
    return formatRemaining(auction.endTime, this.now());
  }

  formattedPrice(auction: Auction): string {
    return formatCurrency(auction.currentPrice);
  }

  formattedStartingPrice(auction: Auction): string {
    return formatCurrency(auction.startingPrice);
  }

  formattedBuyNowPrice(auction: Auction): string {
    return formatCurrency(auction.product.price);
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  submitBid(): void {
    const a = this.auction();
    const user = this.currentUser();
    if (!a || !user || this.bidAmount == null) {
      return;
    }

    this.bidSubmitting.set(true);
    this.bidError.set(null);

    this.auctionService.placeBid(a.id, user.id, this.bidAmount).subscribe({
      next: () => {
        this.bidSubmitting.set(false);
        this.loadAuction();
      },
      error: (err: HttpErrorResponse) => {
        this.bidSubmitting.set(false);
        this.bidError.set("Offerta non valida. Controlla l'importo e riprova.");
        console.error(err);
      },
    });
  }

  buyNow(): void {
    const a = this.auction();
    const user = this.currentUser();
    if (!a || !user) {
      return;
    }

    this.buySubmitting.set(true);
    this.buyError.set(null);

    this.productService.buyNow(a.product.id, user.id).subscribe({
      next: () => {
        this.buySubmitting.set(false);
        this.buySuccess.set(true);
        this.loadAuction();
      },
      error: (err: HttpErrorResponse) => {
        this.buySubmitting.set(false);
        if (err.status === 409) {
          this.buyError.set('Il prezzo di acquisto diretto non è più valido per questa asta.');
        } else {
          this.buyError.set('Acquisto non riuscito. Riprova.');
        }
        console.error(err);
      },
    });
  }
}
