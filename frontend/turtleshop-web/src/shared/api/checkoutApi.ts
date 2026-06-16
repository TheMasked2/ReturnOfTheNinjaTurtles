import { baseApi } from "./base-api";

export const checkoutApi = {
  placeOrder: (customerId: string, payload: any) =>
    baseApi.post(`/checkout/customer/${customerId}/place-order`, payload),
};