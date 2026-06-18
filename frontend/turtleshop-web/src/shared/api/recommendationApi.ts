import { baseApi } from "./base-api";

export interface RecommendedProduct {
  productId: number;
  productName: string;
  price: number;
  imageUrl?: string | null;
}

const endpoint = "/recommendations";

export const recommendationApi = {
  getPopularThisMonth: (limit = 4) =>
    baseApi.get<RecommendedProduct[]>(`${endpoint}/popular/month?limit=${limit}`),

  getPopularThisSeason: (limit = 4) =>
    baseApi.get<RecommendedProduct[]>(`${endpoint}/popular/season?limit=${limit}`),

  getFrequentlyBoughtTogether: (productId: number, limit = 4) =>
    baseApi.get<RecommendedProduct[]>(
      `${endpoint}/frequently-bought-together?productId=${productId}&limit=${limit}`
    ),

  getSeasonalRecommendations: (customerId: string, limit = 4) =>
    baseApi.get<RecommendedProduct[]>(`${endpoint}/seasonal?customerId=${customerId}&limit=${limit}`),
};