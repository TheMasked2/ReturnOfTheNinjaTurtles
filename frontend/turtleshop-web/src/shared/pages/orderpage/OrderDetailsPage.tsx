import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { orderApi } from "../../api/orderApi";
import type { OrderDetails, Shipment, Transaction } from "../../api/orderApi";

export default function OrderDetailsPage() {
  const { orderId } = useParams<{ orderId: string }>();
  const [order, setOrder] = useState<OrderDetails | null>(null);
  const [shipment, setShipment] = useState<Shipment | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!orderId) return;
    const id = Number(orderId);

    Promise.all([
      orderApi.getOrder(id),
      orderApi.getShipmentByOrder(id),
      orderApi.getTransactionsByOrder(id),
    ])
      .then(([orderResponse, shipmentResponse, transactionResponse]) => {
        setOrder(orderResponse);
        setShipment(shipmentResponse);
        setTransactions(transactionResponse);
      })
      .catch((err) => setError(err.message || "Unable to load order details."))
      .finally(() => setLoading(false));
  }, [orderId]);

  if (loading) return <div>Loading order details...</div>;
  if (error) return <div>Error: {error}</div>;
  if (!order) return <div>Order not found.</div>;

  return (
    <div className="page">
      <section className="section">
        <div className="section-heading">
          <h2>Order #{order.orderId}</h2>
          <Link className="button button-secondary" to="/orders">
            Back to orders
          </Link>
        </div>

        <div className="order-details">
          <h3>Order summary</h3>
          <p>Status: {order.status}</p>
          <p>Total: ${order.totalAmount}</p>
          <p>Placed on: {order.orderDate}</p>

          {order.items?.length ? (
            <div>
              <h4>Items</h4>
              <ul>
                {order.items.map((item) => (
                  <li key={item.productId}>
                    {item.productName} × {item.quantity} — ${item.price}
                  </li>
                ))}
              </ul>
            </div>
          ) : null}

          <h3>Shipment</h3>
          {shipment ? (
            <div>
              <p>Shipment ID: {shipment.shipmentId}</p>
              <p>Method: {shipment.method}</p>
              <p>Status: {shipment.status}</p>
              <p>Address: {shipment.address}</p>
            </div>
          ) : (
            <p>No shipment details available.</p>
          )}

          <h3>Payment</h3>
          {transactions.length ? (
            <ul>
              {transactions.map((transaction) => (
                <li key={transaction.transactionId}>
                  Transaction #{transaction.transactionId}: ${transaction.amount} — {transaction.status}
                </li>
              ))}
            </ul>
          ) : (
            <p>No payment records found.</p>
          )}
        </div>
      </section>
    </div>
  );
}