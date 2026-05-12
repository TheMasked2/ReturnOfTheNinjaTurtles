import { Link } from "react-router-dom";
import type { Product } from "../api/productApi";

interface ProductCardProps {
  product: Product;
}

export function ProductCard({ product }: ProductCardProps) {
  return (
    <article className="card product-card">
      <div>
        <h3>{product.name}</h3>
        <p>{product.description}</p>
      </div>
      <div className="product-meta">
        <span className="price-tag">${product.price.toFixed(2)}</span>
        <span className="badge">Available since {new Date(product.availableSince).getFullYear()}</span>
      </div>
      <Link to={`/products/${product.id}`} className="button button-secondary">
        View details
      </Link>
    </article>
  );
}
