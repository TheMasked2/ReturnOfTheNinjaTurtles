import { useEffect, useState } from "react";
import { ProductCard } from "../../components/product/ProductCard";
import { Pagination } from "../../components/pagination/Pagination";
import { productApi, type PaginatedResponse, type Product } from "../../api/productApi";

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const pageSize = 20;

  useEffect(() => {
    setLoading(true);
    productApi
      .getProducts(page, pageSize)
      .then((response: PaginatedResponse<Product[]>) => {
        setProducts(response.content);
        setTotalPages(response.totalPages);
        setError(null);
      })
      .catch((err) => setError(err.message || "Unable to load products."))
      .finally(() => setLoading(false));
  }, [page]);

  return (
    <div className="page">
      <section className="section">
        <div className="section-heading">
          <div>
            <h2>Products</h2>
            <p className="text-muted">Browse all available items in the TurtleShop catalog.</p>
          </div>
        </div>

        {loading ? (
          <div className="status-message">Loading products...</div>
        ) : error ? (
          <div className="status-message status-error">{error}</div>
        ) : products.length === 0 ? (
          <div className="status-message">No products are available at the moment.</div>
        ) : (
          <>
            <div className="grid grid-4">
              {products.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>

            <Pagination page={page} totalPages={totalPages} onPageChange={setPage} disabled={loading} />
          </>
        )}
      </section>
    </div>
  );
}