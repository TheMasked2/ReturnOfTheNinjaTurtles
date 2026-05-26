import { useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { cartApi } from "../../api/cartApi";
import { wishlistApi } from "../../api/wishlistApi";
import { LoginPromptModal } from "../modal/LoginPromptModal";
import type { Product } from "../../api/productApi";

interface ProductCardProps {
  product: Product;
}

export function ProductCard({ product }: ProductCardProps) {
  const { isAuthenticated } = useAuth();
  const [isWishlistLoading, setIsWishlistLoading] = useState(false);
  const [isCartLoading, setIsCartLoading] = useState(false);
  const [showLoginModal, setShowLoginModal] = useState(false);

  const handleAddToWishlist = async () => {
    if (!isAuthenticated) {
      setShowLoginModal(true);
      return;
    }

    setIsWishlistLoading(true);
    try {
      await wishlistApi.addItem(product.id);
    } catch (error) {
      console.error("Failed to add wishlist item", error);
    } finally {
      setIsWishlistLoading(false);
    }
  };

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      setShowLoginModal(true);
      return;
    }

    setIsCartLoading(true);
    try {
      await cartApi.addItem(product.id);
    } catch (error) {
      console.error("Failed to add cart item", error);
    } finally {
      setIsCartLoading(false);
    }
  };

  return (
    <>
      <article className="card product-card">
        <div className="product-card-header">
          <div>
            <h3>{product.name}</h3>
            <p>{product.description}</p>
          </div>
          <div className="product-card-actions">
            <button
              type="button"
              className="icon-button"
              aria-label="Add to wishlist"
              onClick={handleAddToWishlist}
              disabled={isWishlistLoading}
            >
              ❤️
            </button>
            <button
              type="button"
              className="icon-button"
              aria-label="Add to cart"
              onClick={handleAddToCart}
              disabled={isCartLoading}
            >
              🛒
            </button>
          </div>
        </div>

        <div className="product-meta">
          <span className="price-tag">${product.price.toFixed(2)}</span>
        </div>
        <Link to={`/products/${product.id}`} className="button button-secondary">
          View details
        </Link>
      </article>

      <LoginPromptModal isOpen={showLoginModal} onClose={() => setShowLoginModal(false)} />
    </>
  );
}