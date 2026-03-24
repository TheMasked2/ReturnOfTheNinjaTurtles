import { createBrowserRouter } from "react-router-dom";
import { HomeLayout } from "../shared/layout/HomeLayout.tsx";
import HomePage from "../shared/pages/HomePage.tsx";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <HomeLayout />,
        children: [
            { index: true, element: <HomePage /> }, // This makes it the default view
        ],
    },
]);