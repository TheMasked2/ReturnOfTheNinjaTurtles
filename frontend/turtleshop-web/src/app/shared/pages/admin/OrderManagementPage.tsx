import React, { useState } from 'react';
import ManagementList from '../../components/admin/ManagementList';
import { useGetOrdersQuery, useCreateOrderMutation, useUpdateOrderMutation, useDeleteOrderMutation, type Order } from '../../api/adminApi';
import OrderFormModal from '../../components/admin/OrderFormModal';

const OrderManagementPage: React.FC = () => {
  const { data: orders, isLoading } = useGetOrdersQuery();
  const [deleteOrder] = useDeleteOrderMutation();
  const [createOrder] = useCreateOrderMutation();
  const [updateOrder] = useUpdateOrderMutation();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<Partial<Order> | null>(null);

  const handleEdit = (order: Order) => {
    setSelectedOrder(order);
    setIsModalOpen(true);
  };

  const handleDelete = (order: Order) => {
    if (window.confirm('Are you sure you want to delete this order?')) {
      deleteOrder(order.id);
    }
  };

  const handleCreate = () => {
    setSelectedOrder(null);
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
    setSelectedOrder(null);
  };

  const handleFormSubmit = async (order: Partial<Order>) => {
    if (order.id) {
      await updateOrder(order as Order);
    } else {
      await createOrder(order);
    }
    handleModalClose();
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }
  
  const formattedOrders = orders?.map(order => ({
      ...order,
      customerName: `${order.customer.firstName} ${order.customer.lastName}`
  }))

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">Order Management</h1>
        <button onClick={handleCreate} className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
          Create Order
        </button>
      </div>
      <ManagementList
        items={formattedOrders || []}
        columns={['id', 'customerName', 'date', 'total', 'status']}
        onEdit={handleEdit}
        onDelete={handleDelete}
      />
      <OrderFormModal
        isOpen={isModalOpen}
        onClose={handleModalClose}
        onSubmit={handleFormSubmit}
        order={selectedOrder}
      />
    </div>
  );
};

export default OrderManagementPage;
