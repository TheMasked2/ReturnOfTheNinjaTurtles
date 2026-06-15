import { Link } from "react-router-dom";

export default function NotFoundPage() {
  return (
    <div className="page">
      <section className="section form-panel">
        <div className="section-heading">
          <div>
            <h2>Page not found</h2>
            <p className="text-muted">The page you are looking for does not exist.</p>
          </div>
        </div>
        <p className="status-message">Return to the homepage to continue browsing.</p>
        <Link className="button button-primary" to="/">
          Go home
        </Link>
      </section>
    </div>
  );
}