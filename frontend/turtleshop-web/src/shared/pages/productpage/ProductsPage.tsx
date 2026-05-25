import { useEffect, useState } from "react";
import { ProductCard } from "../../components/product/ProductCard";
import { productApi } from "../../api/productApi";
import type { Product } from "../../api/productApi";

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    productApi
      .getProducts()
      .then((items) => setProducts(items))
      .catch((err) => setError(err.message || "Unable to load products."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="page">
      <section className="section">
        <div className="section-heading">
          <div>
            <h2>Products</h2>
            <p className="text-muted">Browse all available items in the TurtleShop catalog.</p>
          </div>
        </div>

        {loading ? (
          <div className="status-message">Loading products...</div>
        ) : error ? (
          <div className="status-message status-error">{error}</div>
        ) : products.length === 0 ? (
          <div className="status-message">No products are available at the moment.</div>
        ) : (
          <div className="grid grid-4">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
