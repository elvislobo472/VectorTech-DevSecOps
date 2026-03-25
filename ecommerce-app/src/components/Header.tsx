'use client';

import Link from 'next/link';
import { useCart } from '@/components/providers/CartProvider';
import { useAuth } from '@/components/providers/AuthProvider';

export function Header() {
  const { cart } = useCart();
  const { isAuthenticated, user, logout } = useAuth();
  const cartCount = cart?.totalItems ?? 0;

  return (
    <header className="sticky top-0 z-50 border-b border-blue-900/60 bg-slate-950/85 shadow-lg shadow-blue-950/40 backdrop-blur">
      <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex items-center justify-between">
        <Link href="/" className="flex items-center">
          <span className="text-2xl font-bold bg-gradient-to-r from-blue-300 to-blue-500 bg-clip-text text-transparent">VectorTech</span>
        </Link>
        <div className="flex items-center gap-8">
          <ul className="hidden md:flex gap-6">
            <li>
              <Link href="/" className="text-slate-200 hover:text-blue-300 transition-colors">
                Home
              </Link>
            </li>
            <li>
              <Link href="/products" className="text-slate-200 hover:text-blue-300 transition-colors">
                Products
              </Link>
            </li>
            <li>
              <Link href="/about" className="text-slate-200 hover:text-blue-300 transition-colors">
                About
              </Link>
            </li>
            {isAuthenticated && (
              <li>
                <Link href="/orders" className="text-slate-200 hover:text-blue-300 transition-colors">
                  Orders
                </Link>
              </li>
            )}
          </ul>
          <div className="flex items-center gap-3">
            {isAuthenticated ? (
              <>
                <span className="hidden md:inline-flex text-sm text-slate-300">
                  Hi, <span className="ml-1 font-semibold text-slate-100">{user?.name}</span>
                </span>
                <button
                  type="button"
                  onClick={logout}
                  className="hidden md:inline-flex items-center rounded-lg px-4 py-2 text-sm font-medium text-slate-200 hover:text-blue-300 transition-colors"
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link
                  href="/login"
                  className="hidden md:inline-flex items-center rounded-lg px-4 py-2 text-sm font-medium text-slate-200 hover:text-blue-300 transition-colors"
                >
                  Sign in
                </Link>
                <Link
                  href="/signup"
                  className="hidden md:inline-flex items-center rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-500 transition-colors"
                >
                  Sign up
                </Link>
              </>
            )}
            <Link href="/cart" className="relative rounded-full border border-blue-400/30 bg-blue-900/30 p-2 hover:bg-blue-800/40 transition-colors" aria-label="Shopping cart">
              <span className="text-2xl">🛒</span>
              {cartCount > 0 && (
                <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                  {cartCount}
                </span>
              )}
            </Link>
          </div>
        </div>
      </nav>
    </header>
  );
}
