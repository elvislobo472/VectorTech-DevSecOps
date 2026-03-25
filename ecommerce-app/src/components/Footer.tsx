'use client';

import Link from 'next/link';

export function Footer() {
  return (
    <footer className="mt-16 border-t border-blue-900/50 bg-gradient-to-b from-slate-950 via-blue-950 to-slate-950 text-slate-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid md:grid-cols-4 gap-8 mb-8">
          <div>
            <h3 className="text-lg font-bold mb-4 text-blue-300">VectorTech</h3>
            <p className="text-slate-300">Your one-stop shop for premium tech products.</p>
          </div>
          <div>
            <h4 className="font-semibold mb-4">Quick Links</h4>
            <ul className="text-slate-300 space-y-2">
              <li><Link href="/" className="hover:text-blue-300 transition">Home</Link></li>
              <li><Link href="/products" className="hover:text-blue-300 transition">Products</Link></li>
              <li><Link href="/about" className="hover:text-blue-300 transition">About</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="font-semibold mb-4">Support</h4>
            <ul className="text-slate-300 space-y-2">
              <li><Link href="/products" className="hover:text-blue-300 transition">Contact Us</Link></li>
              <li><Link href="/products" className="hover:text-blue-300 transition">FAQ</Link></li>
              <li><Link href="/products" className="hover:text-blue-300 transition">Shipping Info</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="font-semibold mb-4">Follow Us</h4>
            <ul className="text-slate-300 space-y-2">
              <li><a href="#" className="hover:text-blue-300 transition">Twitter</a></li>
              <li><a href="#" className="hover:text-blue-300 transition">Facebook</a></li>
              <li><a href="#" className="hover:text-blue-300 transition">Instagram</a></li>
            </ul>
          </div>
        </div>
        <div className="border-t border-blue-900/60 pt-8 text-center text-slate-400">
          <p>&copy; 2026 VectorTech. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
}
