import { authApi } from "./authApi";
import { baseApi } from "./base-api";
import type { User } from "../auth/AuthContext";

export type ProfileUpdatePayload = {
  email?: string;
  password?: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  address?: string;
  city?: string;
  postalCode?: string;
  country?: string;
};

export const userApi = {
  getMe: (): Promise<User> => authApi.getProfile(),
  updateProfile: (id: string, payload: ProfileUpdatePayload): Promise<User> =>
    baseApi.put(`/customer/${id}`, payload),
};