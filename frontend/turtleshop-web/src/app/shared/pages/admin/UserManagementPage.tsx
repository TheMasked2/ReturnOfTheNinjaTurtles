import React, { useState } from 'react';
import ManagementList from '../../components/admin/ManagementList';
import { useGetUsersQuery, useCreateUserMutation, useUpdateUserMutation, useDeleteUserMutation } from '../../api/adminApi';
import UserFormModal from '../../components/admin/UserFormModal';
import type { User } from '../../../../shared/auth/AuthContext';

const UserManagementPage: React.FC = () => {
  const { data: users, isLoading } = useGetUsersQuery();
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

  const handleDelete = (user: User) => {
    if (window.confirm('Are you sure you want to delete this user?')) {
      deleteUser(user.id);
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
      await updateUser(user as User);
    } else {
      await createUser(user);
    }
    handleModalClose();
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">User Management</h1>
        <button onClick={handleCreate} className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
          Create User
        </button>
      </div>
      <ManagementList
        items={users || []}
        columns={['id', 'firstName', 'lastName', 'email', 'role']}
        onEdit={handleEdit}
        onDelete={handleDelete}
      />
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
