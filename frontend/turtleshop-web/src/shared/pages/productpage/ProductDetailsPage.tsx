import { useEffect, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { cartApi } from "../../api/cartApi";
import { wishlistApi } from "../../api/wishlistApi";
import { productApi, type Product } from "../../api/productApi";
import { recommendationApi } from "../../api/recommendationApi";
import { publishHeaderRefresh } from "../../state/refreshBus";
import { ProductCard } from "../../components/product/ProductCard";

export default function ProductDetailsPage() {
  const { productId } = useParams<{ productId: string }>();
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const [product, setProduct] = useState<Product | null>(null);
  const [relatedProducts, setRelatedProducts] = useState<Product[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [notification, setNotification] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const showNotification = (message: string) => {
    setNotification(message);
    window.setTimeout(() => setNotification(null), 3000);
  };

  const ensureCart = async () => {
    if (!user?.id) return;
    try {
      await cartApi.getActiveCart(user.id);
    } catch (error: any) {
      if (error.message.includes("409") || error.message.includes("no active carts")) {
        await cartApi.createCart(user.id);
      } else {
        throw error;
      }
    }
  };

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }

    if (!user?.id) return;

    setBusy(true);
    try {
      await ensureCart();
      await cartApi.addItem(user.id, Number(productId));
      showNotification("Added to cart! 🛒");
      publishHeaderRefresh();
    } catch (err) {
      console.error("Failed to add product to cart", err);
      setError("Unable to add product to cart right now.");
    } finally {
      setBusy(false);
    }
  };

  const handleAddToWishlist = async () => {
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }

    if (!user?.id) return;

    setBusy(true);
    try {
      let wishlistId: number;
      try {
        const wishlist = await wishlistApi.getWishlistByCustomer(user.id);
        wishlistId = wishlist.wishlistId;
      } catch (error: any) {
        if (error.message.includes("404") || error.message.includes("Wishlist not found")) {
          wishlistId = await wishlistApi.createWishlist(user.id);
        } else {
          throw error;
        }
      }
      await wishlistApi.addItemToWishlist(wishlistId, Number(productId));
      showNotification("Added to wishlist! ❤️");
      publishHeaderRefresh();
    } catch (err) {
      console.error("Failed to add product to wishlist", err);
      setError("Unable to add product to wishlist right now.");
    } finally {
      setBusy(false);
    }
  };

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

    const loadProductAndSuggestions = async () => {
      setLoading(true);
      setError(null);
      setProduct(null);
      setRelatedProducts([]);

      try {
        const productItem = await productApi.getProductById(id);
        setProduct(productItem);

        const recs = await recommendationApi.getFrequentlyBoughtTogether(id, 4);
        const ids = recs.map((rec) => rec.productId).filter(Boolean);
        const related = ids.length ? await productApi.getProductsByIds(ids) : [];
        setRelatedProducts(related);
      } catch (err: any) {
        setError(err?.message || "Unable to load product details.");
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

          <div className="product-detail-actions">
            <button
              type="button"
              className="button button-secondary"
              onClick={handleAddToWishlist}
              disabled={busy}
            >
              Add to wishlist
            </button>
            <button
              type="button"
              className="button button-primary"
              onClick={handleAddToCart}
              disabled={busy}
            >
              Add to cart
            </button>
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
                  <ProductCard
                    key={related.id}
                    product={related}
                    onAddedToCart={() => showNotification("Added related product to cart! 🛒")}
                  />
                ))}
              </div>
            ) : (
              <div className="text-muted">No related products available right now.</div>
            )}
          </div>
        </div>
      </section>

      {notification && (
        <div className="toast-notification">
          {notification}
        </div>
      )}
    </div>
  );
}