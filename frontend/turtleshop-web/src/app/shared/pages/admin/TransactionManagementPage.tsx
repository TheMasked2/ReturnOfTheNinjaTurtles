import React, { useState } from 'react';
import ManagementList from '../../components/admin/ManagementList';
import { useGetTransactionsQuery, useCreateTransactionMutation, useUpdateTransactionMutation, useDeleteTransactionMutation, type Transaction } from '../../api/adminApi';
import TransactionFormModal from '../../components/admin/TransactionFormModal';

const TransactionManagementPage: React.FC = () => {
  const { data: transactions, isLoading, refetch } = useGetTransactionsQuery();
  const [deleteTransaction] = useDeleteTransactionMutation();
  const [createTransaction] = useCreateTransactionMutation();
  const [updateTransaction] = useUpdateTransactionMutation();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedTransaction, setSelectedTransaction] = useState<Partial<Transaction> | null>(null);

  type DisplayTransaction = Transaction & { id: number; date: string };

  const handleEdit = (transaction: DisplayTransaction) => {
    setSelectedTransaction(transaction);
    setIsModalOpen(true);
  };

  const handleDelete = async (transaction: DisplayTransaction) => {
    if (window.confirm('Are you sure you want to delete this transaction?')) {
      await deleteTransaction(transaction.transactionId);
      await refetch();
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
    if (transaction.transactionId) {
      await updateTransaction(transaction as Transaction);
    } else {
      await createTransaction({
        orderId: Number(transaction.orderId),
        paymentMethodId: transaction.paymentMethodId ?? 0,
        amount: transaction.amount ?? 0,
        status: transaction.status ?? "PENDING",
      });
    }
    handleModalClose();
    await refetch();
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  const formattedTransactions = (transactions || []).map((transaction) => ({
    ...transaction,
    id: transaction.transactionId,
    date: transaction.transactionDate,
  })) as DisplayTransaction[];

  return (
    <div className="container mx-auto px-4 py-8 admin-management-page">
      <div className="management-card">
        <div className="management-header">
          <div>
            <h1>Transaction Management</h1>
            <p className="management-description">Manage payment records and transaction status with a clean, scrollable ledger view.</p>
          </div>
          <button onClick={handleCreate} className="button button-secondary">
            Create Transaction
          </button>
        </div>
        <ManagementList
          items={formattedTransactions}
          columns={['id', 'orderId', 'amount', 'date', 'status']}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      </div>
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
