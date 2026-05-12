import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { productApi, Product } from "../api/productApi";

export default function ProductDetailsPage() {
  const params = useParams();
  const productId = params.productId ?? "";
  const [product, setProduct] = useState<Product | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!productId) return;

    productApi
      .getProductById(productId)
      .then((item) => setProduct(item))
      .catch((err) => setError(err.message || "Product not found."))
      .finally(() => setLoading(false));
  }, [productId]);

  if (loading) {
    return <div className="status-message">Loading product...</div>;
  }

  if (error) {
    return <div className="status-message status-error">{error}</div>;
  }

  if (!product) {
    return <div className="status-message">No product details available.</div>;
  }

  return (
    <div className="page">
      <section className="section form-panel">
        <div className="section-heading">
          <div>
            <h2>{product.name}</h2>
            <p className="text-muted">Detailed product information and purchase inspiration.</p>
          </div>
          <Link to="/products" className="text-link">
            Back to products
          </Link>
        </div>

        <div className="product-detail card">
          <div className="product-meta">
            <span className="price-tag">${product.price.toFixed(2)}</span>
            <span className="badge">Available since {new Date(product.availableSince).getFullYear()}</span>
          </div>
          <p>{product.description}</p>
          <div className="form-field">
            <label>Specs</label>
            <textarea readOnly value={product.specs} rows={5} />
          </div>
          <div className="form-field">
            <label>Suggested</label>
            <div>
              {product.suggestedProducts.length > 0 ? (
                product.suggestedProducts.map((suggestion) => (
                  <span key={suggestion} className="badge" style={{ marginRight: 8 }}>
                    {suggestion}
                  </span>
                ))
              ) : (
                <span className="text-muted">No suggestions available</span>
              )}
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
