import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ProductCard } from "../../components/product/ProductCard";
import { productApi, type Product } from "../../api/productApi";
import { recommendationApi, type RecommendedProduct } from "../../api/recommendationApi";

export default function HomePage() {
  const [heroProducts, setHeroProducts] = useState<Product[]>([]);
  const [seasonalProducts, setSeasonalProducts] = useState<Product[]>([]);
  const [heroIndex, setHeroIndex] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function loadRecommendations() {
      setLoading(true);
      setError(null);

      try {
        const [monthRecs, seasonRecs] = await Promise.all([
          recommendationApi.getPopularThisMonth(4),
          recommendationApi.getPopularThisSeason(4),
        ]);

        const monthIds = monthRecs.map((item) => item.productId);
        const seasonIds = seasonRecs.map((item) => item.productId);

        const [monthProducts, seasonProducts] = await Promise.all([
          monthIds.length ? productApi.getProductsByIds(monthIds) : Promise.resolve([]),
          seasonIds.length ? productApi.getProductsByIds(seasonIds) : Promise.resolve([]),
        ]);

        if (!active) return;
        setHeroProducts(monthProducts);
        setSeasonalProducts(seasonProducts);
      } catch (err: any) {
        setError(err?.message || "Unable to load recommendations.");
      } finally {
        if (active) setLoading(false);
      }
    }

    loadRecommendations();
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (heroProducts.length === 0) return;
    const interval = window.setInterval(() => {
      setHeroIndex((current) => (current + 1) % heroProducts.length);
    }, 5000);
    return () => window.clearInterval(interval);
  }, [heroProducts]);

  const heroProduct = heroProducts[heroIndex];

  return (
    <div className="page page-home">
      <section className="hero-card">
        <div>
          <span className="eyebrow">TurtleShop</span>
          <h1>Shop naturally, shop comfortably.</h1>
          <p>Explore the Ninja Turtle-inspired marketplace built for sleek product browsing, curated collections, and effortless login.</p>
          <div className="hero-actions">
            <Link className="button button-primary" to="/products">
              Browse products
            </Link>
            <Link className="button button-secondary" to="/wishlist">
              View wishlist
            </Link>
          </div>
        </div>
        <div className="hero-preview">
          {loading ? (
            <div className="hero-panel">Loading top products this month...</div>
          ) : heroProduct ? (
            <div className="hero-panel">
              <h2>Trending this month</h2>
              <p>{heroProduct.name}</p>
              <strong>${heroProduct.price.toFixed(2)}</strong>
              <div className="hero-badge">
                {heroProducts.map((_, index) => (
                  <span key={index} className={index === heroIndex ? "active" : ""} />
                ))}
              </div>
            </div>
          ) : (
            <div className="hero-panel">No trending items available right now.</div>
          )}
        </div>
      </section>

      <section className="section">
        <div className="section-heading">
          <h2>Popular items this season</h2>
          <Link to="/products" className="text-link">
            See all products
          </Link>
        </div>

        {loading ? (
          <div className="status-message">Loading popular season items...</div>
        ) : error ? (
          <div className="status-message status-error">{error}</div>
        ) : seasonalProducts.length > 0 ? (
          <div className="grid grid-4">
            {seasonalProducts.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        ) : (
          <div className="status-message">No seasonal recommendations available.</div>
        )}
      </section>
    </div>
  );
}