import { baseApi } from "./base-api";

export interface Wishlist {
  wishlistId: number;
  customerId: string;
}

export const wishlistApi = {
  getItems: () => baseApi.get<any[]>("/wishlist"),
  getWishlistByCustomer: (customerId: string) =>
    baseApi.get<Wishlist>(`/wishlist/customer/${customerId}`),
  createWishlist: (customerId: string) =>
    baseApi.post<number>(`/wishlist/customer/${customerId}`, {}),
  addItemToWishlist: (wishlistId: number, productId: number) =>
    baseApi.post(`/wishlist-item/wishlist/${wishlistId}/product/${productId}/return-id`, {}),
  getWishlistItems: (wishlistId: number) =>
    baseApi.get<any[]>(`/wishlist-item/wishlist/${wishlistId}`),
  deleteWishlistItem: (wishlistId: number, productId: number) =>
    baseApi.delete(`/wishlist-item/wishlist/${wishlistId}/product/${productId}`),
};