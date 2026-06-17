import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { productApi, type Product } from "../../api/productApi";
import { recommendationApi } from "../../api/recommendationApi";

export default function ProductDetailsPage() {
  const { productId } = useParams<{ productId: string }>();
  const [product, setProduct] = useState<Product | null>(null);
  const [relatedProducts, setRelatedProducts] = useState<Product[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!productId) {
      setProduct(null);
      setError("Product not found.");
      setLoading(false);
      return;
    }

    const id = Number(productId);
    if (Number.isNaN(id)) {
      setProduct(null);
      setError("Invalid product identifier.");
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    setProduct(null);
    setRelatedProducts([]);

    const loadProductAndSuggestions = async () => {
      try {
        const productItem = await productApi.getProductById(id);
        setProduct(productItem);

        const recs = await recommendationApi.getFrequentlyBoughtTogether(id, 4);
        const ids = recs.map((rec) => rec.productId).filter(Boolean);
        const related = ids.length ? await productApi.getProductsByIds(ids) : [];
        setRelatedProducts(related);
      } catch (err: any) {
        setError(err?.message || "Product not found.");
      } finally {
        setLoading(false);
      }
    };

    loadProductAndSuggestions();
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
            {relatedProducts.length > 0 ? (
              <div className="grid grid-4">
                {relatedProducts.map((related) => (
                  <div key={related.id} className="suggested-card card">
                    <h4>{related.name}</h4>
                    <p className="text-muted">${related.price.toFixed(2)}</p>
                    <Link to={`/products/${related.id}`} className="text-link">
                      View product
                    </Link>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-muted">No related products available right now.</div>
            )}
          </div>
        </div>
      </section>
    </div>
  );
}