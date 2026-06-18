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
          <div>
            <h2>Order #{order.orderId}</h2>
            <p className="text-muted">Order placed on {order.orderDate}</p>
          </div>
          <Link className="button button-secondary" to="/orders">
            Back to orders
          </Link>
        </div>

        <div className="order-details-grid">
          <article className="card">
            <h3>Order summary</h3>
            <div className="order-summary-grid">
              <div>
                <p className="order-card-label">Status</p>
                <p><strong>{order.status}</strong></p>
              </div>
              <div>
                <p className="order-card-label">Total</p>
                <p><strong>${order.totalAmount.toFixed(2)}</strong></p>
              </div>
              <div>
                <p className="order-card-label">Items</p>
                <p>{order.items?.length ?? 0}</p>
              </div>
            </div>

            <div className="order-items">
              {order.items?.length ? (
                <>
                  <h4>Items</h4>
                  <ul>
                    {order.items.map((item) => (
                      <li key={item.productId}>
                        {item.productName} × {item.quantity} — ${item.price.toFixed(2)}
                      </li>
                    ))}
                  </ul>
                </>
              ) : (
                <p>No items found.</p>
              )}
            </div>
          </article>

          <article className="card">
            <h3>Shipment</h3>
            {shipment ? (
              <div className="shipment-details">
                <div>
                  <p className="order-card-label">Method</p>
                  <p>{shipment.method}</p>
                </div>
                <div>
                  <p className="order-card-label">Status</p>
                  <p>{shipment.status}</p>
                </div>
                <div>
                  <p className="order-card-label">Address</p>
                  <p>{shipment.address}</p>
                </div>
              </div>
            ) : (
              <p>No shipment details available.</p>
            )}
          </article>

          <article className="card">
            <h3>Payment</h3>
            {transactions.length ? (
              <table className="transaction-table">
                <thead>
                  <tr>
                    <th>Transaction</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.map((transaction) => (
                    <tr key={transaction.transactionId}>
                      <td>#{transaction.transactionId}</td>
                      <td>${transaction.amount.toFixed(2)}</td>
                      <td>{transaction.status}</td>
                      <td>{transaction.transactionDate}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p>No payment records found.</p>
            )}
          </article>
        </div>
      </section>
    </div>
  );
}