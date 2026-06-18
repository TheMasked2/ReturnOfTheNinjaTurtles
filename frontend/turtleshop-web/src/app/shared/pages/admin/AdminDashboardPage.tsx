import React from 'react';
import { Link } from 'react-router-dom';

const AdminDashboardPage: React.FC = () => {
  return (
    <div className="container mx-auto px-4 py-8">
      <section className="page page-home admin-dashboard">
        <div className="hero-card">
          <div>
            <span className="eyebrow">Admin Center</span>
            <h1>Manage the TurtleShop backend with confidence.</h1>
            <p>Navigate to user, product, inventory, transaction, and order management pages to update the database directly.</p>
          </div>
          <div className="hero-panel">
            <div className="section-heading">
              <h2>Quick actions</h2>
            </div>
            <div className="grid grid-4">
              <Link to="/admin/users" className="button button-secondary">
                Users
              </Link>
              <Link to="/admin/products" className="button button-secondary">
                Products
              </Link>
              <Link to="/admin/inventory" className="button button-secondary">
                Inventory
              </Link>
              <Link to="/admin/transactions" className="button button-secondary">
                Transactions
              </Link>
              <Link to="/admin/orders" className="button button-secondary">
                Orders
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default AdminDashboardPage;
