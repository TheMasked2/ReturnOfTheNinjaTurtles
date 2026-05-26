import { baseApi } from "./base-api";

export interface Customer {
  id: string;
  email: string;
  firstName: string;
  lastName?: string;
  phone?: string;
  roles: string[];
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  customer: Customer;
}

export const authApi = {
  login: (credentials: any) =>
    baseApi.post<AuthResponse>("/auth/login", credentials),

  register: (data: any) =>
    baseApi.post<AuthResponse>("/auth/register", data),

  getProfile: () =>
    baseApi.get<Customer>("/auth/me"),
};