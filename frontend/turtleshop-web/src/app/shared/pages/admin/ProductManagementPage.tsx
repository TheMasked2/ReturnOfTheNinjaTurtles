import React, { useState } from 'react';
import ManagementList from '../../components/admin/ManagementList';
import { useGetProductsQuery, useCreateProductMutation, useUpdateProductMutation, useDeleteProductMutation, type Product } from '../../api/adminApi';
import ProductFormModal from '../../components/admin/ProductFormModal';

const ProductManagementPage: React.FC = () => {
  const { data: products, isLoading } = useGetProductsQuery();
  const [deleteProduct] = useDeleteProductMutation();
  const [createProduct] = useCreateProductMutation();
  const [updateProduct] = useUpdateProductMutation();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<Partial<Product> | null>(null);

  const handleEdit = (product: Product) => {
    setSelectedProduct(product);
    setIsModalOpen(true);
  };

  const handleDelete = (product: Product) => {
    if (window.confirm('Are you sure you want to delete this product?')) {
      deleteProduct(product.id);
    }
  };

  const handleCreate = () => {
    setSelectedProduct(null);
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
    setSelectedProduct(null);
  };

  const handleFormSubmit = async (product: Partial<Product>) => {
    if (product.id) {
      await updateProduct(product as Product);
    } else {
      await createProduct(product);
    }
    handleModalClose();
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">Product Management</h1>
        <button onClick={handleCreate} className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
          Create Product
        </button>
      </div>
      <ManagementList
        items={products || []}
        columns={['id', 'name', 'price', 'stock']}
        onEdit={handleEdit}
        onDelete={handleDelete}
      />
      <ProductFormModal
        isOpen={isModalOpen}
        onClose={handleModalClose}
        onSubmit={handleFormSubmit}
        product={selectedProduct}
      />
    </div>
  );
};

export default ProductManagementPage;
