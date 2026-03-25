"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useAuth } from "@/components/providers/AuthProvider";
import { useCart } from "@/components/providers/CartProvider";
import { checkout } from "@/lib/api";

const currency = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 2,
});

export default function CheckoutPage() {
  const router = useRouter();
  const { isAuthenticated, token } = useAuth();
  const { cart, refreshCart } = useCart();
  const [placingOrder, setPlacingOrder] = useState(false);
  const [error, setError] = useState("");

  if (!isAuthenticated) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-20 text-center">
        <h1 className="text-4xl font-bold text-slate-100 mb-4">Sign in to checkout</h1>
        <p className="text-slate-300 mb-8">You need an account before placing an order.</p>
        <Link href="/login?redirect=/checkout" className="rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white hover:bg-blue-500 transition-colors">
          Sign in
        </Link>
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-20 text-center">
        <h1 className="text-4xl font-bold text-slate-100 mb-4">Nothing to checkout</h1>
        <p className="text-slate-300 mb-8">Add a few products to your cart first.</p>
        <Link href="/products" className="rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white hover:bg-blue-500 transition-colors">
          Browse products
        </Link>
      </div>
    );
  }

  const placeOrder = async () => {
    if (!token) return;

    setError("");
    setPlacingOrder(true);
    try {
      const order = await checkout(token);
      await refreshCart();
      router.push(`/orders?placed=${order.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to place order.");
    } finally {
      setPlacingOrder(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
      <div className="mb-10">
        <h1 className="text-5xl font-bold text-slate-100 mb-3">Checkout</h1>
        <p className="text-slate-300">Confirm your order and place it securely.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 rounded-2xl border border-blue-900/40 bg-slate-900/70 p-6">
          <h2 className="text-2xl font-bold text-slate-100 mb-6">Order Items</h2>
          <div className="space-y-4">
            {cart.items.map((item) => (
              <div key={item.id} className="flex items-center justify-between gap-4 border-b border-blue-900/30 pb-4 last:border-b-0 last:pb-0">
                <div>
                  <p className="font-semibold text-slate-100">{item.productName}</p>
                  <p className="text-sm text-slate-400">Qty: {item.quantity}</p>
                </div>
                <p className="font-semibold text-blue-300">{currency.format(item.subtotal)}</p>
              </div>
            ))}
          </div>
        </div>

        <aside className="rounded-2xl border border-blue-900/40 bg-slate-900/70 p-6 h-fit sticky top-24">
          <h2 className="text-2xl font-bold text-slate-100 mb-6">Payment Summary</h2>
          <div className="space-y-3 text-slate-300">
            <div className="flex items-center justify-between">
              <span>Total items</span>
              <span>{cart.totalItems}</span>
            </div>
            <div className="flex items-center justify-between">
              <span>Subtotal</span>
              <span>{currency.format(cart.totalAmount)}</span>
            </div>
            <div className="flex items-center justify-between">
              <span>Shipping</span>
              <span className="text-green-400">Free</span>
            </div>
          </div>
          <div className="mt-6 border-t border-blue-900/40 pt-6 flex items-center justify-between text-lg font-bold text-slate-100">
            <span>Grand total</span>
            <span>{currency.format(cart.totalAmount)}</span>
          </div>
          {error && <p className="mt-4 text-sm text-red-400">{error}</p>}
          <button
            type="button"
            onClick={placeOrder}
            disabled={placingOrder}
            className="mt-6 w-full rounded-lg bg-blue-600 py-3 font-semibold text-white hover:bg-blue-500 disabled:opacity-60 transition-colors"
          >
            {placingOrder ? 'Placing order…' : 'Place order'}
          </button>
        </aside>
      </div>
    </div>
  );
}
