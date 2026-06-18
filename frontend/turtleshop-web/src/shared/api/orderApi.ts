import { baseApi } from "./base-api";

export interface OrderSummary {
  orderId: number;
  status: string;
  orderDate: string;
  totalAmount: number;
}

export interface OrderDetails extends OrderSummary {
  shippingMethod?: string;
  shippingAddress?: string;
  items?: Array<{
    productId: number;
    productName: string;
    quantity: number;
    price: number;
  }>;
}

export interface Shipment {
  shipmentId: number;
  orderId: number;
  method: string;
  status: string;
  address: string;
}

export interface Transaction {
  transactionId: number;
  orderId: number;
  paymentMethodId: number;
  amount: number;
  status: string;
  transactionDate: string;
}

export const orderApi = {
  getOrdersByCustomer: (customerId: string) =>
    baseApi.get<OrderSummary[]>(`/orders/customer/${customerId}`),

  getOrder: (orderId: number) =>
    baseApi.get<OrderDetails>(`/orders/${orderId}`),

  getShipmentByOrder: async (orderId: number) => {
    const raw = await baseApi.get<{
      shipmentId: number;
      orderId: number;
      shipmentMethod?: string;
      shippingAddress?: string;
      method?: string;
      address?: string;
      status?: string;
    }>(`/shipments/order/${orderId}`);

    return {
      shipmentId: raw.shipmentId,
      orderId: raw.orderId,
      method: raw.method ?? raw.shipmentMethod ?? "",
      address: raw.address ?? raw.shippingAddress ?? "",
      status: raw.status ?? "UNKNOWN",
    };
  },

  getTransactionsByOrder: (orderId: number) =>
    baseApi.get<Transaction[]>(`/transactions/order/${orderId}`),
};