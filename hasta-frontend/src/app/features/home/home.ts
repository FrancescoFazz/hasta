import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuctionCard } from '../auction-card/auction-card';
import { CategoryIcon } from '../../shared/category-icon/category-icon';
import { AuctionService } from '../../core/services/auction.service';
import { CategoryService } from '../../core/services/category.service';
import { Auction, isAuctionActive } from '../../core/models/auction.model';

const PREVIEW_COUNT = 8;
const FEATURED_COUNT = 4;

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [AuctionCard, CategoryIcon, RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {
  private auctionService = inject(AuctionService);
  private categoryService = inject(CategoryService);

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

  readonly featuredAuctions = computed(() =>
    [...this.activeAuctions()]
      .sort((a, b) => b.currentPrice - a.currentPrice)
      .slice(0, FEATURED_COUNT),
  );

  readonly categoryTiles = computed(() => {
    const auctions = this.auctions();
    const now = this.now();
    return this.categoryService.getAll().map((info) => ({
      info,
      activeCount: auctions.filter(
        (a) => a.product.category === info.id && isAuctionActive(a, now),
      ).length,
    }));
  });

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
