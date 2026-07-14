import { Component, DestroyRef, computed, inject, input, signal } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Auction, formatRemaining, isAuctionActive } from '../../core/models/auction.model';
import { formatCurrency } from '../../core/utils/format.util';
import { CategoryService } from '../../core/services/category.service';
import { CategoryIcon } from '../../shared/category-icon/category-icon';

@Component({
  selector: 'app-auction-card',
  standalone: true,
  imports: [RouterLink, NgTemplateOutlet, CategoryIcon],
  templateUrl: './auction-card.html',
  styleUrl: './auction-card.scss',
})
export class AuctionCard {
  auction = input.required<Auction>();

  private categoryService = inject(CategoryService);

  private readonly now = signal(Date.now());

  constructor() {
    const tickId = setInterval(() => this.now.set(Date.now()), 30_000);
    inject(DestroyRef).onDestroy(() => clearInterval(tickId));
  }

  readonly active = computed(() => isAuctionActive(this.auction(), this.now()));
  readonly categoryInfo = computed(() => this.categoryService.getInfo(this.auction().product.category));
  readonly remaining = computed(() => formatRemaining(this.auction().endTime, this.now()));
  readonly formattedPrice = computed(() => formatCurrency(this.auction().currentPrice));
}
