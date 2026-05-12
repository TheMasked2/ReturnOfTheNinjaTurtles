import { createBrowserRouter } from "react-router-dom";
import { HomeLayout } from "../shared/layout/HomeLayout.tsx";
import HomePage from "../shared/pages/HomePage.tsx";
import ProductsPage from "../shared/pages/ProductsPage.tsx";
import ProductDetailsPage from "../shared/pages/ProductDetailsPage.tsx";
import WishlistPage from "../shared/pages/WishlistPage.tsx";
import LoginPage from "../shared/pages/LoginPage.tsx";
import RegisterPage from "../shared/pages/RegisterPage.tsx";
import NotFoundPage from "../shared/pages/NotFoundPage.tsx";
import { ProtectedRoute } from "../shared/components/ProtectedRoute.tsx";

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
            { path: "login", element: <LoginPage /> },
            { path: "register", element: <RegisterPage /> },
            { path: "*", element: <NotFoundPage /> },
        ],
    },
]);