import { Outlet } from "react-router-dom";

export function HomeLayout() {
    return (
        <div>
            <nav style={{ padding: '10px', borderBottom: '1px solid #ccc' }}>
                <strong>TurtleShop</strong>
            </nav>
            <main>
                <Outlet /> {/* CHILD ROUTES RENDER HERE */}
            </main>
        </div>
    );
}