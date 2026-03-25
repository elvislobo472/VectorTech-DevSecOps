export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface AuthUser {
  userId: number;
  name: string;
  email: string;
  role: string;
}

export interface AuthPayload extends AuthUser {
  token: string;
  tokenType: string;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  category: string;
  categoryId: number | null;
  imageUrl: string;
  stock: number;
  rating: number;
  inStock: boolean;
  createdAt: string;
}

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  imageUrl: string;
  price: number;
  quantity: number;
  subtotal: number;
}

export interface Cart {
  cartId: number;
  items: CartItem[];
  totalAmount: number;
  totalItems: number;
}

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  imageUrl: string;
  quantity: number;
  price: number;
  subtotal: number;
}

export interface Order {
  id: number;
  userId: number;
  status: string;
  totalAmount: number;
  items: OrderItem[];
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}
