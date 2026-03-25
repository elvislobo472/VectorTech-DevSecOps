import Link from 'next/link';

export default function AboutPage() {
  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
      <section className="rounded-2xl border border-blue-900/40 bg-gradient-to-r from-slate-950 via-blue-950 to-slate-950 p-8 md:p-12 mb-10">
        <h1 className="text-4xl md:text-5xl font-bold text-slate-100 mb-4">About VectorTech</h1>
        <p className="text-slate-300 text-lg leading-relaxed max-w-3xl">
          VectorTech is focused on premium electronics and accessories with fast shipping, trusted quality,
          and support that keeps your setup running at its best.
        </p>
      </section>

      <section className="grid md:grid-cols-3 gap-6 mb-10">
        <div className="rounded-xl border border-blue-900/40 bg-slate-900/70 p-6">
          <h2 className="text-xl font-semibold text-slate-100 mb-2">Mission</h2>
          <p className="text-slate-300">Bring reliable, high-performance tech to everyday buyers at transparent prices.</p>
        </div>
        <div className="rounded-xl border border-blue-900/40 bg-slate-900/70 p-6">
          <h2 className="text-xl font-semibold text-slate-100 mb-2">Quality Promise</h2>
          <p className="text-slate-300">Every product is selected for durability, practical features, and user value.</p>
        </div>
        <div className="rounded-xl border border-blue-900/40 bg-slate-900/70 p-6">
          <h2 className="text-xl font-semibold text-slate-100 mb-2">Support</h2>
          <p className="text-slate-300">Responsive pre-sales guidance and post-purchase support for confidence at checkout.</p>
        </div>
      </section>

      <section className="rounded-xl border border-blue-900/40 bg-slate-900/70 p-8 text-center">
        <h3 className="text-2xl font-bold text-slate-100 mb-3">Explore the Collection</h3>
        <p className="text-slate-300 mb-6">Browse all categories and discover your next upgrade.</p>
        <Link
          href="/products"
          className="inline-flex items-center justify-center rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white transition-colors hover:bg-blue-500"
        >
          View Products
        </Link>
      </section>
    </div>
  );
}
