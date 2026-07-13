import { Component, OnInit, computed, inject, signal } from '@angular/core';
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

  readonly activeAuctions = computed(() => this.auctions().filter(isAuctionActive));
  readonly inactiveAuctions = computed(() => this.auctions().filter((a) => !isAuctionActive(a)));

  ngOnInit(): void {
    this.auctionService.getAll().subscribe({
      next: (auctions) => {
        this.auctions.set(auctions);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Errore nel caricamento delle aste');
        this.loading.set(false);
        console.error(err);
      },
    });
  }
}
