import { baseApi } from "./base-api";

export interface Product {
  id: number;
  name: string;
  description: string;
  specs: string;
  price: number;
  availableSince: string;
  suggestedProducts: string[];
}

const endpoint = "/products";

export const productApi = {
  getProducts: () => baseApi.get<Product[]>(endpoint),
  getProductById: (productId: string | number) =>
    baseApi.get<Product>(`${endpoint}/${productId}`),
};
