export interface Product {
  id: number;
  name: string;
  description: string;
  category: string;
  price: number;
  quantity: number;
  seller: {
    id: number;
    username: string;
  };
}
