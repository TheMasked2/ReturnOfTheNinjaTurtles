import React from 'react';
import { Link } from 'react-router-dom';

const AdminDashboardPage: React.FC = () => {
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">Admin Dashboard</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <Link to="/admin/users" className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-bold mb-2">User Management</h2>
          <p>Manage users, roles, and permissions.</p>
        </Link>
        <Link to="/admin/inventory" className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-bold mb-2">Inventory Management</h2>
          <p>Track and manage product inventory.</p>
        </Link>
        <Link to="/admin/products" className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-bold mb-2">Product Management</h2>
          <p>Add, edit, and remove products.</p>
        </Link>
        <Link to="/admin/transactions" className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-bold mb-2">Transaction Management</h2>
          <p>View and manage transactions.</p>
        </Link>
        <Link to="/admin/orders" className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-bold mb-2">Order Management</h2>
          <p>Manage customer orders.</p>
        </Link>
      </div>
    </div>
  );
};

export default AdminDashboardPage;
