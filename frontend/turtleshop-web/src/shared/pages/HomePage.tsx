import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ProductCard } from "../components/ProductCard";
import { productApi } from "../api/productApi";
import type { Product } from "../api/productApi";

export default function HomePage() {
    const [featured, setFeatured] = useState<Product[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        productApi
            .getProducts()
            .then((products) => setFeatured(products.slice(0, 4)))
            .catch((err) => setError(err.message || "Unable to load products."))
            .finally(() => setLoading(false));
    }, []);

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
                    <div className="hero-panel">
                        <p>Warm beige and soft gray tones create a calm and elevated shopping experience.</p>
                    </div>
                </div>
            </section>

            <section className="section">
                <div className="section-heading">
                    <h2>Featured products</h2>
                    <Link to="/products" className="text-link">
                        See all products
                    </Link>
                </div>

                {loading ? (
                    <div className="status-message">Loading featured items...</div>
                ) : error ? (
                    <div className="status-message status-error">{error}</div>
                ) : (
                    <div className="grid grid-4">
                        {featured.map((product) => (
                            <ProductCard key={product.id} product={product} />
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}