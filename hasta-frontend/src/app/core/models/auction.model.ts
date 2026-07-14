import { Category } from './category.model';

export interface AuctionUserRef {
  id: number;
  username: string;
  name: string;
  surname: string;
}

export interface AuctionProductRef {
  id: number;
  name: string;
  description: string;
  quantity: number;
  category: Category;
}

export interface Auction {
  id: number;
  quantitySold: number;
  startingPrice: number;
  finalPrice: number | null;
  currentPrice: number;
  startTime: string;
  endTime: string;
  sold: boolean;
  seller: AuctionUserRef;
  winner: AuctionUserRef | null;
  product: AuctionProductRef;
}

export function isAuctionActive(auction: Auction, now: number = Date.now()): boolean {
  return !auction.sold && new Date(auction.endTime).getTime() > now;
}

export function formatRemaining(endTime: string, now: number = Date.now()): string {
  const diffMs = new Date(endTime).getTime() - now;
  if (diffMs <= 0) return 'Conclusa';

  const minutes = Math.floor(diffMs / 60_000);
  const days = Math.floor(minutes / 1440);
  const hours = Math.floor((minutes % 1440) / 60);
  const mins = minutes % 60;

  if (days > 0) return `${days}g ${hours}h`;
  if (hours > 0) return `${hours}h ${mins}m`;
  return `${mins}m`;
}
