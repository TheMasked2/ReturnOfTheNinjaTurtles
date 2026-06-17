import React, { useState } from 'react';
import ManagementList from '../../components/admin/ManagementList';
import { useGetInventoryQuery, useAddInventoryMutation, useUpdateInventoryMutation, useDeleteInventoryMutation, type InventoryItem } from '../../api/adminApi';
import InventoryFormModal from '../../components/admin/InventoryFormModal';

const InventoryManagementPage: React.FC = () => {
  const { data: inventory, isLoading } = useGetInventoryQuery();
  const [deleteInventory] = useDeleteInventoryMutation();
  const [addInventory] = useAddInventoryMutation();
  const [updateInventory] = useUpdateInventoryMutation();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<Partial<InventoryItem> | null>(null);

  const handleEdit = (item: InventoryItem) => {
    setSelectedItem(item);
    setIsModalOpen(true);
  };

  const handleDelete = (item: InventoryItem) => {
    if (window.confirm('Are you sure you want to delete this inventory item?')) {
      deleteInventory(item.id);
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
    if (item.id) {
      await updateInventory(item as InventoryItem);
    } else {
      await addInventory(item);
    }
    handleModalClose();
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  const formattedInventory = inventory?.map(item => ({
      ...item,
      productName: item.product.name
  }))

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">Inventory Management</h1>
        <button onClick={handleCreate} className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
          Add Inventory
        </button>
      </div>
      <ManagementList
        items={formattedInventory || []}
        columns={['id', 'productName', 'stock', 'location']}
        onEdit={handleEdit}
        onDelete={handleDelete}
      />
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
