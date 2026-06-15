import { Outlet } from "react-router-dom";
import { PublicNavbar } from "../components/navbar/PublicNavbar";

export function HomeLayout() {
    return (
        <div className="app-shell">
            <PublicNavbar />
            <main className="main-content">
                <Outlet />
            </main>
        </div>
    );
}