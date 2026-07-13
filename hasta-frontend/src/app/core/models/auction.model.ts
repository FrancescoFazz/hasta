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


export function isAuctionActive(auction: Auction): boolean {
  return !auction.sold && new Date(auction.endTime).getTime() > Date.now();
}
