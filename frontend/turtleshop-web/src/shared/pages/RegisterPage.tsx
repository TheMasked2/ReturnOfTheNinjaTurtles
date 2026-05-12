import { FormEvent, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await register({ username, password, email });
      navigate("/");
    } catch (err: any) {
      setError(err.message || "Unable to register account.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <section className="section form-panel">
        <div className="section-heading">
          <div>
            <h2>Register</h2>
            <p className="text-muted">Create a new account to save wishlists and checkout faster.</p>
          </div>
        </div>

        <form className="form-grid" onSubmit={handleSubmit}>
          <div className="form-field">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
              placeholder="Choose a username"
            />
          </div>

          <div className="form-field">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
              placeholder="Enter your email"
            />
          </div>

          <div className="form-field">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              placeholder="Choose a password"
            />
          </div>

          {error && <div className="status-message status-error">{error}</div>}

          <button className="button button-primary" type="submit" disabled={loading}>
            {loading ? "Creating account..." : "Register"}
          </button>

          <p className="text-muted">
            Already have an account? <Link to="/login">Sign in</Link>.
          </p>
        </form>
      </section>
    </div>
  );
}
