import { type FormEvent, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await login({ email, password });
      navigate("/");
    } catch (err: any) {
      setError(err.message || "Unable to login. Please check your credentials.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <section className="section form-panel">
        <div className="section-heading">
          <div>
            <h2>Login</h2>
            <p className="text-muted">Sign in to access your wishlist and profile.</p>
          </div>
        </div>

        <form className="form-grid" onSubmit={handleSubmit}>
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
              placeholder="Enter your password"
            />
          </div>

          {error && <div className="status-message status-error">{error}</div>}

          <button className="button button-primary" type="submit" disabled={loading}>
            {loading ? "Signing in..." : "Sign in"}
          </button>

          <p className="text-muted">
            New here? <Link to="/register">Create an account</Link>.
          </p>
        </form>
      </section>
    </div>
  );
}
