import { baseApi } from "./base-api";

export interface PaymentMethod {
  paymentMethodId: number;
  provider: string;
  type: string;
}

export const paymentApi = {
  listPaymentMethods: () => baseApi.get<PaymentMethod[]>("/payment-methods"),
};