import { useEffect, useState } from "react";
import { useAuth } from "../auth/AuthContext";
import { userApi } from "../api/userApi";
import type { User } from "../auth/AuthContext";
import "./ProfilePage.css";

export function ProfilePage() {
    const { user: authUser } = useAuth();
    const [user, setUser] = useState<User | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        userApi.getMe()
            .then(setUser)
            .catch((err) => setError(err.message || "Unable to load profile."))
            .finally(() => setLoading(false));
    }, []);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <div className="page page-profile">
            <section className="hero-card">
                <div>
                    <span className="eyebrow">My Profile</span>
                    <h1>{user?.username}</h1>
                    <p><strong>Email:</strong> {user?.email}</p>
                </div>
            </section>
        </div>
    );
}
