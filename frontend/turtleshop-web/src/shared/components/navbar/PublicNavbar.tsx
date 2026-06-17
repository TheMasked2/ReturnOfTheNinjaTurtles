import { useEffect, useState, useRef, type MouseEvent as ReactMouseEvent } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { cartApi } from "../../api/cartApi";
import { wishlistApi } from "../../api/wishlistApi";
import { productApi } from "../../api/productApi";
import { CartPanel } from "../cart/CartPanel";
import { subscribeHeaderRefresh } from "../../state/refreshBus";

const navClass = ({ isActive }: { isActive: boolean }) =>
  isActive ? "nav-link active" : "nav-link";

export function PublicNavbar() {
  const { isAuthenticated, logout, user } = useAuth();
  const navigate = useNavigate();

  const [wishlistCount, setWishlistCount] = useState(0);
  const [cartCount, setCartCount] = useState(0);
  const [cartItems, setCartItems] = useState<any[]>([]);
  const [isCartOpen, setIsCartOpen] = useState(false);

  const cartToggleRef = useRef<HTMLAnchorElement | null>(null);

useEffect(() => {
  if (!isAuthenticated || !user?.id) {
    setWishlistCount(0);
    setCartCount(0);
    setCartItems([]);
    return;
  }

  const loadCounts = async () => {
    try {
      let activeCartItems: any[] = [];

      try {
        const cart = await cartApi.getActiveCart(user.id);
        activeCartItems = cart.items || [];
      } catch {
        // Ignore missing cart
      }

      const cartProductIds = Array.from(
        new Set(
          activeCartItems
            .map((item: any) => item.productId)
            .filter(Boolean)
        )
      );

      const products = cartProductIds.length
        ? await productApi.getProductsByIds(cartProductIds).catch(() => [])
        : [];

      let wishlistItemsResponse: any[] = [];

      try {
        const wishlist = await wishlistApi.getWishlistByCustomer(user.id);
        wishlistItemsResponse = await wishlistApi.getWishlistItems(
          wishlist.wishlistId
        );
      } catch {
        // Ignore missing wishlist
      }

      const mappedCartItems = activeCartItems
        .map((item: any) => {
          const product = products.find(
            (p: any) => p.id === item.productId
          );

          return {
            ...item,
            name: product?.name || `Product #${item.productId}`,
            totalPrice: product
              ? product.price * (item.quantity || 1)
              : null,
          };
        })
        .sort(
          (a: any, b: any) =>
            (a.cartItemId ?? 0) - (b.cartItemId ?? 0)
        );

      setCartCount(mappedCartItems.length);
      setCartItems(mappedCartItems);
      setWishlistCount(wishlistItemsResponse.length);
    } catch (error) {
      console.error("Failed to load header items", error);
      setWishlistCount(0);
      setCartCount(0);
      setCartItems([]);
    }
  };

  const unsubscribe = subscribeHeaderRefresh(loadCounts);

  loadCounts();

  return () => {
    unsubscribe();
  };
}, [isAuthenticated, user?.id]);

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  const handleCartToggle = (
    event: ReactMouseEvent<HTMLAnchorElement>
  ) => {
    event.preventDefault();
    setIsCartOpen((prev) => !prev);
  };

  return (
    <header className="site-header">
      <div className="site-branding">
        <NavLink to="/" className="brand-link">
          TurtleShop
        </NavLink>
        <span className="brand-subtitle">
          The ninja fast way to shop!
        </span>
      </div>

      <div className="nav-links-wrapper">
        <nav className="nav-links">
          <NavLink to="/" end className={navClass}>
            Home
          </NavLink>

          <NavLink to="/products" className={navClass}>
            Products
          </NavLink>

          {isAuthenticated && user?.roles?.includes("ROLE_ADMIN") && (
            <NavLink to="/admin" className={navClass}>
              Admin
            </NavLink>
          )}

          <NavLink to="/wishlist" className={navClass}>
            Wishlist
            {wishlistCount > 0 && (
              <span className="badge badge-nav">
                {wishlistCount}
              </span>
            )}
          </NavLink>
        </nav>

        <CartPanel
          isOpen={isCartOpen}
          items={cartItems}
          onClose={() => setIsCartOpen(false)}
          toggleRef={cartToggleRef}
        />
      </div>

      <div className="auth-actions">
        <a
          href="#"
          className="button button-icon cart-toggle"
          onClick={handleCartToggle}
          ref={cartToggleRef}
          aria-label="Toggle cart"
        >
          <span
            className="icon icon-cart"
            aria-hidden="true"
          >
            🛒
          </span>

          {cartCount > 0 && (
            <span className="badge badge-nav">
              {cartCount}
            </span>
          )}
        </a>

        {isAuthenticated ? (
          <>
            <NavLink
              to="/profile"
              className="button button-ghost"
            >
              Profile
            </NavLink>

            <button
              className="button button-secondary"
              onClick={handleLogout}
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <NavLink
              to="/login"
              className="button button-ghost"
            >
              Login
            </NavLink>

            <NavLink
              to="/register"
              className="button button-secondary"
            >
              Register
            </NavLink>
          </>
        )}
      </div>
    </header>
  );
}