const BASE_URL = "http://localhost:8080/api";

export const baseApi = {
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
      const error = await response.json().catch(() => ({ message: "An error occurred" }));
      throw new Error(error.message || `HTTP error! status: ${response.status}`);
    }

    if (response.status === 204) {
      return {} as T;
    }

    const contentType = response.headers.get("content-type") ?? "";
    if (contentType.includes("application/json")) {
      return response.json();
    }

    return response.text() as unknown as T;
  },

  get: <T>(url: string) => baseApi.request<T>(url, { method: "GET" }),
  post: <T>(url: string, body: any) => baseApi.request<T>(url, { method: "POST", body: JSON.stringify(body) }),
  put: <T>(url: string, body: any) => baseApi.request<T>(url, { method: "PUT", body: JSON.stringify(body) }),
  patch: <T>(url: string, body: any) => baseApi.request<T>(url, { method: "PATCH", body: JSON.stringify(body) }),
  delete: <T>(url: string) => baseApi.request<T>(url, { method: "DELETE" }),
};
