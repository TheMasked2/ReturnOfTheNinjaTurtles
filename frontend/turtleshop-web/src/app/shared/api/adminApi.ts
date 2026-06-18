import { useEffect, useState } from "react";
import type { User } from "../../../shared/auth/AuthContext";
import { baseApi } from "../../../shared/api/base-api";
import { productApi } from "../../../shared/api/productApi";

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  specs?: string;
  availableSince?: string;
  suggestedProducts?: string[];
}

export interface InventoryItem {
  inventoryId: number;
  productId: number;
  quantityAvailable: number;
  quantityReserved: number;
  version?: string;
}

export interface Transaction {
  transactionId: number;
  orderId: number;
  paymentMethodId: number;
  amount: number;
  status: string;
  transactionDate: string;
}

export interface OrderSummary {
  orderId: number;
  customerId: string;
  customerEmail: string;
  orderDate: string;
  status: string;
  totalAmount: number;
  itemLines: number;
  totalItems: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

type Fetcher<T> = () => Promise<T[]>;

const useFetchList = <T,>(fetcher: Fetcher<T>) => {
  const [data, setData] = useState<T[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refetch = async () => {
    setIsLoading(true);
    try {
      const response = await fetcher();
      setData(response);
      setError(null);
    } catch (err: any) {
      setError(err?.message || "Unable to load data");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void refetch();
  }, []);

  return { data, isLoading, error, refetch };
};

export const useGetUsersQuery = () =>
  useFetchList<User>(async () => {
    const response = await baseApi.get<PaginatedResponse<User>>("/customer?page=0&size=100");
    return response.content ?? [];
  });

export const useCreateUserMutation = () => [async (user: Partial<User> & { password: string }) => {
  await baseApi.post("/auth/register", {
    email: user.email,
    password: user.password,
    firstName: user.firstName,
    lastName: user.lastName,
    phone: user.phone,
  });
}] as const;

export const useUpdateUserMutation = () => [async (user: Partial<User> & { id: string }) => {
  return baseApi.put<User>(`/customer/${user.id}`, {
    email: user.email,
    firstName: user.firstName,
    lastName: user.lastName,
    phone: user.phone,
  });
}] as const;

export const useDeleteUserMutation = () => [async (id: string) => {
  await baseApi.delete<void>(`/customer/${id}`);
}] as const;

export const useGetProductsQuery = () =>
  useFetchList<Product>(async () => {
    const response = await productApi.getProducts();
    return response.content;
  });

export const useCreateProductMutation = () => [async (product: Partial<Product>) => {
  return baseApi.post<Product>("/products", {
    name: product.name,
    description: product.description,
    price: product.price,
    specs: product.specs ?? "",
    availableSince: product.availableSince ?? null,
    suggestedProducts: product.suggestedProducts ?? [],
  });
}] as const;

export const useUpdateProductMutation = () => [async (product: Partial<Product> & { id: number }) => {
  return baseApi.put<Product>(`/products/${product.id}`, {
    name: product.name,
    description: product.description,
    price: product.price,
    specs: product.specs ?? "",
    availableSince: product.availableSince ?? null,
    suggestedProducts: product.suggestedProducts ?? [],
  });
}] as const;

export const useDeleteProductMutation = () => [async (id: number) => {
  await baseApi.delete<void>(`/products/${id}`);
}] as const;

export const useGetInventoryQuery = () =>
  useFetchList<InventoryItem>(async () =>
    baseApi.get<InventoryItem[]>("/inventory?page=0&size=100")
  );

export const useAddInventoryMutation = () => [async (item: {
  productId: number;
  quantityAvailable: number;
  quantityReserved?: number;
}) => {
  await baseApi.post<void>(`/inventory/product/${item.productId}`, {
    quantityAvailable: item.quantityAvailable,
    quantityReserved: item.quantityReserved ?? 0,
  });
}] as const;

export const useUpdateInventoryMutation = () => [async (item: {
  productId: number;
  quantityAvailable: number;
  quantityReserved: number;
}) => {
  await baseApi.put<void>(`/inventory/product/${item.productId}`, {
    quantityAvailable: item.quantityAvailable,
    quantityReserved: item.quantityReserved,
  });
}] as const;

export const useDeleteInventoryMutation = () => [async (productId: number) => {
  await baseApi.delete<void>(`/inventory/product/${productId}`);
}] as const;

export const useGetTransactionsQuery = () =>
  useFetchList<Transaction>(async () =>
    baseApi.get<Transaction[]>("/transactions?page=0&size=100")
  );

export const useCreateTransactionMutation = () => [async (transaction: Partial<Transaction> & {
  orderId: number;
  paymentMethodId: number;
  amount: number;
  status: string;
}) => {
  await baseApi.post<void>("/transactions", {
    orderId: transaction.orderId,
    paymentMethodId: transaction.paymentMethodId,
    amount: transaction.amount,
    status: transaction.status,
  });
}] as const;

export const useUpdateTransactionMutation = () => [async (transaction: Partial<Transaction> & {
  transactionId: number;
  amount: number;
  status: string;
  paymentMethodId: number;
}) => {
  await baseApi.put<void>(`/transactions/${transaction.transactionId}`, {
    amount: transaction.amount,
    status: transaction.status,
    paymentMethodId: transaction.paymentMethodId,
  });
}] as const;

export const useDeleteTransactionMutation = () => [async (transactionId: number) => {
  await baseApi.delete<void>(`/transactions/${transactionId}`);
}] as const;

export const useGetOrdersQuery = () =>
  useFetchList<OrderSummary>(async () =>
    baseApi.get<OrderSummary[]>("/orders/admin/summary?limit=100")
  );

export const useCancelOrderMutation = () => [async (orderId: number) => {
  await baseApi.patch<void>(`/orders/${orderId}/cancel`, {});
}] as const;