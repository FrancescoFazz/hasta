import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { AuctionService } from '../../core/services/auction.service';
import { Auction, isAuctionActive } from '../../core/models/auction.model';
import { Category } from '../../core/models/category.model';
import { CategoryService } from '../../core/services/category.service';
import { AuctionCard } from '../auction-card/auction-card';
import { CategoryIcon } from '../../shared/category-icon/category-icon';
import { CategoryBar } from '../../layout/category-bar/category-bar';

@Component({
  selector: 'app-category-page',
  standalone: true,
  imports: [AuctionCard, CategoryIcon, CategoryBar],
  templateUrl: './category-page.html',
  styleUrl: './category-page.scss',
})
export class CategoryPage implements OnInit {
  private route = inject(ActivatedRoute);
  private auctionService = inject(AuctionService);
  private categoryService = inject(CategoryService);

  private readonly allAuctions = signal<Auction[]>([]);
  private readonly now = signal(Date.now());
  readonly category = signal<Category | null>(null);
  readonly loading = signal(true);

  readonly categoryInfo = computed(() => {
    const cat = this.category();
    return cat ? this.categoryService.getInfo(cat) : undefined;
  });

  private readonly byCategory = computed(() => {
    const cat = this.category();
    return cat ? this.allAuctions().filter((a) => a.product.category === cat) : [];
  });

  readonly activeAuctions = computed(() =>
    this.byCategory().filter((a) => isAuctionActive(a, this.now())),
  );
  readonly inactiveAuctions = computed(() =>
    this.byCategory().filter((a) => !isAuctionActive(a, this.now())),
  );

  private destroyRef = inject(DestroyRef);

  constructor() {
    const tickId = setInterval(() => this.now.set(Date.now()), 30_000);
    inject(DestroyRef).onDestroy(() => clearInterval(tickId));
  }

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      this.category.set(params.get('id') as Category);
    });

    this.auctionService.getAll().subscribe({
      next: (auctions) => {
        this.allAuctions.set(auctions);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
