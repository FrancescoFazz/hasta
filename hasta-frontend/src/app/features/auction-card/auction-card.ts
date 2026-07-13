import { Component, computed, input } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Auction, isAuctionActive } from '../../core/models/auction.model';

@Component({
  selector: 'app-auction-card',
  standalone: true,
  imports: [RouterLink, NgTemplateOutlet],
  templateUrl: './auction-card.html',
  styleUrl: './auction-card.scss',
})
export class AuctionCard {
  auction = input.required<Auction>();
  readonly active = computed(() => isAuctionActive(this.auction()));
}
