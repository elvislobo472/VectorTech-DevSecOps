'use client';

import { getProductById, getProducts } from '@/lib/api';
import { notFound } from 'next/navigation';
import Link from 'next/link';
import Image from 'next/image';
import { useEffect, useMemo, useState } from 'react';
import { useParams, usePathname, useRouter } from 'next/navigation';
import type { Product } from '@/lib/types';
import { useCart } from '@/components/providers/CartProvider';
import { useAuth } from '@/components/providers/AuthProvider';

export default function ProductDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const pathname = usePathname();
  const { isAuthenticated } = useAuth();
  const { addItem, isLoading: cartLoading } = useCart();
  const [product, setProduct] = useState<Product | null>(null);
  const [catalog, setCatalog] = useState<Product[]>([]);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const productId = Number(params.id);

    Promise.all([getProductById(productId), getProducts()])
      .then(([currentProduct, allProducts]) => {
        setProduct(currentProduct);
        setCatalog(allProducts);
      })
      .catch((err) => setError(err instanceof Error ? err.message : 'Unable to load product.'))
      .finally(() => setLoading(false));
  }, [params.id]);

  if (!loading && !product && !error) {
    notFound();
  }

  const relatedProducts = useMemo(
    () => catalog.filter((p) => p.category === product?.category && p.id !== product?.id).slice(0, 3),
    [catalog, product],
  );

  const handleAddToCart = async () => {
    if (!product) return;

    setError('');

    if (!isAuthenticated) {
      router.push(`/login?redirect=${encodeURIComponent(pathname || `/products/${product.id}`)}`);
      return;
    }

    try {
      await addItem(product.id, quantity);
      router.push('/cart');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to add item to cart.');
    }
  };

  if (loading) {
    return <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 text-slate-300">Loading product…</div>;
  }

  if (!product) {
    return <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 text-red-300">{error || 'Product not found.'}</div>;
  }

  return (
    <div>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Breadcrumb */}
        <div className="flex items-center gap-2 mb-8 text-sm text-slate-300">
          <Link href="/" className="hover:text-blue-300">
            Home
          </Link>
          <span>/</span>
          <Link href="/products" className="hover:text-blue-300">
            Products
          </Link>
          <span>/</span>
          <span className="text-slate-100 font-semibold">{product.name}</span>
        </div>

        {/* Product Details */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
          {/* Product Image */}
          <div className="rounded-lg overflow-hidden sticky top-20 h-fit border border-blue-900/40 bg-slate-900/70">
            <div className="relative h-96 md:h-full">
              <Image
                src={product.imageUrl}
                alt={product.name}
                fill
                className="object-cover"
              />
            </div>
          </div>

          {/* Product Info */}
          <div>
            <div className="mb-4">
              <span className="inline-block rounded-full bg-blue-900/60 text-blue-200 px-3 py-1 text-sm font-semibold">
                {product.category}
              </span>
            </div>

            <h1 className="text-4xl font-bold text-slate-100 mb-4">{product.name}</h1>

            {/* Rating */}
            <div className="flex items-center gap-4 mb-6">
              <div className="flex items-center">
                <span className="text-yellow-400 text-xl">★</span>
                <span className="ml-2 text-lg font-semibold text-slate-100">{product.rating}</span>
                <span className="ml-2 text-slate-300">(Based on customer reviews)</span>
              </div>
            </div>

            {/* Price */}
            <div className="mb-6">
              <p className="text-5xl font-bold text-blue-300 mb-2">₹{product.price}</p>
              <p className="text-slate-300">Free shipping on orders over ₹2,500</p>
            </div>

            {/* Stock Status */}
            <div className="mb-6">
              {product.inStock ? (
                <p className="text-green-600 font-semibold flex items-center gap-2">
                  <span className="w-2 h-2 bg-green-600 rounded-full"></span>
                  In Stock
                </p>
              ) : (
                <p className="text-red-600 font-semibold flex items-center gap-2">
                  <span className="w-2 h-2 bg-red-600 rounded-full"></span>
                  Out of Stock
                </p>
              )}
            </div>

            {/* Description */}
            <div className="mb-8">
              <h3 className="text-lg font-semibold text-slate-100 mb-3">Description</h3>
              <p className="text-slate-300 leading-relaxed">{product.description}</p>
            </div>

            {/* Quantity and Add to Cart */}
            <div className="flex gap-4 mb-8">
              <div className="flex items-center border border-blue-900/40 rounded-lg bg-slate-900/70">
                <button
                  onClick={() => setQuantity(Math.max(1, quantity - 1))}
                  className="px-4 py-2 text-slate-300 hover:bg-slate-800"
                >
                  −
                </button>
                <span className="px-6 py-2 font-semibold text-slate-100">{quantity}</span>
                <button
                  onClick={() => setQuantity(quantity + 1)}
                  className="px-4 py-2 text-slate-300 hover:bg-slate-800"
                >
                  +
                </button>
              </div>

              <button
                onClick={handleAddToCart}
                disabled={!product.inStock}
                className={`flex-1 py-3 rounded-lg font-semibold text-white transition-colors ${
                  product.inStock
                    ? 'bg-blue-600 hover:bg-blue-500'
                    : 'bg-slate-700 cursor-not-allowed'
                }`}
              >
                {product.inStock ? (cartLoading ? 'Adding…' : 'Add to Cart') : 'Out of Stock'}
              </button>
            </div>
            {error && <p className="mb-6 text-sm text-red-400">{error}</p>}

            {/* Features */}
            <div className="rounded-lg border border-blue-900/40 bg-slate-900/70 p-6 mb-8">
              <h3 className="font-semibold text-slate-100 mb-4">Why Choose This Product?</h3>
              <ul className="space-y-2 text-slate-300">
                <li className="flex items-center gap-2">
                  <span className="text-green-600">✓</span> Premium quality materials
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-green-600">✓</span> 2-year warranty included
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-green-600">✓</span> 30-day money-back guarantee
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-green-600">✓</span> Free technical support
                </li>
              </ul>
            </div>
          </div>
        </div>

        {/* Related Products */}
        {relatedProducts.length > 0 && (
          <div>
            <h2 className="text-3xl font-bold text-slate-100 mb-8">Related Products</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              {relatedProducts.map((related) => (
                <Link key={related.id} href={`/products/${related.id}`}>
                  <div className="h-full overflow-hidden rounded-lg border border-blue-900/40 bg-slate-900/70 shadow-md shadow-blue-950/30 hover:shadow-xl hover:shadow-blue-900/40 transition-shadow cursor-pointer">
                    <div className="relative h-48 bg-slate-800 overflow-hidden">
                      <Image
                        src={related.imageUrl}
                        alt={related.name}
                        fill
                        className="object-cover hover:scale-105 transition-transform"
                      />
                    </div>
                    <div className="p-4">
                      <h3 className="font-semibold text-slate-100 mb-2 line-clamp-2">
                        {related.name}
                      </h3>
                      <p className="text-2xl font-bold text-blue-300">₹{related.price}</p>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
