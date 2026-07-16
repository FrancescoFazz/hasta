import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { UserService } from '../../core/services/user.service';
import { AuctionService } from '../../core/services/auction.service';
import { ProductService } from '../../products/product.service';
import { formatCurrency } from '../../core/utils/format.util';
import { Auction, isAuctionActive } from '../../core/models/auction.model';
import { Purchase } from '../../products/purchase.model';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.scss',
})
export class UserProfile implements OnInit {
  private userService = inject(UserService);
  private auctionService = inject(AuctionService);
  private productService = inject(ProductService);

  readonly user = this.userService.currentUser;
  readonly loading = signal(true);
  readonly activityLoading = signal(true);

  readonly topUpAmounts = [10, 50, 100];
  readonly chargingAmount = signal<number | null>(null);
  readonly chargeError = signal<string | null>(null);

  private readonly allAuctions = signal<Auction[]>([]);
  private readonly purchases = signal<Purchase[]>([]);
  private readonly now = signal(Date.now());

  readonly memberNumber = computed(() => {
    const u = this.user();
    return u ? u.id.toString().padStart(4, '0') : '';
  });

  readonly memberSince = computed(() => {
    const u = this.user();
    if (!u) {
      return '';
    }
    return new Intl.DateTimeFormat('it-IT', { month: 'long', year: 'numeric' }).format(new Date(u.createdAt));
  });

  readonly mySelling = computed(() => {
    const u = this.user();
    if (!u) {
      return [];
    }
    return [...this.allAuctions()]
      .filter((a) => a.seller.id === u.id)
      .sort((a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime());
  });

  readonly myWinning = computed(() => {
    const u = this.user();
    if (!u) {
      return [];
    }
    return [...this.allAuctions()]
      .filter((a) => a.winner?.id === u.id)
      .sort((a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime());
  });

  readonly myPurchases = computed(() =>
    [...this.purchases()].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()),
  );

  readonly stats = computed(() => ({
    selling: this.mySelling().length,
    winning: this.myWinning().filter((a) => isAuctionActive(a, this.now())).length,
    won: this.myWinning().filter((a) => !isAuctionActive(a, this.now())).length,
    purchases: this.myPurchases().length,
  }));

  constructor() {
    const tickId = setInterval(() => this.now.set(Date.now()), 30_000);
    inject(DestroyRef).onDestroy(() => clearInterval(tickId));
  }

  ngOnInit(): void {
    this.userService.loadCurrentUser().subscribe({
      next: (user) => {
        this.user.set(user);
        this.loading.set(false);
        this.loadActivity();
      },
      error: (err) => {
        console.error(err);
        this.loading.set(false);
      },
    });
  }

  private loadActivity(): void {
    this.auctionService.getAll().subscribe({
      next: (auctions) => this.allAuctions.set(auctions),
      error: (err) => console.error(err),
    });

    this.productService.getMyPurchases().subscribe({
      next: (purchases) => {
        this.purchases.set(purchases);
        this.activityLoading.set(false);
      },
      error: (err) => {
        this.activityLoading.set(false);
        console.error(err);
      },
    });
  }

  isActive(auction: Auction): boolean {
    return isAuctionActive(auction, this.now());
  }

  formattedBalance(balance: number): string {
    return formatCurrency(balance);
  }

  formattedAmount(value: number): string {
    return formatCurrency(value);
  }

  formattedDate(value: string): string {
    return new Intl.DateTimeFormat('it-IT', { day: 'numeric', month: 'short', year: 'numeric' }).format(
      new Date(value),
    );
  }

  addCredit(amount: number): void {
    const user = this.user();
    if (!user) {
      return;
    }
    this.chargingAmount.set(amount);
    this.chargeError.set(null);

    this.userService.addCredit(user.id, amount).subscribe({
      next: (updated) => {
        this.user.set(updated);
        this.chargingAmount.set(null);
      },
      error: (err) => {
        this.chargingAmount.set(null);
        this.chargeError.set('Ricarica non riuscita, riprova.');
        console.error(err);
      },
    });
  }
}
