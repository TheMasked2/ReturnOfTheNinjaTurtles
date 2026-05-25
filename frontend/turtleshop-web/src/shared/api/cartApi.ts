import { baseApi } from "./base-api";

export const cartApi = {
  getItems: () => baseApi.get<any[]>("/cart"),
  addItem: (productId: number) =>
    baseApi.post("/cart", { productId }),
};