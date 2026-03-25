"use client";

import Link from "next/link";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { useCart } from "@/components/providers/CartProvider";
import { useAuth } from "@/components/providers/AuthProvider";

const currency = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 2,
});

export default function CartPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const { cart, isLoading, updateItemQuantity, removeItem } = useCart();

  if (authLoading) {
    return <div className="max-w-5xl mx-auto px-4 py-16 text-slate-300">Loading cart…</div>;
  }

  if (!isAuthenticated) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-20 text-center">
        <h1 className="text-4xl font-bold text-slate-100 mb-4">Your cart is waiting</h1>
        <p className="text-slate-300 mb-8">Sign in to view and manage your shopping cart.</p>
        <Link href="/login?redirect=/cart" className="rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white hover:bg-blue-500 transition-colors">
          Sign in
        </Link>
      </div>
    );
  }

  const items = cart?.items ?? [];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
      <div className="mb-10">
        <h1 className="text-5xl font-bold text-slate-100 mb-3">Shopping Cart</h1>
        <p className="text-slate-300">Review your items and continue to checkout.</p>
      </div>

      {items.length === 0 ? (
        <div className="rounded-2xl border border-blue-900/40 bg-slate-900/70 p-10 text-center">
          <p className="text-xl text-slate-200 mb-6">Your cart is empty.</p>
          <Link href="/products" className="rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white hover:bg-blue-500 transition-colors">
            Continue shopping
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-4">
            {items.map((item) => (
              <div key={item.id} className="rounded-2xl border border-blue-900/40 bg-slate-900/70 p-5 flex flex-col md:flex-row md:items-center gap-5">
                <Image
                  src={item.imageUrl}
                  alt={item.productName}
                  width={112}
                  height={112}
                  className="h-28 w-28 rounded-xl object-cover border border-blue-900/30"
                />
                <div className="flex-1">
                  <Link href={`/products/${item.productId}`} className="text-xl font-semibold text-slate-100 hover:text-blue-300 transition-colors">
                    {item.productName}
                  </Link>
                  <p className="mt-2 text-blue-300 font-semibold">{currency.format(item.price)}</p>
                </div>
                <div className="flex items-center gap-3">
                  <button
                    type="button"
                    onClick={() => updateItemQuantity(item.productId, Math.max(1, item.quantity - 1))}
                    disabled={isLoading}
                    className="rounded-lg border border-blue-900/40 px-3 py-2 text-slate-200 hover:bg-slate-800 disabled:opacity-60"
                  >
                    −
                  </button>
                  <span className="min-w-8 text-center text-slate-100 font-semibold">{item.quantity}</span>
                  <button
                    type="button"
                    onClick={() => updateItemQuantity(item.productId, item.quantity + 1)}
                    disabled={isLoading}
                    className="rounded-lg border border-blue-900/40 px-3 py-2 text-slate-200 hover:bg-slate-800 disabled:opacity-60"
                  >
                    +
                  </button>
                </div>
                <div className="text-right min-w-28">
                  <p className="text-lg font-bold text-slate-100">{currency.format(item.subtotal)}</p>
                  <button
                    type="button"
                    onClick={() => removeItem(item.productId)}
                    disabled={isLoading}
                    className="mt-3 text-sm text-red-400 hover:text-red-300 disabled:opacity-60"
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>

          <aside className="rounded-2xl border border-blue-900/40 bg-slate-900/70 p-6 h-fit sticky top-24">
            <h2 className="text-2xl font-bold text-slate-100 mb-6">Order Summary</h2>
            <div className="space-y-3 text-slate-300">
              <div className="flex items-center justify-between">
                <span>Items</span>
                <span>{cart?.totalItems ?? 0}</span>
              </div>
              <div className="flex items-center justify-between">
                <span>Subtotal</span>
                <span>{currency.format(cart?.totalAmount ?? 0)}</span>
              </div>
              <div className="flex items-center justify-between">
                <span>Shipping</span>
                <span className="text-green-400">Free</span>
              </div>
            </div>
            <div className="mt-6 border-t border-blue-900/40 pt-6 flex items-center justify-between text-lg font-bold text-slate-100">
              <span>Total</span>
              <span>{currency.format(cart?.totalAmount ?? 0)}</span>
            </div>
            <button
              type="button"
              onClick={() => router.push('/checkout')}
              className="mt-6 w-full rounded-lg bg-blue-600 py-3 font-semibold text-white hover:bg-blue-500 transition-colors"
            >
              Proceed to checkout
            </button>
          </aside>
        </div>
      )}
    </div>
  );
}
