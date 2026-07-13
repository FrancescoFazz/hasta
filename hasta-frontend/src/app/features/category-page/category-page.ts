import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuctionService } from '../../core/services/auction.service';
import { Auction } from '../../core/models/auction.model';
import { Category } from '../../core/models/category.model';
import { AuctionCard } from '../auction-card/auction-card';

@Component({
  selector: 'app-category-page',
  standalone: true,
  imports: [AuctionCard],
  templateUrl: './category-page.html',
  styleUrl: './category-page.scss',
})
export class CategoryPage implements OnInit {
  private route = inject(ActivatedRoute);
  private auctionService = inject(AuctionService);

  private readonly allAuctions = signal<Auction[]>([]);
  readonly category = signal<Category | null>(null);
  readonly loading = signal(true);

  readonly filtered = computed(() => {
    const cat = this.category();
    return cat ? this.allAuctions().filter((a) => a.product.category === cat) : [];
  });

  ngOnInit(): void {
    this.category.set(this.route.snapshot.paramMap.get('id') as Category);
    this.auctionService.getAll().subscribe({
      next: (auctions) => {
        this.allAuctions.set(auctions);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        console.error(err);
      },
    });
  }
}
