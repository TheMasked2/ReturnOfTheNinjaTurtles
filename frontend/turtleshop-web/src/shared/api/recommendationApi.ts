import { baseApi } from "./base-api";

export interface RecommendedProduct {
  id?: number;
  productId?: number;
  name: string;
  description?: string;
  specs?: string;
  price?: number;
  availableSince?: string;
  suggestedProducts?: string[];
}

export const recommendationApi = {
  getSeasonalRecommendations: (customerId: string, limit = 4) =>
    baseApi.get<RecommendedProduct[]>(
      `/recommendations/seasonal?customerId=${encodeURIComponent(customerId)}&limit=${limit}`
    ),
};