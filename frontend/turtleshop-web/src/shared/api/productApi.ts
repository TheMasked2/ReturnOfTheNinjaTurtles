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

export interface ProductSearchParams {
  search?: string;
  sortBy?: "price_asc" | "price_desc";
  minPrice?: number;
  maxPrice?: number;
  categoryId?: number;
  page?: number;
  pageSize?: number;
}

export interface ProductCategory {
  id: number;
  name: string;
}

const endpoint = "/products";

function buildQueryString(params?: ProductSearchParams) {
  if (!params) return "";
  const query = new URLSearchParams();

  if (params.search) query.set("search", params.search);
  if (params.sortBy) query.set("sortBy", params.sortBy);
  if (typeof params.minPrice === "number") query.set("minPrice", String(params.minPrice));
  if (typeof params.maxPrice === "number") query.set("maxPrice", String(params.maxPrice));
  if (typeof params.categoryId === "number") query.set("categoryId", String(params.categoryId));
  if (typeof params.page === "number") query.set("page", String(params.page));
  if (typeof params.pageSize === "number") query.set("pageSize", String(params.pageSize));

  const queryString = query.toString();
  return queryString ? `?${queryString}` : "";
}

export async function getProducts(params: ProductSearchParams = {}): Promise<PaginatedResponse<Product[]>> {
  return baseApi.get<PaginatedResponse<Product[]>>(`${endpoint}${buildQueryString(params)}`);
}

export const getProductsByIds = (productIds: number[]) => {
  if (!productIds?.length) {
    return Promise.resolve([] as Product[]);
  }

  return baseApi.get<Product[]>(`${endpoint}?ids=${productIds.join(",")}`);
};

export const getCategories = async (): Promise<ProductCategory[]> => {
  const raw = await baseApi.get<Array<ProductCategory | { categoryId: number; name: string }>>("/categories");

  return raw.map((item) => ({
    id: "id" in item ? item.id : item.categoryId,
    name: item.name,
  }));
};

export const productApi = {
  getProducts,
  getProductsByIds,
  getCategories,
  getProductById: (productId: string | number) =>
    baseApi.get<Product>(`${endpoint}/${productId}`),
};