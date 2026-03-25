'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/components/providers/AuthProvider';

export default function SignupPage() {
  const router = useRouter();
  const { signup } = useAuth();
  const [redirectTo, setRedirectTo] = useState('/products');
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    const redirect = new URLSearchParams(window.location.search).get('redirect');
    if (redirect) {
      setRedirectTo(redirect);
    }
  }, []);

  const update = (field: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!form.name || !form.email || !form.password || !form.confirmPassword) {
      setError('Please fill in all fields.');
      return;
    }

    if (form.password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    try {
      await signup(form.name, form.email, form.password);
      setSuccess(true);
      setTimeout(() => {
        router.push(redirectTo);
      }, 800);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to create your account.');
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center px-4 py-16">
        <div className="w-full max-w-md rounded-2xl border border-blue-900/50 bg-slate-900/80 p-10 text-center shadow-2xl shadow-blue-950/50">
          <div className="mb-4 text-5xl">🎉</div>
          <h2 className="text-2xl font-bold text-slate-100 mb-2">Account created!</h2>
          <p className="text-slate-400 mb-8">Welcome to VectorTech, {form.name}. You can now sign in.</p>
          <Link
            href="/login"
            className="inline-flex items-center justify-center rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white transition-colors hover:bg-blue-500"
          >
            Go to Sign in
          </Link>
        </div>
      </div>
    );
  }

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
            <p className="mt-2 text-slate-400 text-sm">Create your account — it&apos;s free</p>
          </div>

          {error && (
            <div className="mb-6 rounded-lg bg-red-900/40 border border-red-700/50 px-4 py-3 text-sm text-red-300">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-slate-300 mb-1.5">
                Full name
              </label>
              <input
                id="name"
                type="text"
                autoComplete="name"
                value={form.name}
                onChange={update('name')}
                placeholder="John Doe"
                className="w-full rounded-lg border border-blue-900/40 bg-slate-950 px-4 py-3 text-slate-100 placeholder:text-slate-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500/40 focus:outline-none transition-colors"
              />
            </div>

            <div>
              <label htmlFor="email" className="block text-sm font-medium text-slate-300 mb-1.5">
                Email address
              </label>
              <input
                id="email"
                type="email"
                autoComplete="email"
                value={form.email}
                onChange={update('email')}
                placeholder="you@example.com"
                className="w-full rounded-lg border border-blue-900/40 bg-slate-950 px-4 py-3 text-slate-100 placeholder:text-slate-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500/40 focus:outline-none transition-colors"
              />
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-slate-300 mb-1.5">
                Password <span className="text-slate-500 font-normal">(min. 8 characters)</span>
              </label>
              <div className="relative">
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="new-password"
                  value={form.password}
                  onChange={update('password')}
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
              {/* Strength bar */}
              {form.password && (
                <div className="mt-2 flex gap-1">
                  {[...Array(4)].map((_, i) => (
                    <div
                      key={i}
                      className={`h-1 flex-1 rounded-full transition-colors ${
                        form.password.length >= (i + 1) * 3
                          ? form.password.length >= 12
                            ? 'bg-green-500'
                            : form.password.length >= 8
                            ? 'bg-yellow-400'
                            : 'bg-red-500'
                          : 'bg-slate-700'
                      }`}
                    />
                  ))}
                </div>
              )}
            </div>

            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-slate-300 mb-1.5">
                Confirm password
              </label>
              <input
                id="confirmPassword"
                type={showPassword ? 'text' : 'password'}
                autoComplete="new-password"
                value={form.confirmPassword}
                onChange={update('confirmPassword')}
                placeholder="••••••••"
                className={`w-full rounded-lg border px-4 py-3 bg-slate-950 text-slate-100 placeholder:text-slate-500 focus:ring-1 focus:outline-none transition-colors ${
                  form.confirmPassword && form.password !== form.confirmPassword
                    ? 'border-red-600/60 focus:border-red-500 focus:ring-red-500/30'
                    : 'border-blue-900/40 focus:border-blue-500 focus:ring-blue-500/40'
                }`}
              />
              {form.confirmPassword && form.password !== form.confirmPassword && (
                <p className="mt-1.5 text-xs text-red-400">Passwords do not match</p>
              )}
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-lg bg-blue-600 py-3 font-semibold text-white transition-colors hover:bg-blue-500 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loading ? 'Creating account…' : 'Create account'}
            </button>
          </form>

          <p className="mt-4 text-center text-xs text-slate-500">
            By signing up you agree to our{' '}
            <a href="#" className="text-blue-400 hover:underline">Terms</a> and{' '}
            <a href="#" className="text-blue-400 hover:underline">Privacy Policy</a>.
          </p>

          <p className="mt-6 text-center text-sm text-slate-400">
            Already have an account?{' '}
            <Link href="/login" className="font-medium text-blue-400 hover:text-blue-300 transition-colors">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
