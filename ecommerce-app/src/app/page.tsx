'use client';

import { getProducts } from '@/lib/api';
import { ProductCard } from '@/components/ProductCard';
import Link from 'next/link';
import { useEffect, useMemo, useState } from 'react';
import type { Product } from '@/lib/types';

export default function Home() {
  const [products, setProducts] = useState<Product[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getProducts()
      .then(setProducts)
      .catch((err) => setError(err instanceof Error ? err.message : 'Unable to load products.'))
      .finally(() => setLoading(false));
  }, []);

  const categories = useMemo(
    () => Array.from(new Set(products.map((product) => product.category))),
    [products],
  );

  return (
    <div>
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-slate-950 via-blue-950 to-blue-900 text-white py-16 border-b border-blue-900/50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h1 className="text-5xl md:text-6xl font-bold mb-4 bg-gradient-to-r from-white to-blue-300 bg-clip-text text-transparent">Welcome to VectorTech</h1>
          <p className="text-xl md:text-2xl text-blue-100 mb-8">
            Discover premium tech products at unbeatable prices
          </p>
          <div className="flex items-center justify-center gap-4 flex-wrap">
            <Link href="/products" className="bg-blue-500 text-white px-8 py-3 rounded-lg font-semibold hover:bg-blue-400 transition-colors">
              Shop Now
            </Link>
            <Link href="/about" className="border border-blue-300/50 px-8 py-3 rounded-lg font-semibold text-blue-100 hover:bg-blue-900/40 transition-colors">
              Learn More
            </Link>
          </div>
        </div>
      </section>

      {/* Featured Products */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="mb-12">
          <h2 className="text-4xl font-bold text-slate-100 mb-2">Featured Products</h2>
          <p className="text-slate-300">Check out our latest and greatest tech products</p>
        </div>

        {loading ? (
          <div className="rounded-xl border border-blue-900/40 bg-slate-900/70 p-6 text-slate-300">Loading products…</div>
        ) : error ? (
          <div className="rounded-xl border border-red-900/50 bg-red-950/30 p-6 text-red-300">{error}</div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {products.slice(0, 6).map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        )}
      </section>

      {/* Categories */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-16">
        <h3 className="text-3xl font-bold text-slate-100 mb-6">Top Categories</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
          {categories.map((category) => (
            <Link
              key={category}
              href="/products"
              className="rounded-xl border border-blue-900/40 bg-slate-900/70 p-5 text-center text-slate-100 font-semibold hover:border-blue-500/60 hover:bg-blue-900/40 transition-colors"
            >
              {category}
            </Link>
          ))}
        </div>
      </section>

      {/* Info Section */}
      <section className="border-t border-blue-900/50 bg-slate-950/80 py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="text-4xl mb-4">🚚</div>
              <h3 className="text-xl font-semibold text-slate-100 mb-2">Fast Shipping</h3>
              <p className="text-slate-300">Free shipping on orders over ₹2,500</p>
            </div>
            <div className="text-center">
              <div className="text-4xl mb-4">🔒</div>
              <h3 className="text-xl font-semibold text-slate-100 mb-2">Secure Payment</h3>
              <p className="text-slate-300">Your payment information is safe with us</p>
            </div>
            <div className="text-center">
              <div className="text-4xl mb-4">↩️</div>
              <h3 className="text-xl font-semibold text-slate-100 mb-2">Easy Returns</h3>
              <p className="text-slate-300">30-day money-back guarantee</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
