import React, { useState, useEffect } from 'react';
import { Modal } from '../../../../shared/components/modal/Modal';
import type { Transaction } from '../../api/adminApi';

interface TransactionFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (transaction: Partial<Transaction>) => void;
  transaction: Partial<Transaction> | null;
}

const TransactionFormModal: React.FC<TransactionFormModalProps> = ({ isOpen, onClose, onSubmit, transaction }) => {
  const [formData, setFormData] = useState<Partial<Transaction>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (transaction) {
      setFormData(transaction);
    } else {
      setFormData({});
    }
  }, [transaction]);

  const validate = () => {
    const newErrors: Record<string, string> = {};
    if (!formData.orderId) newErrors.orderId = 'Order ID is required';
    if (!formData.amount || formData.amount <= 0) newErrors.amount = 'Amount must be a positive number';
    if (!formData.status) newErrors.status = 'Status is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validate()) {
      onSubmit(formData);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    const parsedValue = name === 'amount' ? parseFloat(value) : value;
    setFormData({ ...formData, [name]: parsedValue });
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={transaction ? 'Edit Transaction' : 'Create Transaction'}>
      <form onSubmit={handleSubmit}>
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="orderId">
            Order ID
          </label>
          <input
            type="text"
            name="orderId"
            id="orderId"
            value={formData.orderId || ''}
            onChange={handleChange}
            className={`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline ${errors.orderId ? 'border-red-500' : ''}`}
          />
          {errors.orderId && <p className="text-red-500 text-xs italic">{errors.orderId}</p>}
        </div>
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="amount">
            Amount
          </label>
          <input
            type="number"
            name="amount"
            id="amount"
            value={formData.amount || ''}
            onChange={handleChange}
            className={`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline ${errors.amount ? 'border-red-500' : ''}`}
          />
          {errors.amount && <p className="text-red-500 text-xs italic">{errors.amount}</p>}
        </div>
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="status">
            Status
          </label>
          <input
            type="text"
            name="status"
            id="status"
            value={formData.status || ''}
            onChange={handleChange}
            className={`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline ${errors.status ? 'border-red-500' : ''}`}
          />
          {errors.status && <p className="text-red-500 text-xs italic">{errors.status}</p>}
        </div>
        <div className="flex items-center justify-between">
          <button
            type="submit"
            className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
          >
            {transaction ? 'Update' : 'Create'}
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

export default TransactionFormModal;
