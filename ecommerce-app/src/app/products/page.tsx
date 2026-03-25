'use client';

import { getProducts } from '@/lib/api';
import { ProductCard } from '@/components/ProductCard';
import { useEffect, useMemo, useState } from 'react';
import type { Product } from '@/lib/types';

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState<'featured' | 'price-low' | 'price-high' | 'rating'>('featured');

  useEffect(() => {
    getProducts()
      .then(setProducts)
      .catch((err) => setError(err instanceof Error ? err.message : 'Unable to load products.'))
      .finally(() => setLoading(false));
  }, []);

  const categories = Array.from(new Set(products.map((p) => p.category)));

  const filteredProducts = useMemo(() => {
    const query = searchQuery.trim().toLowerCase();

    const base = products.filter((product) => {
      const categoryMatch = selectedCategory ? product.category === selectedCategory : true;
      const queryMatch = query
        ? product.name.toLowerCase().includes(query) || product.description.toLowerCase().includes(query)
        : true;

      return categoryMatch && queryMatch;
    });

    if (sortBy === 'price-low') {
      return [...base].sort((a, b) => a.price - b.price);
    }

    if (sortBy === 'price-high') {
      return [...base].sort((a, b) => b.price - a.price);
    }

    if (sortBy === 'rating') {
      return [...base].sort((a, b) => b.rating - a.rating);
    }

    return base;
  }, [products, searchQuery, selectedCategory, sortBy]);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
      {/* Page Header */}
      <div className="mb-12">
        <h1 className="text-5xl font-bold text-slate-100 mb-4">All Products</h1>
        <p className="text-xl text-slate-300">Browse our complete collection of tech products</p>
      </div>

      {/* Search + Sort */}
      <div className="mb-8 rounded-xl border border-blue-900/40 bg-slate-900/70 p-4 md:p-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <input
            type="text"
            placeholder="Search products..."
            value={searchQuery}
            onChange={(event) => setSearchQuery(event.target.value)}
            className="md:col-span-2 rounded-lg border border-blue-900/40 bg-slate-950 px-4 py-3 text-slate-100 placeholder:text-slate-400 focus:border-blue-500 focus:outline-none"
          />
          <select
            value={sortBy}
            onChange={(event) => setSortBy(event.target.value as 'featured' | 'price-low' | 'price-high' | 'rating')}
            className="rounded-lg border border-blue-900/40 bg-slate-950 px-4 py-3 text-slate-100 focus:border-blue-500 focus:outline-none"
          >
            <option value="featured">Sort: Featured</option>
            <option value="price-low">Price: Low to High</option>
            <option value="price-high">Price: High to Low</option>
            <option value="rating">Rating: High to Low</option>
          </select>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
        {/* Sidebar - Category Filter */}
        <div className="lg:col-span-1">
          <div className="rounded-lg border border-blue-900/40 bg-slate-900/70 p-6 sticky top-20">
            <h3 className="text-lg font-semibold text-slate-100 mb-4">Categories</h3>
            <div className="space-y-2">
              <button
                onClick={() => setSelectedCategory(null)}
                className={`block w-full text-left px-4 py-2 rounded-lg transition-colors ${
                  selectedCategory === null
                    ? 'bg-blue-600 text-white'
                    : 'text-slate-300 hover:bg-slate-800'
                }`}
              >
                All Products
              </button>
              {categories.map((category) => (
                <button
                  key={category}
                  onClick={() => setSelectedCategory(category)}
                  className={`block w-full text-left px-4 py-2 rounded-lg transition-colors ${
                    selectedCategory === category
                      ? 'bg-blue-600 text-white'
                      : 'text-slate-300 hover:bg-slate-800'
                  }`}
                >
                  {category}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Products Grid */}
        <div className="lg:col-span-3">
          <div className="mb-6 flex items-center justify-between gap-4 text-slate-300">
            <span className="rounded-full bg-blue-900/50 px-4 py-2 text-sm">Total catalog: {products.length}</span>
            Showing {filteredProducts.length} products
          </div>
          {loading ? (
            <div className="rounded-xl border border-blue-900/40 bg-slate-900/70 p-6 text-slate-300">Loading products…</div>
          ) : error ? (
            <div className="rounded-xl border border-red-900/50 bg-red-950/30 p-6 text-red-300">{error}</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              {filteredProducts.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          )}

          {!loading && !error && filteredProducts.length === 0 && (
            <div className="text-center py-12">
              <p className="text-xl text-slate-300">No products found for this selection.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
