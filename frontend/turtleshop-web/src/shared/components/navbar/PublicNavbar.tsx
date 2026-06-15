import { useEffect, useState, useRef, type MouseEvent as ReactMouseEvent } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { cartApi } from "../../api/cartApi";
import { wishlistApi } from "../../api/wishlistApi";
import { CartPanel } from "../cart/CartPanel";
import { WishlistPanel } from "../wishlist/WishlistPanel";

const navClass = ({ isActive }: { isActive: boolean }) =>
    isActive ? "nav-link active" : "nav-link";

export function PublicNavbar() {
    const { isAuthenticated, logout } = useAuth();
    const navigate = useNavigate();
    const [wishlistCount, setWishlistCount] = useState(0);
    const [cartCount, setCartCount] = useState(0);
    const [cartItems, setCartItems] = useState<any[]>([]);
    const [wishlistItems, setWishlistItems] = useState<any[]>([]);
    const [isCartOpen, setIsCartOpen] = useState(false);
    const [isWishlistOpen, setIsWishlistOpen] = useState(false);
    const cartToggleRef = useRef<HTMLAnchorElement | null>(null);
    const wishlistToggleRef = useRef<HTMLAnchorElement | null>(null);

    useEffect(() => {
        if (!isAuthenticated) {
            setWishlistCount(0);
            setCartCount(0);
            setCartItems([]);
            setWishlistItems([]);
            return;
        }

        const loadCounts = async () => {
            try {
                const [wishlistItems, cartItems] = await Promise.all([
                    wishlistApi.getItems(),
                    cartApi.getItems(),
                ]);
                setWishlistCount(wishlistItems.length);
                setCartCount(cartItems.length);
                setCartItems(cartItems);
                setWishlistItems(wishlistItems);
            } catch {
                setWishlistCount(0);
                setCartCount(0);
                setCartItems([]);
                setWishlistItems([]);
            }
        };

        loadCounts();
    }, [isAuthenticated]);

    const handleLogout = () => {
        logout();
        navigate("/");
    };

    const handleCartToggle = (event: ReactMouseEvent<HTMLAnchorElement>) => {
        event.preventDefault();
        setIsWishlistOpen(false);
        setIsCartOpen((prev) => !prev);
    };

    const handleWishlistToggle = (event: ReactMouseEvent<HTMLAnchorElement>) => {
        event.preventDefault();
        setIsCartOpen(false);
        setIsWishlistOpen((prev) => !prev);
    };

    return (
        <header className="site-header">
            <div className="site-branding">
                <NavLink to="/" className="brand-link">
                    TurtleShop
                </NavLink>
                <span className="brand-subtitle">The ninja fast way to shop!</span>
            </div>

            <div className="nav-links-wrapper">
                <nav className="nav-links">
                    <NavLink to="/" end className={navClass}>
                        Home
                    </NavLink>
                    <NavLink to="/products" className={navClass}>
                        Products
                    </NavLink>
                    <NavLink
                        to="/wishlist"
                        className={navClass}
                        onClick={handleWishlistToggle}
                        ref={wishlistToggleRef}
                    >
                        Wishlist
                        {wishlistCount > 0 && (
                            <span className="badge badge-nav">{wishlistCount}</span>
                        )}
                    </NavLink>
                    <NavLink
                        to="/cart"
                        className={navClass}
                        onClick={handleCartToggle}
                        ref={cartToggleRef}
                    >
                        Cart
                        {cartCount > 0 && (
                            <span className="badge badge-nav">{cartCount}</span>
                        )}
                    </NavLink>
                </nav>

                <CartPanel
                    isOpen={isCartOpen}
                    items={cartItems}
                    onClose={() => setIsCartOpen(false)}
                    toggleRef={cartToggleRef}
                />

                <WishlistPanel
                    isOpen={isWishlistOpen}
                    items={wishlistItems}
                    onClose={() => setIsWishlistOpen(false)}
                    toggleRef={wishlistToggleRef}
                />
            </div>

            <div className="auth-actions">
                {isAuthenticated ? (
                    <>
                        <NavLink to="/profile" className="button button-ghost">
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
                        <NavLink to="/login" className="button button-ghost">
                            Login
                        </NavLink>
                        <NavLink to="/register" className="button button-secondary">
                            Register
                        </NavLink>
                    </>
                )}
            </div>
        </header>
    );
}