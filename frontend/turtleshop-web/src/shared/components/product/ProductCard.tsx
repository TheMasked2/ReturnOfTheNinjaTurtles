import { useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { cartApi } from "../../api/cartApi";
import { wishlistApi } from "../../api/wishlistApi";
import { LoginPromptModal } from "../modal/LoginPromptModal";
import { publishHeaderRefresh } from "../../state/refreshBus"
import type { Product } from "../../api/productApi";

interface ProductCardProps {
  product: Product;
  onAddedToCart?: () => void;
}

export function ProductCard({ product, onAddedToCart }: ProductCardProps) {
  const { isAuthenticated, user } = useAuth();
  const [isWishlistLoading, setIsWishlistLoading] = useState(false);
  const [isCartLoading, setIsCartLoading] = useState(false);
  const [showLoginModal, setShowLoginModal] = useState(false);
  
  // 1. Add notification state
  const [notification, setNotification] = useState<string | null>(null);

  // 2. Helper to show and auto-hide the notification
  const showNotification = (message: string) => {
    setNotification(message);
    setTimeout(() => {
      setNotification(null);
    }, 3000);
  };

  const handleAddToWishlist = async () => {
    if (!isAuthenticated) {
      setShowLoginModal(true);
      return;
    }
  
    if (!user?.id) {
      console.error("Missing customer id");
      return;
    }
  
    setIsWishlistLoading(true);
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
  
      await wishlistApi.addItemToWishlist(wishlistId, product.id);
      
      // 3a. Trigger popup on success
      showNotification("Added to wishlist! ❤️");

      publishHeaderRefresh();
      
    } catch (error: any) {
      // Optional: Inform user if item already exists (409 Conflict)
      if (error.message.includes("409") || error.message.includes("already exists")) {
        showNotification("Item is already in your wishlist!");
      } else {
        console.error("Failed to add wishlist item", error);
      }
    } finally {
      setIsWishlistLoading(false);
    }
  };

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      setShowLoginModal(true);
      return;
    }

    if (!user?.id) {
      console.error("Missing customer id");
      return;
    }

    setIsCartLoading(true);
    try {
      try {
        await cartApi.getActiveCart(user.id);
      } catch (error: any) {
        if (error.message.includes("409") || error.message.includes("no active carts")) {
          await cartApi.createCart(user.id);
        } else {
          throw error;
        }
      }

      await cartApi.addItem(user.id, product.id);
      
      // 3b. Trigger popup on success
      showNotification("Added to cart! 🛒");

      publishHeaderRefresh();
      onAddedToCart?.();
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
        </div>

        <div className="product-card-footer">
          <div className="product-meta">
            <span className="price-tag">${product.price.toFixed(2)}</span>
          </div>

          <div className="product-card-action-group">
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

        <Link to={`/products/${product.id}`} className="button button-secondary">
          View details
        </Link>
      </article>

      <LoginPromptModal isOpen={showLoginModal} onClose={() => setShowLoginModal(false)} />

      {/* 4. Render the popup */}
      {notification && (
        <div 
          style={{
            position: "fixed",
            bottom: "30px",
            right: "30px",
            backgroundColor: "#2c3e50",
            color: "#fff",
            padding: "12px 24px",
            borderRadius: "8px",
            boxShadow: "0 4px 6px rgba(0,0,0,0.1)",
            zIndex: 9999,
            fontWeight: 500,
            animation: "fadeIn 0.3s ease-in-out"
          }}
        >
          {notification}
        </div>
      )}
    </>
  );
}