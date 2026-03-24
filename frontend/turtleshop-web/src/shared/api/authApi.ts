import { baseApi } from "./base-api";

export interface User {
  id: string;
  username: string;
  email: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export const authApi = {
  login: (credentials: any) =>
    baseApi.post<AuthResponse>("/auth/login", credentials),

  register: (data: any) =>
    baseApi.post<AuthResponse>("/auth/register", data),

  getProfile: () =>
    baseApi.get<User>("/auth/me"),
};