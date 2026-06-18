import { useMemo } from "react";

export interface ProductCategory {
  id: number;
  name: string;
}

export interface ProductFiltersState {
  search: string;
  sortBy: "price_asc" | "price_desc" | "";
  minPrice: string;
  maxPrice: string;
  categoryId: string;
}

interface ProductFiltersProps {
  categories: ProductCategory[];
  filters: ProductFiltersState;
  onChange: (filters: ProductFiltersState) => void;
}

export function ProductFilters({ categories, filters, onChange }: ProductFiltersProps) {
  const canClear = useMemo(
    () =>
      filters.search !== "" ||
      filters.sortBy !== "" ||
      filters.minPrice !== "" ||
      filters.maxPrice !== "" ||
      filters.categoryId !== "",
    [filters]
  );

  return (
    <div className="product-filters card">
      <div className="filter-heading">
        <h3>Filter products</h3>
      </div>
      <div className="filter-row">
        <label>
          Search
          <input
            type="search"
            value={filters.search}
            placeholder="Search by name"
            onChange={(event) => onChange({ ...filters, search: event.target.value })}
          />
        </label>

        <label>
          Category
          <select
            value={filters.categoryId}
            onChange={(event) => onChange({ ...filters, categoryId: event.target.value })}
          >
            <option value="">All categories</option>
            {categories.map((category) => (
              <option key={category.id} value={String(category.id)}>
                {category.name}
              </option>
            ))}
          </select>
        </label>

        <label>
          Sort by price
          <select
            value={filters.sortBy}
            onChange={(event) =>
              onChange({ ...filters, sortBy: event.target.value as ProductFiltersState["sortBy"] })
            }
          >
            <option value="">None</option>
            <option value="price_asc">Low to high</option>
            <option value="price_desc">High to low</option>
          </select>
        </label>
      </div>

      <div className="filter-row">
        <label>
          Min price
          <input
            type="number"
            min="0"
            value={filters.minPrice}
            onChange={(event) => onChange({ ...filters, minPrice: event.target.value })}
          />
        </label>

        <label>
          Max price
          <input
            type="number"
            min="0"
            value={filters.maxPrice}
            onChange={(event) => onChange({ ...filters, maxPrice: event.target.value })}
          />
        </label>

        <button
          type="button"
          className="button button-ghost"
          disabled={!canClear}
          onClick={() =>
            onChange({
              search: "",
              sortBy: "",
              minPrice: "",
              maxPrice: "",
              categoryId: "",
            })
          }
        >
          Clear filters
        </button>
      </div>
    </div>
  );
}