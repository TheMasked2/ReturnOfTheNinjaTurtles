import React, { useState } from 'react';
import ManagementList from '../../components/admin/ManagementList';
import { useGetProductsQuery, useCreateProductMutation, useUpdateProductMutation, useDeleteProductMutation, type Product } from '../../api/adminApi';
import ProductFormModal from '../../components/admin/ProductFormModal';

const ProductManagementPage: React.FC = () => {
  const { data: products, isLoading, refetch } = useGetProductsQuery();
  const [deleteProduct] = useDeleteProductMutation();
  const [createProduct] = useCreateProductMutation();
  const [updateProduct] = useUpdateProductMutation();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<Partial<Product> | null>(null);

  const handleEdit = (product: Product) => {
    setSelectedProduct(product);
    setIsModalOpen(true);
  };

  const handleDelete = async (product: Product) => {
    if (window.confirm('Are you sure you want to delete this product?')) {
      await deleteProduct(product.id);
      await refetch();
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
    await refetch();
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="container mx-auto px-4 py-8 admin-management-page">
      <div className="management-card">
        <div className="management-header">
          <div>
            <h1>Product Management</h1>
            <p className="management-description">Create, update, and remove products while keeping the product catalog clean and easy to browse.</p>
          </div>
          <button onClick={handleCreate} className="button button-secondary">
            Create Product
          </button>
        </div>
        <ManagementList
          items={products || []}
          columns={['id', 'name', 'price', 'specs']}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      </div>
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
