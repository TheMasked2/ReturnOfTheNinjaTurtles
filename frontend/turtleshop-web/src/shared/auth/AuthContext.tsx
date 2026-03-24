import React, { createContext, useContext, useState, useEffect, ReactNode } from "react";

// --- 1. GENERIC BASE API ---
const BASE_URL = "http://localhost:8080/api";

const baseApi = {
  // The <T,> syntax is essential in .tsx files to distinguish from JSX tags
  request: async <T,>(endpoint: string, options: RequestInit = {}): Promise<T> => {
    const token = localStorage.getItem("turtleshop_token");

    const headers: HeadersInit = {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    };

    const response = await fetch(`${BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: "An error occurred" }));
      throw new Error(error.message || `Error: ${response.status}`);
    }

    // Return empty object for 204 No Content, otherwise parse JSON
    if (response.status === 204) return {} as T;
    return response.json();
  },
};

// --- 2. TYPES ---
interface User {
  id: string;
  username: string;
  email: string;
}

interface AuthResponse {
  token: string;
  user: User;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: { username: string; password: string }) => Promise<void>;
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

  const login = async (credentials: { username: string; password: string }) => {
    try {
      const data = await baseApi.request<AuthResponse>("/auth/login", {
        method: "POST",
        body: JSON.stringify(credentials),
      });

      setUser(data.user);
      localStorage.setItem("turtleshop_token", data.token);
      localStorage.setItem("turtleshop_user", JSON.stringify(data.user));
    } catch (err: any) {
      throw err;
    }
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