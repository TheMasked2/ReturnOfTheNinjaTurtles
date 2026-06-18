import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { userApi, type ProfileUpdatePayload } from "../../api/userApi";
import type { User } from "../../auth/AuthContext";
import "./ProfilePage.css";

export function ProfilePage() {
    const [user, setUser] = useState<User | null>(null);
    const [formData, setFormData] = useState<ProfileUpdatePayload>({
        email: "",
        firstName: "",
        lastName: "",
        phone: "",
        address: "",
        city: "",
        postalCode: "",
        country: "",
        password: "",
    });
    const [error, setError] = useState<string | null>(null);
    const [saveError, setSaveError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [editing, setEditing] = useState(false);

    useEffect(() => {
        userApi.getMe()
            .then((data) => {
                setUser(data);
                setFormData({
                    email: data.email || "",
                    firstName: data.firstName || "",
                    lastName: data.lastName || "",
                    phone: data.phone || "",
                    address: data.address || "",
                    city: data.city || "",
                    postalCode: data.postalCode || "",
                    country: data.country || "",
                    password: "",
                });
            })
            .catch((err) => setError(err.message || "Unable to load profile."))
            .finally(() => setLoading(false));
    }, []);

    const handleChange = (field: keyof ProfileUpdatePayload) => (event: React.ChangeEvent<HTMLInputElement>) => {
        setFormData((prev) => ({ ...prev, [field]: event.target.value }));
    };

        const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        if (!user?.id) return;
    
        setSaving(true);
        setSaveError(null);
    
        try {
            const updated = await userApi.updateProfile(user.id, {
                email: formData.email?.trim() || undefined,
                firstName: formData.firstName?.trim() || undefined,
                lastName: formData.lastName?.trim() || undefined,
                phone: formData.phone?.trim() || undefined,
                address: formData.address?.trim() || undefined,
                city: formData.city?.trim() || undefined,
                postalCode: formData.postalCode?.trim() || undefined,
                country: formData.country?.trim() || undefined,
                password: formData.password?.trim() || undefined,
            });
            setUser(updated);
            setEditing(false);
            setFormData((prev) => ({ ...prev, password: "" }));
        } catch (err: unknown) {
            setSaveError((err as Error).message || "Unable to save profile.");
        } finally {
            setSaving(false);
        }
    };

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
                    <h1 className="hero-title">
                        Welcome back, {user?.firstName ? `${user.firstName.charAt(0)}.` : "Turtle Lover"}!
                    </h1>
                    <p><strong>Email:</strong> {user?.email}</p>
                    <div className="profile-actions">
                        <Link to="/wishlist" className="button button-secondary">
                            View wishlist
                        </Link>
                        <Link to="/orders" className="button button-primary">
                            View orders
                        </Link>
                    </div>
                </div>
            </section>

            <section className="profile-card profile-details">
                <div className="profile-details-header">
                    <div>
                        <span className="eyebrow">Profile Details</span>
                        <h2 className="section-title">Account information</h2>
                    </div>
                    <button
                        type="button"
                        className="button button-secondary"
                        onClick={() => setEditing((value) => !value)}
                    >
                        {editing ? "Cancel" : "Edit details"}
                    </button>
                </div>

                {saveError && <div className="form-error">{saveError}</div>}

                <form className="profile-form" onSubmit={handleSubmit}>
                    <div className="form-grid">
                        <label className="form-field">
                            Email
                            <input
                                type="email"
                                value={formData.email}
                                onChange={handleChange("email")}
                                disabled={!editing}
                            />
                        </label>
                        <label className="form-field">
                            First name
                            <input
                                type="text"
                                value={formData.firstName}
                                onChange={handleChange("firstName")}
                                disabled={!editing}
                            />
                        </label>
                        <label className="form-field">
                            Last name
                            <input
                                type="text"
                                value={formData.lastName}
                                onChange={handleChange("lastName")}
                                disabled={!editing}
                            />
                        </label>
                        <label className="form-field">
                            Phone
                            <input
                                type="tel"
                                value={formData.phone}
                                onChange={handleChange("phone")}
                                disabled={!editing}
                            />
                        </label>
                        <label className="form-field">
                            Address
                            <input
                                type="text"
                                value={formData.address}
                                onChange={handleChange("address")}
                                disabled={!editing}
                            />
                        </label>
                        <label className="form-field">
                            City
                            <input
                                type="text"
                                value={formData.city}
                                onChange={handleChange("city")}
                                disabled={!editing}
                            />
                        </label>
                        <label className="form-field">
                            Postal code
                            <input
                                type="text"
                                value={formData.postalCode}
                                onChange={handleChange("postalCode")}
                                disabled={!editing}
                            />
                        </label>
                        <label className="form-field">
                            Country
                            <input
                                type="text"
                                value={formData.country}
                                onChange={handleChange("country")}
                                disabled={!editing}
                            />
                        </label>
                        <label className="form-field">
                            New password
                            <input
                                type="password"
                                value={formData.password}
                                onChange={handleChange("password")}
                                disabled={!editing}
                                autoComplete="new-password"
                            />
                        </label>
                    </div>

                    {editing && (
                        <div className="form-actions">
                            <button className="button button-primary" type="submit" disabled={saving}>
                                {saving ? "Saving..." : "Save changes"}
                            </button>
                        </div>
                    )}
                </form>
            </section>
        </div>
    );
}