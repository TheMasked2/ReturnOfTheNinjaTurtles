import { Link } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function WishlistPage() {
  const { isAuthenticated, user } = useAuth();

  return (
    <div className="page">
      <section className="section">
        <div className="section-heading">
          <div>
            <h2>Wishlist</h2>
            <p className="text-muted">Keep track of items you want to save for later.</p>
          </div>
        </div>

        {isAuthenticated ? (
          <div className="form-panel">
            <p>Hi {user?.username}, your wishlist is ready to grow.</p>
            <p className="text-muted">
              Add favorite products from the products page, then come back here to view them later.
            </p>
            <Link to="/products" className="button button-secondary">
              Browse products
            </Link>
          </div>
        ) : (
          <div className="status-message">
            <p className="text-muted">You need to be signed in to view your wishlist.</p>
            <Link className="button button-primary" to="/login">
              Login now
            </Link>
          </div>
        )}
      </section>
    </div>
  );
}
