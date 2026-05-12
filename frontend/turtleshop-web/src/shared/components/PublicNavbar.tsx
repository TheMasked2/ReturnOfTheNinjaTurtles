import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

const navClass = ({ isActive }: { isActive: boolean }) =>
    isActive ? "nav-link active" : "nav-link";

export function PublicNavbar() {
    const { isAuthenticated, user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate("/");
    };

    return (
        <header className="site-header">
            <div className="site-branding">
                <NavLink to="/" className="brand-link">
                    TurtleShop
                </NavLink>
                <span className="brand-subtitle">Gray, beige, and clean.</span>
            </div>

            <nav className="nav-links">
                <NavLink to="/" end className={navClass}>
                    Home
                </NavLink>
                <NavLink to="/products" className={navClass}>
                    Products
                </NavLink>
                <NavLink to="/wishlist" className={navClass}>
                    Wishlist
                </NavLink>
            </nav>

            <div className="auth-actions">
                {isAuthenticated ? (
                    <>
                        <span className="user-badge">{user?.username}</span>
                        <button className="button button-ghost" onClick={handleLogout}>
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
