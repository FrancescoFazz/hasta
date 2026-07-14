import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { CategoryBar } from '../../layout/category-bar/category-bar';
import { AuctionCard } from '../auction-card/auction-card';
import { AuctionService } from '../../core/services/auction.service';
import { Auction, isAuctionActive } from '../../core/models/auction.model';

// Numero di aste mostrate prima del pulsante "Vedi tutte", per attive e concluse.
const PREVIEW_COUNT = 8;

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

  // "Vedi tutte": finché è false si mostrano solo le prime PREVIEW_COUNT.
  readonly showAllActive = signal(false);
  readonly showAllInactive = signal(false);

  readonly visibleActive = computed(() =>
    this.showAllActive() ? this.activeAuctions() : this.activeAuctions().slice(0, PREVIEW_COUNT),
  );
  readonly visibleInactive = computed(() =>
    this.showAllInactive() ? this.inactiveAuctions() : this.inactiveAuctions().slice(0, PREVIEW_COUNT),
  );

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
    this.showAllActive.set(false);
    this.showAllInactive.set(false);
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

  showAllActiveAuctions(): void {
    this.showAllActive.set(true);
  }

  showAllInactiveAuctions(): void {
    this.showAllInactive.set(true);
  }
}
