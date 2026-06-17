import React, { useState, useEffect } from 'react';
import { Modal } from '../../../../shared/components/modal/Modal';
import type { Order } from '../../api/adminApi';
import { useGetUsersQuery } from '../../api/adminApi';
import type { User } from '../../../../shared/auth/AuthContext';

interface OrderFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (order: Partial<Order>) => void;
  order: Partial<Order> | null;
}

const OrderFormModal: React.FC<OrderFormModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
  order,
}) => {
  const [formData, setFormData] = useState<Partial<Order>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});
  const { data: users = [] } = useGetUsersQuery();

  useEffect(() => {
    if (order) {
      setFormData(order);
    } else {
      setFormData({});
    }
  }, [order]);

  const validate = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.customer) {
      newErrors.customer = 'Customer is required';
    }

    if (!formData.total || formData.total <= 0) {
      newErrors.total = 'Total must be a positive number';
    }

    if (!formData.status) {
      newErrors.status = 'Status is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (
    e: React.SyntheticEvent<HTMLFormElement>
  ) => {
    e.preventDefault();

    if (validate()) {
      onSubmit(formData);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;

    if (name === 'customer') {
      const selectedUser = users.find((u: User) => u.id === value);

      setFormData({
        ...formData,
        customer: selectedUser,
      });
    } else {
      const parsedValue = name === 'total' ? parseFloat(value) : value;

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
      title={order ? 'Edit Order' : 'Create Order'}
    >
      <form onSubmit={handleSubmit}>
        <div className="mb-4">
          <label
            className="block text-gray-700 text-sm font-bold mb-2"
            htmlFor="customer"
          >
            Customer
          </label>

          <select
            name="customer"
            id="customer"
            value={formData.customer?.id || ''}
            onChange={handleChange}
            className={`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline ${
              errors.customer ? 'border-red-500' : ''
            }`}
          >
            <option value="">Select a customer</option>

            {users.map((u: User) => (
              <option key={u.id} value={u.id}>
                {u.firstName} {u.lastName}
              </option>
            ))}
          </select>

          {errors.customer && (
            <p className="text-red-500 text-xs italic">
              {errors.customer}
            </p>
          )}
        </div>

        <div className="mb-4">
          <label
            className="block text-gray-700 text-sm font-bold mb-2"
            htmlFor="total"
          >
            Total
          </label>

          <input
            type="number"
            name="total"
            id="total"
            value={formData.total || ''}
            onChange={handleChange}
            className={`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline ${
              errors.total ? 'border-red-500' : ''
            }`}
          />

          {errors.total && (
            <p className="text-red-500 text-xs italic">
              {errors.total}
            </p>
          )}
        </div>

        <div className="mb-4">
          <label
            className="block text-gray-700 text-sm font-bold mb-2"
            htmlFor="status"
          >
            Status
          </label>

          <input
            type="text"
            name="status"
            id="status"
            value={formData.status || ''}
            onChange={handleChange}
            className={`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline ${
              errors.status ? 'border-red-500' : ''
            }`}
          />

          {errors.status && (
            <p className="text-red-500 text-xs italic">
              {errors.status}
            </p>
          )}
        </div>

        <div className="flex items-center justify-between">
          <button
            type="submit"
            className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
          >
            {order ? 'Update' : 'Create'}
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

export default OrderFormModal;