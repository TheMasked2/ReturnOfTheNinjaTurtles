import { createContext, useContext, useState, useEffect, type ReactNode } from "react";
import { authApi } from "../api/authApi";

// --- 2. TYPES ---
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName?: string;
  phone?: string;
  address?: string;
  city?: string;
  postalCode?: string;
  country?: string;
  roles: string[];
  createdAt?: string;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: { email: string; password: string }) => Promise<void>;
  register: (data: { email: string; password: string; firstName: string; lastName?: string; phone?: string }) => Promise<void>;
  logout: () => void;
}

// --- 3. CONTEXT & PROVIDER ---
const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem("turtleshop_user");
    const savedToken = localStorage.getItem("turtleshop_token");

    if (savedUser && savedToken) {
      try {
        setUser(JSON.parse(savedUser));
      } catch (e) {
        localStorage.removeItem("turtleshop_user");
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (credentials: { email: string; password: string }) => {
    const data = await authApi.login(credentials);

    setUser(data.customer);
    localStorage.setItem("turtleshop_token", data.token);
    localStorage.setItem("turtleshop_user", JSON.stringify(data.customer));
  };

  const register = async (data: { email: string; password: string; firstName: string; lastName?: string; phone?: string }) => {
    await authApi.register(data);
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem("turtleshop_token");
    localStorage.removeItem("turtleshop_user");
  };

  const value = {
    user,
    isAuthenticated: !!user,
    isLoading,
    login,
    register,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {!isLoading && children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};