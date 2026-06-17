import type { CartSummaryItem } from "./checkoutTypes";

type OrderSummaryProps = {
  items: CartSummaryItem[];
  subtotal: number;
};

export default function OrderSummary({ items, subtotal }: OrderSummaryProps) {
  return (
    <div className="order-summary">
      <h3>Order summary</h3>

      {items.length === 0 ? (
        <div className="status-message">Your cart is empty.</div>
      ) : (
        <>
          <div style={{ display: "flex", flexDirection: "column", gap: "0.85rem" }}>
            {items.map((item) => (
              <div
                key={item.cartItemId}
                className="cart-summary-item"
                style={{ display: "flex", justifyContent: "space-between", gap: "0.75rem" }}
              >
                <div>
                  <strong>{item.name}</strong>
                  <div className="text-muted">
                    {item.quantity} × ${item.unitPrice.toFixed(2)}
                  </div>
                </div>
                <div>
                  <strong>${item.subtotal.toFixed(2)}</strong>
                </div>
              </div>
            ))}
          </div>

          <div
            className="summary-line summary-total"
            style={{ marginTop: "1.25rem", paddingTop: "0.85rem", borderTop: "1px solid var(--border)" }}
          >
            <span>Subtotal</span>
            <strong>${subtotal.toFixed(2)}</strong>
          </div>
        </>
      )}
    </div>
  );
}
