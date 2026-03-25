import type {
  ApiResponse,
  AuthPayload,
  Cart,
  LoginRequest,
  Order,
  Product,
  RegisterRequest,
} from "@/lib/types";

// Always use relative URLs on the client so that Nginx handles the routing,
// while allowing absolute URLs on the server/build side where window isn't defined.
const API_BASE_URL = typeof window !== 'undefined' 
  ? "" 
  : (process.env.NEXT_PUBLIC_API_BASE_URL ?? "");

class ApiError extends Error {
  constructor(message: string, readonly status: number) {
    super(message);
    this.name = "ApiError";
  }
}

async function request<T>(path: string, init: RequestInit = {}, token?: string): Promise<T> {
  const headers = new Headers(init.headers);

  if (!headers.has("Content-Type") && init.body) {
    headers.set("Content-Type", "application/json");
  }

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers,
    cache: "no-store",
  });

  const text = await response.text();
  const payload = text ? (JSON.parse(text) as ApiResponse<T>) : null;

  if (!response.ok) {
    throw new ApiError(payload?.message ?? "Something went wrong", response.status);
  }

  return payload?.data as T;
}

export { ApiError, API_BASE_URL };

export function login(input: LoginRequest) {
  return request<AuthPayload>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function signup(input: RegisterRequest) {
  return request<AuthPayload>("/api/auth/register", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function getProducts() {
  return request<Product[]>("/api/products");
}

export function getProductById(id: number) {
  return request<Product>(`/api/products/${id}`);
}

export function searchProducts(query: string) {
  return request<Product[]>(`/api/products/search?query=${encodeURIComponent(query)}`);
}

export function getProductsByCategory(category: string) {
  return request<Product[]>(`/api/products/category/${encodeURIComponent(category)}`);
}

export function getCart(token: string) {
  return request<Cart>("/api/cart", { method: "GET" }, token);
}

export function addToCart(token: string, productId: number, quantity: number) {
  return request<Cart>(
    "/api/cart/add",
    {
      method: "POST",
      body: JSON.stringify({ productId, quantity }),
    },
    token,
  );
}

export function updateCartItem(token: string, productId: number, quantity: number) {
  return request<Cart>(
    "/api/cart/update",
    {
      method: "PUT",
      body: JSON.stringify({ productId, quantity }),
    },
    token,
  );
}

export function removeCartItem(token: string, productId: number) {
  return request<Cart>(`/api/cart/remove/${productId}`, { method: "DELETE" }, token);
}

export function checkout(token: string) {
  return request<Order>("/api/orders/checkout", { method: "POST" }, token);
}

export function getOrders(token: string) {
  return request<Order[]>("/api/orders", { method: "GET" }, token);
}
