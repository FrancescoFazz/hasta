export interface Purchase {
  id: number;
  price: number;
  quantity: number;
  buyer: {
    id: number;
    username: string;
  };
  product: {
    id: number;
    name: string;
  };
  createdAt: string;
}
