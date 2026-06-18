import { useEffect, useState } from "react";
import { ProductCard } from "../../components/product/ProductCard";
import { Pagination } from "../../components/pagination/Pagination";
import { productApi, type PaginatedResponse, type Product, type ProductCategory } from "../../api/productApi";
import { ProductFilters, type ProductFiltersState } from "../../components/product/ProductFilters";

const initialFilters: ProductFiltersState = {
  search: "",
  sortBy: "",
  minPrice: "",
  maxPrice: "",
  categoryId: "",
};

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<ProductCategory[]>([]);
  const [filters, setFilters] = useState<ProductFiltersState>(initialFilters);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [categoriesLoading, setCategoriesLoading] = useState(true);

  const pageSize = 20;

  useEffect(() => {
    setCategoriesLoading(true);
    productApi
      .getCategories()
      .then(setCategories)
      .catch(() => setCategories([]))
      .finally(() => setCategoriesLoading(false));
  }, []);

  useEffect(() => {
    setLoading(true);
    setError(null);

    const params = {
      search: filters.search.trim() || undefined,
      sortBy: filters.sortBy || undefined,
      minPrice: filters.minPrice ? Number(filters.minPrice) : undefined,
      maxPrice: filters.maxPrice ? Number(filters.maxPrice) : undefined,
      categoryId: filters.categoryId ? Number(filters.categoryId) : undefined,
      page,
      pageSize,
    };

    productApi
      .getProducts(params)
      .then((response: PaginatedResponse<Product[]>) => {
        setProducts(response.content);
        setTotalPages(response.totalPages);
      })
      .catch((err) => setError(err.message || "Unable to load products."))
      .finally(() => setLoading(false));
  }, [filters, page]);

  const handleFilterChange = (updatedFilters: ProductFiltersState) => {
    setFilters(updatedFilters);
    setPage(0);
  };

  return (
    <div className="page">
      <section className="section">
        <div className="section-heading">
          <div>
            <h2>Products</h2>
            <p className="text-muted">Browse all available items in the TurtleShop catalog.</p>
          </div>
        </div>

        <div className="product-layout-grid">
          <aside className="product-filters-sidebar">
            <ProductFilters
              categories={categories}
              filters={filters}
              onChange={handleFilterChange}
            />
            {categoriesLoading && <div className="status-message">Loading categories...</div>}
          </aside>

          <main className="product-listing-content">
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
          </main>
        </div>
      </section>
    </div>
  );
}