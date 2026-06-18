import React from 'react';
import ManagementList from '../../components/admin/ManagementList';
import { useGetOrdersQuery, useCancelOrderMutation, type OrderSummary } from '../../api/adminApi';

type OrderRow = OrderSummary & { id: number };

const OrderManagementPage: React.FC = () => {
  const { data: orders, isLoading, refetch } = useGetOrdersQuery();
  const [cancelOrder] = useCancelOrderMutation();

  const handleCancel = async (order: OrderRow) => {
    if (window.confirm('Are you sure you want to cancel this order?')) {
      await cancelOrder(order.orderId);
      await refetch();
    }
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  const orderRows = (orders || []).map((order) => ({
    ...order,
    id: order.orderId,
  }));

  return (
    <div className="container mx-auto px-4 py-8 admin-management-page">
      <div className="management-card">
        <div className="management-header">
          <div>
            <h1>Order Management</h1>
            <p className="management-description">Review recent orders and cancel them directly from the admin center.</p>
          </div>
          <button onClick={() => refetch()} className="button button-secondary">
            Refresh Orders
          </button>
        </div>
        <ManagementList
          items={orderRows}
          columns={['orderId', 'customerEmail', 'orderDate', 'status', 'totalAmount']}
          onEdit={handleCancel}
          onDelete={() => void 0}
        />
      </div>
    </div>
  );
};

export default OrderManagementPage;
