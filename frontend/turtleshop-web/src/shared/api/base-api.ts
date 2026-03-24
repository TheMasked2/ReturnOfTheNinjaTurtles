const BASE_URL = "http://localhost:8080/api";

export const baseApi = {
  // Generic Request Handler
  request: async <T>(endpoint: string, options: RequestInit = {}): Promise<T> => {
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
      // Try to get error message from Spring Boot, otherwise default
      const error = await response.json().catch(() => ({ message: "An error occurred" }));
      throw new Error(error.message || `HTTP error! status: ${response.status}`);
    }

    // For 204 No Content responses
    if (response.status === 204) return {} as T;

    return response.json();
  },

  // Helper methods
  get: <T>(url: string) => baseApi.request<T>(url, { method: "GET" }),
  post: <T>(url: string, body: any) => baseApi.request<T>(url, { method: "POST", body: JSON.stringify(body) }),
  put: <T>(url: string, body: any) => baseApi.request<T>(url, { method: "PUT", body: JSON.stringify(body) }),
  delete: <T>(url: string) => baseApi.request<T>(url, { method: "DELETE" }),
};