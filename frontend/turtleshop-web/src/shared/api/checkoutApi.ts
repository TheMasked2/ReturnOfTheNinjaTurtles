import { baseApi } from "./base-api";

export interface PlaceOrderPayload {
  paymentMethod: string;
  shippingMethod: string;
  shippingAddress: string;
}

export interface PlaceOrderResponse {
  orderId: number;
  transactionId: number;
}

export const checkoutApi = {
  placeOrder: (customerId: string, payload: PlaceOrderPayload) =>
    baseApi.post<PlaceOrderResponse>(`/checkout/customer/${customerId}/place-order`, payload),

  confirmOrder: (orderId: number, transactionId: number) =>
    baseApi.post<void>(`/transactions/${transactionId}/confirm-payment?orderId=${orderId}`, {}),

  cancelOrder: (orderId: number) =>
    baseApi.patch<void>(`/orders/${orderId}/cancel`, {}),
};