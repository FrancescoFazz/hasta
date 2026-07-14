import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { CategoryBar } from '../../layout/category-bar/category-bar';
import { AuctionCard } from '../auction-card/auction-card';
import { AuctionService } from '../../core/services/auction.service';
import { Auction, isAuctionActive } from '../../core/models/auction.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CategoryBar, AuctionCard],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {
  private auctionService = inject(AuctionService);

  private readonly auctions = signal<Auction[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly skeletonSlots = Array.from({ length: 6 }, (_, i) => i);

  private readonly now = signal(Date.now());

  readonly activeAuctions = computed(() =>
    this.auctions().filter((a) => isAuctionActive(a, this.now())),
  );
  readonly inactiveAuctions = computed(() =>
    this.auctions().filter((a) => !isAuctionActive(a, this.now())),
  );
  readonly totalAuctions = computed(() => this.auctions().length);

  constructor() {
    const tickId = setInterval(() => this.now.set(Date.now()), 30_000);
    inject(DestroyRef).onDestroy(() => clearInterval(tickId));
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.auctionService.getAll().subscribe({
      next: (auctions) => {
        this.auctions.set(auctions);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Errore nel caricamento delle aste.');
        this.loading.set(false);
        console.error(err);
      },
    });
  }
}
