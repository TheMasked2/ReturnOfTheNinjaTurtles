import { createBrowserRouter } from "react-router-dom";
import { HomeLayout } from "../shared/layout/HomeLayout.tsx";
import HomePage from "../shared/pages/homepage/HomePage.tsx";
import ProductsPage from "../shared/pages/productpage/ProductsPage.tsx";
import ProductDetailsPage from "../shared/pages/productpage/ProductDetailsPage.tsx";
import WishlistPage from "../shared/pages/wishlistpage/WishlistPage.tsx";
import LoginPage from "../shared/pages/loginpage/LoginPage.tsx";
import RegisterPage from "../shared/pages/registerpage/RegisterPage.tsx";
import CheckoutSummaryPage from "../shared/pages/checkoutsummarypage/CheckoutSummaryPage.tsx";
import CheckoutPage from "../shared/pages/checkoutpage/CheckoutPage.tsx";
import OrderConfirmationPage from "../shared/pages/checkoutpage/OrderConfirmationPage.tsx";
import NotFoundPage from "../shared/pages/errorpage/NotFoundPage.tsx";
import { ProtectedRoute } from "../shared/components/ProtectedRoute.tsx";
import { ProfilePage } from "../shared/pages/profilepage/ProfilePage.tsx";
import AdminDashboardPage from "./shared/pages/admin/AdminDashboardPage.tsx";
import UserManagementPage from "./shared/pages/admin/UserManagementPage.tsx";
import ProductManagementPage from "./shared/pages/admin/ProductManagementPage.tsx";
import InventoryManagementPage from "./shared/pages/admin/InventoryManagementPage.tsx";
import TransactionManagementPage from "./shared/pages/admin/TransactionManagementPage.tsx";
import OrderManagementPage from "./shared/pages/admin/OrderManagementPage.tsx";
import OrdersPage from "../shared/pages/orderpage/OrderPage.tsx";
import OrderDetailsPage from "../shared/pages/orderpage/OrderDetailsPage.tsx";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <HomeLayout />,
        children: [
            { index: true, element: <HomePage /> },
            { path: "products", element: <ProductsPage /> },
            { path: "products/:productId", element: <ProductDetailsPage /> },
            {
                path: "wishlist",
                element: (
                    <ProtectedRoute>
                        <WishlistPage />
                    </ProtectedRoute>
                ),
            },
            {
                path: "profile",
                element: (
                    <ProtectedRoute>
                        <ProfilePage />
                    </ProtectedRoute>
                ),
            },
            {
                path: "orders",
                element: (
                    <ProtectedRoute>
                        <OrdersPage />
                    </ProtectedRoute>
                ),
            },
            {
                path: "orders/:orderId",
                element: (
                    <ProtectedRoute>
                        <OrderDetailsPage />
                    </ProtectedRoute>
                ),
            },
            { path: "login", element: <LoginPage /> },
            { path: "register", element: <RegisterPage /> },
            {
                path: "admin",
                element: (
                    <ProtectedRoute role="ROLE_ADMIN">
                        <AdminDashboardPage />
                    </ProtectedRoute>
                ),
            },
            {
                path: "admin/users",
                element: (
                    <ProtectedRoute role="ROLE_ADMIN">
                        <UserManagementPage />
                    </ProtectedRoute>
                ),
            },
            {
                path: "admin/products",
                element: (
                    <ProtectedRoute role="ROLE_ADMIN">
                        <ProductManagementPage />
                    </ProtectedRoute>
                ),
            },
            {
                path: "admin/inventory",
                element: (
                    <ProtectedRoute role="ROLE_ADMIN">
                        <InventoryManagementPage />
                    </ProtectedRoute>
                ),
            },
            {
                path: "admin/transactions",
                element: (
                    <ProtectedRoute role="ROLE_ADMIN">
                        <TransactionManagementPage />
                    </ProtectedRoute>
                ),
            },
            {
                path: "admin/orders",
                element: (
                    <ProtectedRoute role="ROLE_ADMIN">
                        <OrderManagementPage />
                    </ProtectedRoute>
                ),
            },
            { path: "checkout-summary", element: <CheckoutSummaryPage /> },
            { path: "checkout", element: <ProtectedRoute><CheckoutPage /></ProtectedRoute> },
            { path: "checkout/confirmation/:orderId", element: <OrderConfirmationPage /> },
            { path: "*", element: <NotFoundPage /> },
        ],
    },
]);