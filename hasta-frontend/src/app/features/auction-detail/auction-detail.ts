import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuctionService } from '../../core/services/auction.service';
import { Auction, isAuctionActive } from '../../core/models/auction.model';

@Component({
  selector: 'app-auction-detail',
  standalone: true,
  imports: [],
  templateUrl: './auction-detail.html',
  styleUrl: './auction-detail.scss',
})
export class AuctionDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private auctionService = inject(AuctionService);

  readonly auction = signal<Auction | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.auctionService.getById(id).subscribe({
      next: (a) => {
        this.auction.set(a);
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
    return isAuctionActive(auction);
  }
}
