import React, { useState, useEffect } from 'react';
import { Modal } from '../../../../shared/components/modal/Modal';
import type { InventoryItem, Product } from '../../api/adminApi';
import { useGetProductsQuery } from '../../api/adminApi';

interface InventoryFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (item: Partial<InventoryItem>) => void;
  item: Partial<InventoryItem> | null;
}

const InventoryFormModal: React.FC<InventoryFormModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
  item,
}) => {
  const [formData, setFormData] = useState<Partial<InventoryItem>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});
  const { data: products = [] } = useGetProductsQuery();

  useEffect(() => {
    if (item) {
      setFormData(item);
    } else {
      setFormData({});
    }
  }, [item]);

  const validate = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.product) {
      newErrors.product = 'Product is required';
    }

    if (formData.stock === undefined || formData.stock < 0) {
      newErrors.stock = 'Stock must be a non-negative number';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (validate()) {
      onSubmit(formData);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;

    if (name === 'product') {
      const selectedProduct = products.find(
        (p: Product) => p.id === value
      );

      setFormData({
        ...formData,
        product: selectedProduct,
      });
    } else {
      const parsedValue =
        name === 'stock' ? parseInt(value, 10) : value;

      setFormData({
        ...formData,
        [name]: parsedValue,
      });
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={item ? 'Edit Inventory' : 'Add Inventory'}
    >
      <form onSubmit={handleSubmit}>
        <div className="mb-4">
          <label
            className="block text-gray-700 text-sm font-bold mb-2"
            htmlFor="product"
          >
            Product
          </label>

          <select
            name="product"
            id="product"
            value={formData.product?.id || ''}
            onChange={handleChange}
            className={`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline ${
              errors.product ? 'border-red-500' : ''
            }`}
          >
            <option value="">Select a product</option>

            {products.map((p: Product) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>

          {errors.product && (
            <p className="text-red-500 text-xs italic">
              {errors.product}
            </p>
          )}
        </div>

        <div className="mb-4">
          <label
            className="block text-gray-700 text-sm font-bold mb-2"
            htmlFor="stock"
          >
            Stock
          </label>

          <input
            type="number"
            name="stock"
            id="stock"
            value={formData.stock || ''}
            onChange={handleChange}
            className={`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline ${
              errors.stock ? 'border-red-500' : ''
            }`}
          />

          {errors.stock && (
            <p className="text-red-500 text-xs italic">
              {errors.stock}
            </p>
          )}
        </div>

        <div className="mb-4">
          <label
            className="block text-gray-700 text-sm font-bold mb-2"
            htmlFor="location"
          >
            Location
          </label>

          <input
            type="text"
            name="location"
            id="location"
            value={formData.location || ''}
            onChange={handleChange}
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
          />
        </div>

        <div className="flex items-center justify-between">
          <button
            type="submit"
            className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
          >
            {item ? 'Update' : 'Add'}
          </button>

          <button
            type="button"
            onClick={onClose}
            className="inline-block align-baseline font-bold text-sm text-blue-500 hover:text-blue-800"
          >
            Cancel
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default InventoryFormModal;