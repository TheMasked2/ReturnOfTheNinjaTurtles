import React, { useState } from 'react';
import ManagementList from '../../components/admin/ManagementList';
import { useGetInventoryQuery, useAddInventoryMutation, useUpdateInventoryMutation, useDeleteInventoryMutation, type InventoryItem } from '../../api/adminApi';
import InventoryFormModal from '../../components/admin/InventoryFormModal';

type DisplayInventoryItem = InventoryItem & { id: number };

const InventoryManagementPage: React.FC = () => {
  const { data: inventory, isLoading, refetch } = useGetInventoryQuery();
  const [deleteInventory] = useDeleteInventoryMutation();
  const [addInventory] = useAddInventoryMutation();
  const [updateInventory] = useUpdateInventoryMutation();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<Partial<InventoryItem> | null>(null);

  const handleEdit = (item: DisplayInventoryItem) => {
    setSelectedItem(item);
    setIsModalOpen(true);
  };

  const handleDelete = async (item: DisplayInventoryItem) => {
    if (window.confirm('Are you sure you want to delete this inventory item?')) {
      await deleteInventory(item.productId);
      await refetch();
    }
  };

  const handleCreate = () => {
    setSelectedItem(null);
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
    setSelectedItem(null);
  };

  const handleFormSubmit = async (item: Partial<InventoryItem>) => {
    if (item.productId) {
      await updateInventory({
        productId: item.productId,
        quantityAvailable: item.quantityAvailable ?? 0,
        quantityReserved: item.quantityReserved ?? 0,
      });
    } else {
      await addInventory({
        productId: item.productId ?? 0,
        quantityAvailable: item.quantityAvailable ?? 0,
        quantityReserved: item.quantityReserved ?? 0,
      });
    }
    handleModalClose();
    await refetch();
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  const formattedInventory = inventory?.map(item => ({
      ...item,
      id: item.inventoryId,
  })) as DisplayInventoryItem[];

  return (
    <div className="container mx-auto px-4 py-8 admin-management-page">
      <div className="management-card">
        <div className="management-header">
          <div>
            <h1>Inventory Management</h1>
            <p className="management-description">Track stock levels, update product inventory, and keep quantity data aligned with the backend.</p>
          </div>
          <button onClick={handleCreate} className="button button-secondary">
            Add Inventory
          </button>
        </div>
        <ManagementList
          items={formattedInventory || []}
          columns={['id', 'productId', 'quantityAvailable', 'quantityReserved']}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      </div>
      <InventoryFormModal
        isOpen={isModalOpen}
        onClose={handleModalClose}
        onSubmit={handleFormSubmit}
        item={selectedItem}
      />
    </div>
  );
};

export default InventoryManagementPage;
