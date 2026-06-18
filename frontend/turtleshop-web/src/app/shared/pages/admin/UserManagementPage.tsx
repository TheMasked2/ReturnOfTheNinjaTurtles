import React, { useState } from 'react';
import ManagementList from '../../components/admin/ManagementList';
import { useGetUsersQuery, useCreateUserMutation, useUpdateUserMutation, useDeleteUserMutation } from '../../api/adminApi';
import UserFormModal from '../../components/admin/UserFormModal';
import type { User } from '../../../../shared/auth/AuthContext';

const UserManagementPage: React.FC = () => {
  const { data: users, isLoading, refetch } = useGetUsersQuery();
  const [deleteUser] = useDeleteUserMutation();
  const [createUser] = useCreateUserMutation();
  const [updateUser] = useUpdateUserMutation();

  type UserFormData = Partial<User> & { password?: string };

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<UserFormData | null>(null);

  const handleEdit = (user: User) => {
    setSelectedUser(user);
    setIsModalOpen(true);
  };

  const handleDelete = async (user: User) => {
    if (window.confirm('Are you sure you want to delete this user?')) {
      await deleteUser(user.id);
      await refetch();
    }
  };

  const handleCreate = () => {
    setSelectedUser(null);
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
    setSelectedUser(null);
  };

  const handleFormSubmit = async (user: UserFormData) => {
    if (user.id) {
      await updateUser(user as Partial<User> & { id: string });
    } else {
      await createUser(user as Partial<User> & { password: string });
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
            <h1>User Management</h1>
            <p className="management-description">Manage customer accounts, update details, and keep user information aligned with the backend.</p>
          </div>
          <button onClick={handleCreate} className="button button-secondary">
            Create User
          </button>
        </div>
        <ManagementList
          items={users || []}
          columns={['id', 'firstName', 'lastName', 'email', 'roles']}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      </div>
      <UserFormModal
        isOpen={isModalOpen}
        onClose={handleModalClose}
        onSubmit={handleFormSubmit}
        user={selectedUser}
      />
    </div>
  );
};

export default UserManagementPage;
