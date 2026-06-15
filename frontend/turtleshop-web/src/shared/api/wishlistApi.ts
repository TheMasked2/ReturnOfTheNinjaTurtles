import { baseApi } from "./base-api";

export const wishlistApi = {
  getItems: () => baseApi.get<any[]>("/wishlist"),
  addItem: (productId: number) =>
    baseApi.post("/wishlist", { productId }),
};