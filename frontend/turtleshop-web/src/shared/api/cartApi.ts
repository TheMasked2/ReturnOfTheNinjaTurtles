import { baseApi } from "./base-api";

export const cartApi = {
  getActiveCart: (customerId: string) =>
    baseApi.get<any>(`/cart/${customerId}`),

  createCart: (customerId: string) =>
    baseApi.post<any>(`/cart/${customerId}`, {}),

  addItem: (customerId: string, productId: number, quantity: number = 1) =>
    baseApi.post(`/cart/${customerId}/items`, { productId, quantity }),

  updateItemQuantity: (cartItemId: number, quantity: number) =>
    baseApi.patch(`/cart/items/${cartItemId}`, { quantity }),

  removeItem: (cartItemId: number) =>
    baseApi.delete<void>(`/cart/items/${cartItemId}`),
};