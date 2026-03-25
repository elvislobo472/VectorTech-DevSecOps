'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/components/providers/AuthProvider';

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuth();
  const [redirectTo, setRedirectTo] = useState('/products');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const redirect = new URLSearchParams(window.location.search).get('redirect');
    if (redirect) {
      setRedirectTo(redirect);
    }
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!email || !password) {
      setError('Please fill in all fields.');
      return;
    }

    setLoading(true);
    try {
      await login(email, password);
      router.push(redirectTo);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Invalid email or password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4 py-16">
      <div className="w-full max-w-md">
        {/* Card */}
        <div className="rounded-2xl border border-blue-900/50 bg-slate-900/80 shadow-2xl shadow-blue-950/50 backdrop-blur p-8">
          {/* Logo */}
          <div className="mb-8 text-center">
            <Link href="/" className="text-3xl font-bold bg-gradient-to-r from-blue-300 to-blue-500 bg-clip-text text-transparent">
              VectorTech
            </Link>
            <p className="mt-2 text-slate-400 text-sm">Welcome back — sign in to continue</p>
          </div>

          {error && (
            <div className="mb-6 rounded-lg bg-red-900/40 border border-red-700/50 px-4 py-3 text-sm text-red-300">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-slate-300 mb-1.5">
                Email address
              </label>
              <input
                id="email"
                type="email"
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                className="w-full rounded-lg border border-blue-900/40 bg-slate-950 px-4 py-3 text-slate-100 placeholder:text-slate-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500/40 focus:outline-none transition-colors"
              />
            </div>

            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label htmlFor="password" className="block text-sm font-medium text-slate-300">
                  Password
                </label>
                <a href="#" className="text-xs text-blue-400 hover:text-blue-300 transition-colors">
                  Forgot password?
                </a>
              </div>
              <div className="relative">
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full rounded-lg border border-blue-900/40 bg-slate-950 px-4 py-3 pr-12 text-slate-100 placeholder:text-slate-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500/40 focus:outline-none transition-colors"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-3 flex items-center text-slate-400 hover:text-slate-200 transition-colors"
                  aria-label="Toggle password visibility"
                >
                  {showPassword ? '🙈' : '👁️'}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-lg bg-blue-600 py-3 font-semibold text-white transition-colors hover:bg-blue-500 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loading ? 'Signing in…' : 'Sign in'}
            </button>
          </form>

          {/* Divider */}
          <div className="my-6 flex items-center gap-3">
            <div className="h-px flex-1 bg-blue-900/50" />
            <span className="text-xs text-slate-500">or continue with</span>
            <div className="h-px flex-1 bg-blue-900/50" />
          </div>

          {/* Social buttons */}
          <div className="grid grid-cols-2 gap-3">
            <button className="flex items-center justify-center gap-2 rounded-lg border border-blue-900/40 bg-slate-800/60 px-4 py-2.5 text-sm font-medium text-slate-200 hover:bg-slate-700/60 transition-colors">
              <span>G</span> Google
            </button>
            <button className="flex items-center justify-center gap-2 rounded-lg border border-blue-900/40 bg-slate-800/60 px-4 py-2.5 text-sm font-medium text-slate-200 hover:bg-slate-700/60 transition-colors">
              <span>󰊤</span> GitHub
            </button>
          </div>

          <p className="mt-8 text-center text-sm text-slate-400">
            Don&apos;t have an account?{' '}
            <Link href="/signup" className="font-medium text-blue-400 hover:text-blue-300 transition-colors">
              Sign up
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
