import { Link } from "react-router-dom";
import type { OrderSummary } from "../../api/orderApi";

type OrderListProps = {
  orders: OrderSummary[];
};

const listStyle = {
  display: "grid",
  gap: "1rem",
  gridTemplateColumns: "repeat(4, minmax(0, 1fr))",
} as const;

const cardStyle = {
  background: "#ffffff",
  borderRadius: "1rem",
  border: "1px solid rgba(15, 23, 42, 0.08)",
  boxShadow: "0 14px 32px rgba(15, 23, 42, 0.08)",
  padding: "1rem",
  display: "flex",
  flexDirection: "column",
  justifyContent: "space-between",
  minHeight: "170px",
} as const;

const headerStyle = {
  display: "flex",
  justifyContent: "space-between",
  alignItems: "flex-start",
  gap: "1rem",
  marginBottom: "1rem",
} as const;

const metaStyle = {
  display: "grid",
  gap: "0.75rem",
  marginBottom: "1rem",
} as const;

const statusStyle = {
  padding: "0.4rem 0.85rem",
  borderRadius: "999px",
  fontSize: "0.78rem",
  fontWeight: 700,
  textTransform: "uppercase",
  letterSpacing: "0.04em",
  background: "rgba(0, 96, 255, 0.08)",
  color: "#0f172a",
} as const;

function formatOrderDate(orderDate: string) {
  const date = new Date(orderDate);

  if (Number.isNaN(date.getTime())) {
    return orderDate;
  }

  try {
    return new Intl.DateTimeFormat(undefined, {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "numeric",
      minute: "2-digit",
      timeZoneName: "short",
    }).format(date);
  } catch {
    return `${date.toLocaleDateString()} ${date.toLocaleTimeString()}`;
  }
}

export function OrderList({ orders }: OrderListProps) {
  if (orders.length === 0) {
    return (
      <div className="form-panel">
        <p>You have no past orders yet.</p>
        <Link to="/products" className="button button-secondary">
          Browse products
        </Link>
      </div>
    );
  }

  return (
    <div style={listStyle}>
      {orders.map((order) => (
        <article key={order.orderId} style={cardStyle}>
          <div style={headerStyle}>
            <div>
              <p className="order-card-label">Order</p>
              <h3>#{order.orderId}</h3>
            </div>
            <span style={statusStyle}>
              {order.status}
            </span>
          </div>

          <div style={metaStyle}>
            <div>
              <p className="order-card-label">Placed</p>
              <p>{formatOrderDate(order.orderDate)}</p>
            </div>
            <div>
              <p className="order-card-label">Total</p>
              <p>${order.totalAmount.toFixed(2)}</p>
            </div>
          </div>

          <div>
            <Link to={`/orders/${order.orderId}`} className="button button-primary">
              View details
            </Link>
          </div>
        </article>
      ))}
    </div>
  );
}