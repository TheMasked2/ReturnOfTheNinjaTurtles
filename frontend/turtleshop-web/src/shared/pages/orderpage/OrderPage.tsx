import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { orderApi } from "../../api/orderApi";
import type { OrderSummary } from "../../api/orderApi";
import { OrderList } from "../../components/order/OrderList";

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

        <OrderList orders={orders} />
      </section>
    </div>
  );
}