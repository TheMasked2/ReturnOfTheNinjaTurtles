import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { orderApi } from "../../api/orderApi";
import type { OrderSummary } from "../../api/orderApi";

export default function OrdersPage() {
  const { isAuthenticated, user } = useAuth();
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated || !user?.id) {
      setLoading(false);
      return;
    }

    orderApi.getOrdersByCustomer(user.id)
      .then(setOrders)
      .catch((err) => {
        const message = err?.message?.toLowerCase?.() ?? "";
        if (message.includes("not found") || message.includes("conflict")) {
          setOrders([]);
          return;
        }

        setError(err.message || "Unable to load orders.");
      })
      .finally(() => setLoading(false));
  }, [isAuthenticated, user?.id]);

  if (!isAuthenticated) {
    return (
      <div className="page">
        <section className="status-message">
          <p className="text-muted">You need to sign in to view your orders.</p>
          <Link className="button button-primary" to="/login">
            Login now
          </Link>
        </section>
      </div>
    );
  }

  if (loading) return <div>Loading orders...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div className="page">
      <section className="section">
        <div className="section-heading">
          <h2>My Orders</h2>
          <p className="text-muted">Click an order to view shipment and payment details.</p>
        </div>

        {orders.length === 0 ? (
          <div className="form-panel">
            <p>You have no past orders yet.</p>
            <Link to="/products" className="button button-secondary">
              Browse products
            </Link>
          </div>
        ) : (
          <ul className="order-list">
            {orders.map((order) => (
              <li key={order.orderId} className="order-card">
                <Link to={`/orders/${order.orderId}`} className="order-link">
                  Order #{order.orderId}
                </Link>
                <p>Status: {order.status}</p>
                <p>Total: ${order.totalAmount}</p>
                <p>Placed on: {order.orderDate}</p>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}