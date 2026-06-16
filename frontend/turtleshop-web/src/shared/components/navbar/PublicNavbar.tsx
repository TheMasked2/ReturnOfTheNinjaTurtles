import { useEffect, useState, useRef, type MouseEvent as ReactMouseEvent } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { cartApi } from "../../api/cartApi";
import { wishlistApi } from "../../api/wishlistApi";
import { productApi } from "../../api/productApi";
import { CartPanel } from "../cart/CartPanel";
import { WishlistPanel } from "../wishlist/WishlistPanel";
import { publishHeaderRefresh, subscribeHeaderRefresh } from "../../state/refreshBus";

const navClass = ({ isActive }: { isActive: boolean }) =>
    isActive ? "nav-link active" : "nav-link";

export function PublicNavbar() {
    const { isAuthenticated, logout, user } = useAuth();
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
            if (!isAuthenticated || !user?.id) {
                setWishlistCount(0);
                setCartCount(0);
                setCartItems([]);
                setWishlistItems([]);
                return;
            }

        const loadCounts = async () => {
            try {
                const products = await productApi.getProducts().catch(() => []);
                let activeCartItems: any[] = [];
                try {
                    const cart = await cartApi.getActiveCart(user.id);
                    activeCartItems = cart.items || [];
                } catch (error: any) {
                    // Ignore missing cart
                }
    
                let wishlistItemsResponse: any[] = [];
                try {
                    const wishlist = await wishlistApi.getWishlistByCustomer(user.id);
                    wishlistItemsResponse = await wishlistApi.getWishlistItems(wishlist.wishlistId);
                } catch (error: any) {
                    // Ignore missing wishlist
                }
    
                const mappedCartItems = activeCartItems
                    .map((item: any) => {
                        const product = products.find((p: any) => p.id === item.productId);
                        return {
                            ...item,
                            name: product?.name || `Product #${item.productId}`,
                            totalPrice: product ? product.price * (item.quantity || 1) : null,
                        };
                    })
                    .sort(
                        (a: any, b: any) =>
                            (a.cartItemId ?? 0) - (b.cartItemId ?? 0)
                    );
    
                const mappedWishlistItems = wishlistItemsResponse.map((item: any) => {
                    const product = products.find((p: any) => p.id === item.productId);
                    return {
                        ...item,
                        name: product?.name || `Product #${item.productId}`,
                        totalPrice: product?.price ?? null,
                    };
                });
    
                setCartCount(mappedCartItems.length);
                setCartItems(mappedCartItems);
                setWishlistCount(mappedWishlistItems.length);
                setWishlistItems(mappedWishlistItems);

            } catch (error) {
                console.error("Failed to load header items", error);
                setWishlistCount(0);
                setCartCount(0);
                setCartItems([]);
                setWishlistItems([]);
            }
        };
    
        const unsubscribe = subscribeHeaderRefresh(loadCounts);
        loadCounts();
    
        return () => unsubscribe();
    }, [isAuthenticated, user?.id]);

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

    const handleWishlistItemRemove = async (wishlistId: number, productId: number) => {
        try {
            await wishlistApi.deleteWishlistItem(wishlistId, productId);
            publishHeaderRefresh();
        } catch (error) {
            console.error("Failed to remove wishlist item", error);
        }
    };

    return (
        // ... (keep the existing JSX return block exactly the same) 
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
                    onRemoveItem={handleWishlistItemRemove}
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