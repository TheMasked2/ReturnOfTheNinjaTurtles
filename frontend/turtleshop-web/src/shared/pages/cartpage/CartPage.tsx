import { useEffect, useState } from "react";
import { cartApi } from "../../api/cartApi";

export default function CartPage() {
  const [items, setItems] = useState<any[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cartApi
      .getActiveCart("")
      .then((cart) => setItems(cart.items ?? []))
      .catch((err: unknown) => {
        const message = err instanceof Error ? err.message : "Unable to load cart.";
        setError(message);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="page">
      <section className="section">
        <div className="section-heading">
          <div>
            <h2>Shopping Cart</h2>
            <p className="text-muted">Review items in your cart before checkout.</p>
          </div>
        </div>

        {loading ? (
          <div className="status-message">Loading cart...</div>
        ) : error ? (
          <div className="status-message status-error">{error}</div>
        ) : items.length === 0 ? (
          <div className="status-message">Your cart is empty.</div>
        ) : (
          <div className="cart-list">
            {items.map((item, index) => (
              <div key={item.id ?? index} className="cart-item card">
                <div>
                  <strong>{item.productName ?? item.name ?? "Item"}</strong>
                  <p>{item.quantity ? `Quantity: ${item.quantity}` : "1 item"}</p>
                </div>
                <span className="price-tag">
                  {item.totalPrice ? `$${item.totalPrice.toFixed(2)}` : ""}
                </span>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}