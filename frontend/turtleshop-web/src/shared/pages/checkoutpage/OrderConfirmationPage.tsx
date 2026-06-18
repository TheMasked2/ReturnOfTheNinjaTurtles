import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { checkoutApi } from "../../api/checkoutApi";

export default function OrderConfirmationPage() {
  const { orderId } = useParams<{ orderId: string }>();
  const [submitting, setSubmitting] = useState(false);
  const [status, setStatus] = useState<"idle" | "cancelled">("idle");
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  if (!orderId) {
    return (
      <div className="page">
        <section className="section">
          <div className="status-message status-error">Invalid order reference.</div>
        </section>
      </div>
    );
  }

  const handleCancelOrder = async () => {
    setSubmitting(true);
    setError(null);

    try {
      await checkoutApi.cancelOrder(Number(orderId));
      setStatus("cancelled");
    } catch (err: any) {
      setError(err.message || "Unable to cancel the order.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page">
      <section className="section">
        <div className="section-heading">
          <div>
            <h2>Order placed</h2>
            <p className="text-muted">
              Your order has been successfully placed and is now awaiting completion.
            </p>
          </div>
        </div>

        {status === "cancelled" ? (
          <div className="status-message status-success">
            Your order has been cancelled. If you want, you can continue shopping.
          </div>
        ) : (
          <div className="card" style={{ padding: "1.5rem 1.25rem" }}>
            <p>
              Order <strong>#{orderId}</strong> was placed successfully.
            </p>
            <p className="text-muted">
              Press the button below only if you want to cancel this order.
            </p>

            {error && <div className="status-message status-error">{error}</div>}

            <div className="actions-row" style={{ marginTop: "1.5rem", display: "flex", gap: "0.75rem" }}>
              <button
                type="button"
                className="button button-secondary"
                disabled={submitting}
                onClick={() => navigate("/profile")}
              >
                Back to profile
              </button>
              <button
                type="button"
                className="button button-danger"
                disabled={submitting}
                onClick={handleCancelOrder}
              >
                {submitting ? "Cancelling…" : "Cancel order"}
              </button>
            </div>
          </div>
        )}
      </section>
    </div>
  );
}