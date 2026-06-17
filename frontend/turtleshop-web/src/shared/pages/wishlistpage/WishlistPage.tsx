import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { productApi } from "../../api/productApi";
import { wishlistApi } from "../../api/wishlistApi";

interface WishlistItem {
  wishlistId: number;
  productId: number;
  wishlistItemId?: number;
  name: string;
  price: number | null;
}

export default function WishlistPage() {
  const { isAuthenticated, user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [items, setItems] = useState<WishlistItem[]>([]);
  const [wishlistId, setWishlistId] = useState<number | null>(null);
  const [removingProductId, setRemovingProductId] = useState<number | null>(null);

  useEffect(() => {
    if (!isAuthenticated || !user?.id) {
      setLoading(false);
      return;
    }

    const loadWishlist = async () => {
      try {
        setLoading(true);
        const products = await productApi.getProducts().catch(() => []);
        const wishlist = await wishlistApi.getWishlistByCustomer(user.id);
        setWishlistId(wishlist.wishlistId);

        const wishlistItems = await wishlistApi.getWishlistItems(wishlist.wishlistId);
        const mappedItems = wishlistItems.map((item: any) => {
          const product = products.find((p: any) => p.id === item.productId);
          return {
            ...item,
            name: product?.name || `Product #${item.productId}`,
            price: product?.price ?? null,
          };
        });

        setItems(mappedItems);
        setError(null);
      } catch (error: any) {
        setError(error?.message || "Unable to load wishlist.");
      } finally {
        setLoading(false);
      }
    };

    loadWishlist();
  }, [isAuthenticated, user?.id]);

  const handleRemove = async (productId: number) => {
    if (!wishlistId) return;
    setRemovingProductId(productId);

    try {
      await wishlistApi.deleteWishlistItem(wishlistId, productId);
      setItems((prev) => prev.filter((item) => item.productId !== productId));
    } catch (error) {
      console.error("Failed to remove wishlist item", error);
      setError("Unable to remove item from wishlist.");
    } finally {
      setRemovingProductId(null);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="page">
        <section className="section">
          <div className="status-message">
            <p className="text-muted">You need to be signed in to view your wishlist.</p>
            <Link className="button button-primary" to="/login">
              Login now
            </Link>
          </div>
        </section>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="page">
        <section className="section">
          <div>Loading wishlist...</div>
        </section>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page">
        <section className="section">
          <div>Error: {error}</div>
        </section>
      </div>
    );
  }

  return (
    <div className="page">
      <section className="section">
        <div className="section-heading">
          <div>
            <h2>Wishlist</h2>
            <p className="text-muted">Keep track of items you want to save for later.</p>
          </div>
          <Link to="/products" className="button button-secondary">
            Browse products
          </Link>
        </div>

        {items.length === 0 ? (
          <div className="form-panel">
            <p>Your wishlist is currently empty.</p>
            <p className="text-muted">
              Add favorite products from the products page and come back here to view them later.
            </p>
            <Link to="/products" className="button button-primary">
              Add products
            </Link>
          </div>
        ) : (
          <div className="grid-list">
            {items.map((item) => (
              <article key={item.wishlistItemId ?? `${item.wishlistId}-${item.productId}`} className="product-card">
                <div className="product-card-body">
                  <h3>{item.name}</h3>
                  <p className="text-muted">Product #{item.productId}</p>
                  <p>{item.price !== null ? `$${item.price.toFixed(2)}` : "Price unavailable"}</p>
                </div>
                <div className="product-card-actions">
                  <button
                    type="button"
                    className="button button-ghost"
                    disabled={removingProductId === item.productId}
                    onClick={() => handleRemove(item.productId)}
                  >
                    Remove
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}