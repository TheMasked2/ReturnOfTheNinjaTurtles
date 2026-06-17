import React, { useState } from 'react';
import ManagementList from '../../components/admin/ManagementList';
import { useGetTransactionsQuery, useCreateTransactionMutation, useUpdateTransactionMutation, useDeleteTransactionMutation, type Transaction } from '../../api/adminApi';
import TransactionFormModal from '../../components/admin/TransactionFormModal';

const TransactionManagementPage: React.FC = () => {
  const { data: transactions, isLoading } = useGetTransactionsQuery();
  const [deleteTransaction] = useDeleteTransactionMutation();
  const [createTransaction] = useCreateTransactionMutation();
  const [updateTransaction] = useUpdateTransactionMutation();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedTransaction, setSelectedTransaction] = useState<Partial<Transaction> | null>(null);

  const handleEdit = (transaction: Transaction) => {
    setSelectedTransaction(transaction);
    setIsModalOpen(true);
  };

  const handleDelete = (transaction: Transaction) => {
    if (window.confirm('Are you sure you want to delete this transaction?')) {
      deleteTransaction(transaction.id);
    }
  };

  const handleCreate = () => {
    setSelectedTransaction(null);
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
    setSelectedTransaction(null);
  };

  const handleFormSubmit = async (transaction: Partial<Transaction>) => {
    if (transaction.id) {
      await updateTransaction(transaction as Transaction);
    } else {
      await createTransaction(transaction);
    }
    handleModalClose();
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">Transaction Management</h1>
        <button onClick={handleCreate} className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
          Create Transaction
        </button>
      </div>
      <ManagementList
        items={transactions || []}
        columns={['id', 'orderId', 'amount', 'date', 'status']}
        onEdit={handleEdit}
        onDelete={handleDelete}
      />
      <TransactionFormModal
        isOpen={isModalOpen}
        onClose={handleModalClose}
        onSubmit={handleFormSubmit}
        transaction={selectedTransaction}
      />
    </div>
  );
};

export default TransactionManagementPage;
