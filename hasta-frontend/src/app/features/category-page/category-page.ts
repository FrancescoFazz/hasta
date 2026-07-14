import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { AuctionService } from '../../core/services/auction.service';
import { Auction } from '../../core/models/auction.model';
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
  private destroyRef = inject(DestroyRef);

  private readonly allAuctions = signal<Auction[]>([]);
  readonly category = signal<Category | null>(null);
  readonly loading = signal(true);

  readonly categoryInfo = computed(() => {
    const cat = this.category();
    return cat ? this.categoryService.getInfo(cat) : undefined;
  });

  // Nessuno split attive/concluse qui apposta: la pagina categoria mostra tutta la storia
  // dei lotti in un'unica lista, ordinata dal più recente (endTime) al più vecchio, così si
  // vede tutto quello che è successo senza dover separare cosa è ancora attivo da cosa no.
  readonly auctions = computed(() => {
    const cat = this.category();
    if (!cat) return [];
    return this.allAuctions()
      .filter((a) => a.product.category === cat)
      .sort((a, b) => new Date(b.endTime).getTime() - new Date(a.endTime).getTime());
  });

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
