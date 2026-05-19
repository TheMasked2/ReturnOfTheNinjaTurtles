import { baseApi } from "./base-api";
import type { User } from "../auth/AuthContext";

export const userApi = {
    getMe: (): Promise<User> => {
        return baseApi.get<User>("/users/me");
    },
};
