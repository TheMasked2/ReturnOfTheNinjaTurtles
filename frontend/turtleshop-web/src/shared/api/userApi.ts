import { authApi } from "./authApi";
import type { User } from "../auth/AuthContext";

export const userApi = {
  getMe: (): Promise<User> => authApi.getProfile(),
};