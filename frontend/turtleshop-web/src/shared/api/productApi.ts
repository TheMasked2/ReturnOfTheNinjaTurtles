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

export interface PaginatedResponse<T> {
  content: T;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

const endpoint = "/products";

async function getProducts(): Promise<Product[]>;
async function getProducts(page: number, size: number): Promise<PaginatedResponse<Product[]>>;
async function getProducts(page = 0, size = 20) {
  const response = await baseApi.get<PaginatedResponse<Product[]>>(
    `${endpoint}?page=${page}&size=${size}`
  );

  return arguments.length === 0 ? response.content : response;
}

const getProductsByIds = (productIds: number[]) => {
  if (!productIds?.length) {
    return Promise.resolve([] as Product[]);
  }

  return baseApi.get<Product[]>(`${endpoint}?ids=${productIds.join(",")}`);
};

export const productApi = {
  getProducts,
  getProductsByIds,
  getProductById: (productId: string | number) =>
    baseApi.get<Product>(`${endpoint}/${productId}`),
};