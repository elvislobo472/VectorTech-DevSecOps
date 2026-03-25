"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useAuth } from "@/components/providers/AuthProvider";
import { getOrders } from "@/lib/api";
import type { Order } from "@/lib/types";

const currency = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 2,
});

export default function OrdersPage() {
  const { token, isAuthenticated } = useAuth();
  const [orders, setOrders] = useState<Order[]>([]);
  const [hasLoaded, setHasLoaded] = useState(false);
  const [error, setError] = useState("");
  const placedOrderId =
    typeof window !== "undefined" ? new URLSearchParams(window.location.search).get("placed") : null;

  useEffect(() => {
    if (!token) {
      return;
    }

    getOrders(token)
      .then(setOrders)
      .catch((err) => setError(err instanceof Error ? err.message : "Unable to load orders."))
      .finally(() => setHasLoaded(true));
  }, [token]);

  const loading = Boolean(token) && !hasLoaded && !error;

  if (!isAuthenticated) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-20 text-center">
        <h1 className="text-4xl font-bold text-slate-100 mb-4">View your orders</h1>
        <p className="text-slate-300 mb-8">Sign in to see your order history.</p>
        <Link href="/login?redirect=/orders" className="rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white hover:bg-blue-500 transition-colors">
          Sign in
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
      <div className="mb-10">
        <h1 className="text-5xl font-bold text-slate-100 mb-3">Order History</h1>
        <p className="text-slate-300">Track your recent purchases and their status.</p>
      </div>

      {placedOrderId && (
        <div className="mb-8 rounded-xl border border-green-900/50 bg-green-950/30 p-4 text-green-300">
          Order #{placedOrderId} placed successfully.
        </div>
      )}

      {loading ? (
        <div className="rounded-2xl border border-blue-900/40 bg-slate-900/70 p-6 text-slate-300">Loading orders…</div>
      ) : error ? (
        <div className="rounded-2xl border border-red-900/50 bg-red-950/30 p-6 text-red-300">{error}</div>
      ) : orders.length === 0 ? (
        <div className="rounded-2xl border border-blue-900/40 bg-slate-900/70 p-10 text-center">
          <p className="text-xl text-slate-200 mb-6">No orders yet.</p>
          <Link href="/products" className="rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white hover:bg-blue-500 transition-colors">
            Start shopping
          </Link>
        </div>
      ) : (
        <div className="space-y-6">
          {orders.map((order) => (
            <section key={order.id} className="rounded-2xl border border-blue-900/40 bg-slate-900/70 p-6">
              <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-6">
                <div>
                  <h2 className="text-2xl font-bold text-slate-100">Order #{order.id}</h2>
                  <p className="text-sm text-slate-400">Placed on {new Date(order.createdAt).toLocaleString()}</p>
                </div>
                <div className="flex items-center gap-3 flex-wrap">
                  <span className="rounded-full bg-blue-900/50 px-4 py-2 text-sm font-semibold text-blue-200">
                    {order.status}
                  </span>
                  <span className="text-lg font-bold text-slate-100">{currency.format(order.totalAmount)}</span>
                </div>
              </div>

              <div className="space-y-3">
                {order.items.map((item) => (
                  <div key={item.id} className="flex flex-col md:flex-row md:items-center md:justify-between gap-3 border-t border-blue-900/30 pt-3 first:border-t-0 first:pt-0">
                    <div>
                      <p className="font-semibold text-slate-100">{item.productName}</p>
                      <p className="text-sm text-slate-400">Qty: {item.quantity}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-slate-300">{currency.format(item.price)} each</p>
                      <p className="font-semibold text-blue-300">{currency.format(item.subtotal)}</p>
                    </div>
                  </div>
                ))}
              </div>
            </section>
          ))}
        </div>
      )}
    </div>
  );
}
