import { createBrowserRouter } from "react-router-dom";
import { HomeLayout } from "../shared/layout/HomeLayout.tsx";
import HomePage from "../shared/pages/homepage/HomePage.tsx";
import ProductsPage from "../shared/pages/productpage/ProductsPage.tsx";
import ProductDetailsPage from "../shared/pages/productpage/ProductDetailsPage.tsx";
import WishlistPage from "../shared/pages/wishlistpage/WishlistPage.tsx";
import LoginPage from "../shared/pages/loginpage/LoginPage.tsx";
import RegisterPage from "../shared/pages/registerpage/RegisterPage.tsx";
import NotFoundPage from "../shared/pages/errorpage/NotFoundPage.tsx";
import { ProtectedRoute } from "../shared/components/ProtectedRoute.tsx";
import { ProfilePage } from "../shared/pages/profilepage/ProfilePage.tsx";

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
            { path: "login", element: <LoginPage /> },
            { path: "register", element: <RegisterPage /> },
            { path: "*", element: <NotFoundPage /> },
        ],
    },
]);