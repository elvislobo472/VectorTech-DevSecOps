'use client';

import type { Product } from '@/lib/types';
import Link from 'next/link';
import Image from 'next/image';
import { usePathname, useRouter } from 'next/navigation';
import { useState } from 'react';
import { useCart } from '@/components/providers/CartProvider';
import { useAuth } from '@/components/providers/AuthProvider';

interface ProductCardProps {
  product: Product;
}

export function ProductCard({ product }: ProductCardProps) {
  const router = useRouter();
  const pathname = usePathname();
  const { addItem, isLoading } = useCart();
  const { isAuthenticated } = useAuth();
  const [error, setError] = useState('');

  const handleAddToCart = async (event: React.MouseEvent<HTMLButtonElement>) => {
    event.preventDefault();
    event.stopPropagation();
    setError('');

    if (!isAuthenticated) {
      router.push(`/login?redirect=${encodeURIComponent(pathname || '/products')}`);
      return;
    }

    try {
      await addItem(product.id, 1);
      router.push('/cart');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to add item to cart.');
    }
  };

  return (
    <Link href={`/products/${product.id}`}>
      <div className="h-full overflow-hidden rounded-xl border border-blue-900/40 bg-slate-900/70 shadow-md shadow-blue-950/40 transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:shadow-blue-900/40 cursor-pointer">
        <div className="relative h-64 overflow-hidden bg-slate-800">
          <Image
            src={product.imageUrl}
            alt={product.name}
            fill
            className="object-cover hover:scale-105 transition-transform duration-300"
          />
          {!product.inStock && (
            <div className="absolute inset-0 bg-black bg-opacity-40 flex items-center justify-center">
              <span className="text-white font-bold text-lg">Out of Stock</span>
            </div>
          )}
        </div>
        <div className="p-4">
          <p className="mb-1 text-sm text-blue-300/90">{product.category}</p>
          <h3 className="mb-2 line-clamp-2 text-lg font-semibold text-slate-100">
            {product.name}
          </h3>
          <div className="flex items-center justify-between mb-3">
            <span className="text-2xl font-bold text-blue-300">₹{product.price}</span>
            <div className="flex items-center">
              <span className="text-yellow-400 mr-1">★</span>
              <span className="text-sm text-slate-300">{product.rating}</span>
            </div>
          </div>
          <button
            onClick={handleAddToCart}
            className={`w-full py-2 rounded-lg font-semibold transition-colors ${
              product.inStock
                ? 'bg-blue-600 text-white hover:bg-blue-500'
                : 'bg-slate-700 text-slate-400 cursor-not-allowed'
            }`}
            disabled={!product.inStock || isLoading}
          >
            {product.inStock ? (isLoading ? 'Adding…' : 'Add to Cart') : 'Out of Stock'}
          </button>
          {error && <p className="mt-2 text-sm text-red-400">{error}</p>}
        </div>
      </div>
    </Link>
  );
}
