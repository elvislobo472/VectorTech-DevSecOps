"use client";

import {
  addToCart as addToCartRequest,
  getCart as getCartRequest,
  removeCartItem as removeCartItemRequest,
  updateCartItem as updateCartItemRequest,
} from "@/lib/api";
import type { Cart } from "@/lib/types";
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { useAuth } from "@/components/providers/AuthProvider";

interface CartContextValue {
  cart: Cart | null;
  isLoading: boolean;
  refreshCart: () => Promise<void>;
  addItem: (productId: number, quantity?: number) => Promise<void>;
  updateItemQuantity: (productId: number, quantity: number) => Promise<void>;
  removeItem: (productId: number) => Promise<void>;
  clearCart: () => void;
}

const CartContext = createContext<CartContextValue | undefined>(undefined);

export function CartProvider({ children }: { children: ReactNode }) {
  const { token, isAuthenticated } = useAuth();
  const [cart, setCart] = useState<Cart | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const clearCart = useCallback(() => {
    setCart(null);
  }, []);

  const refreshCart = useCallback(async () => {
    if (!token) {
      clearCart();
      return;
    }

    setIsLoading(true);
    try {
      const nextCart = await getCartRequest(token);
      setCart(nextCart);
    } finally {
      setIsLoading(false);
    }
  }, [token, clearCart]);

  const addItem = useCallback(async (productId: number, quantity = 1) => {
    if (!token) {
      throw new Error("You need to sign in to use the cart.");
    }

    setIsLoading(true);
    try {
      const nextCart = await addToCartRequest(token, productId, quantity);
      setCart(nextCart);
    } finally {
      setIsLoading(false);
    }
  }, [token]);

  const updateItemQuantity = useCallback(async (productId: number, quantity: number) => {
    if (!token) {
      throw new Error("You need to sign in to use the cart.");
    }

    setIsLoading(true);
    try {
      const nextCart = await updateCartItemRequest(token, productId, quantity);
      setCart(nextCart);
    } finally {
      setIsLoading(false);
    }
  }, [token]);

  const removeItem = useCallback(async (productId: number) => {
    if (!token) {
      throw new Error("You need to sign in to use the cart.");
    }

    setIsLoading(true);
    try {
      const nextCart = await removeCartItemRequest(token, productId);
      setCart(nextCart);
    } finally {
      setIsLoading(false);
    }
  }, [token]);

  useEffect(() => {
    if (isAuthenticated) {
      refreshCart().catch(() => clearCart());
      return;
    }

    clearCart();
  }, [isAuthenticated, refreshCart, clearCart]);

  const value = useMemo<CartContextValue>(() => ({
    cart,
    isLoading,
    refreshCart,
    addItem,
    updateItemQuantity,
    removeItem,
    clearCart,
  }), [cart, isLoading, refreshCart, addItem, updateItemQuantity, removeItem, clearCart]);

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const context = useContext(CartContext);

  if (!context) {
    throw new Error("useCart must be used within CartProvider");
  }

  return context;
}
