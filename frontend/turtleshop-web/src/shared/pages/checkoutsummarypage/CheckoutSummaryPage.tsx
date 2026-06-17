import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { cartApi } from "../../api/cartApi";
import { productApi, type Product } from "../../api/productApi";
import { recommendationApi, type RecommendedProduct } from "../../api/recommendationApi";
import { publishHeaderRefresh } from "../../state/refreshBus";
import { ProductCard } from "../../components/product/ProductCard";

type CartSummaryItem = {
  cartItemId: number;
  productId: number;
  quantity: number;
  name: string;
  unitPrice: number;
  subtotal: number;
};

export default function CheckoutSummaryPage() {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const [cartItems, setCartItems] = useState<CartSummaryItem[]>([]);
  const [recommendedProducts, setRecommendedProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [processingItemId, setProcessingItemId] = useState<number | null>(null);

  const loadSummary = useCallback(async () => {
    if (!isAuthenticated || !user?.id) {
      setCartItems([]);
      setRecommendedProducts([]);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const cart = await cartApi.getActiveCart(user.id);

      const cartItemsData = (cart.items ?? []) as Array<{
        cartItemId: number;
        productId: number;
        quantity?: number;
      }>;

      const cartProductIds = Array.from(
        new Set(
          cartItemsData
            .map((item) => item.productId)
            .filter((productId): productId is number => typeof productId === "number")
        )
      );

      const cartProducts = cartProductIds.length
        ? await productApi.getProductsByIds(cartProductIds).catch(() => [] as Product[])
        : [];

      const cartProductIdSet = new Set<number>(cartProductIds);

      const mappedCartItems: CartSummaryItem[] = cartItemsData
        .map((item) => {
          const product = cartProducts.find((p) => p.id === item.productId);
          const quantity = item.quantity ?? 1;
          const unitPrice = product?.price ?? 0;

          return {
            cartItemId: item.cartItemId,
            productId: item.productId,
            quantity,
            name: product?.name ?? `Product #${item.productId}`,
            unitPrice,
            subtotal: unitPrice * quantity,
          };
        })
        .sort((a, b) => a.cartItemId - b.cartItemId);

      setCartItems(mappedCartItems);

      const recommendationResponse = await recommendationApi.getPopularThisMonth(4);
      const recommendedProductIds = recommendationResponse
        .map((item: RecommendedProduct) => item.productId)
        .filter((id): id is number => typeof id === "number");

      const recommended = recommendedProductIds.length
        ? await productApi.getProductsByIds(recommendedProductIds).catch(() => [] as Product[])
        : [];

      setRecommendedProducts(
        recommended
          .filter((product) => !cartProductIdSet.has(product.id))
          .slice(0, 4)
      );
    } catch (err: any) {
      setError(err?.message || "Unable to load order summary.");
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, user?.id]);

  useEffect(() => {
    loadSummary();
  }, [loadSummary]);

  const subtotal = cartItems.reduce((sum, item) => sum + item.subtotal, 0);

  const handleQuantityChange = async (cartItemId: number, quantity: number) => {
    if (quantity <= 0) {
      await handleRemoveItem(cartItemId);
      return;
    }

    setProcessingItemId(cartItemId);

    try {
      await cartApi.updateItemQuantity(cartItemId, quantity);
      await loadSummary();
      publishHeaderRefresh();
    } catch (err) {
      console.error("Failed to update cart quantity", err);
      setError("Unable to update quantity right now.");
    } finally {
      setProcessingItemId(null);
    }
  };

  const handleRemoveItem = async (cartItemId: number) => {
    setProcessingItemId(cartItemId);

    try {
      await cartApi.removeItem(cartItemId);
      await loadSummary();
      publishHeaderRefresh();
    } catch (err) {
      console.error("Failed to remove cart item", err);
      setError("Unable to remove item right now.");
    } finally {
      setProcessingItemId(null);
    }
  };

  return (
    <div className="page">
      <section className="section">
        <div className="section-heading">
          <div>
            <h2>Order summary</h2>
            <p className="text-muted">
              Review the items in your cart, update quantities, and continue to checkout.
            </p>
          </div>
        </div>

        {loading ? (
          <div className="status-message">Loading order summary...</div>
        ) : !isAuthenticated ? (
          <div className="status-message">
            Please <Link to="/login">log in</Link> to continue to checkout.
          </div>
        ) : error ? (
          <div className="status-message status-error">{error}</div>
        ) : (
          <>
            <div
              className="checkout-grid"
              style={{ display: "grid", gridTemplateColumns: "minmax(0, 1fr) 320px", gap: "1rem" }}
            >
              <div className="card">
                <h3>Cart items</h3>

                {cartItems.length === 0 ? (
                  <div className="status-message">Your cart is empty.</div>
                ) : (
                  <div className="cart-items-list">
                    {cartItems.map((item) => (
                      <div key={item.cartItemId} className="cart-summary-item">
                        <div>
                          <strong>{item.name}</strong>
                          <div className="text-muted">
                            {item.quantity} × ${item.unitPrice.toFixed(2)}
                          </div>
                        </div>

                        <div className="cart-summary-actions">
                          <div className="cart-item-quantity">
                            <button
                              type="button"
                              disabled={processingItemId === item.cartItemId}
                              onClick={() => handleQuantityChange(item.cartItemId, item.quantity - 1)}
                            >
                              -
                            </button>
                            <span>{item.quantity}</span>
                            <button
                              type="button"
                              disabled={processingItemId === item.cartItemId}
                              onClick={() => handleQuantityChange(item.cartItemId, item.quantity + 1)}
                            >
                              +
                            </button>
                          </div>

                          <div className="cart-summary-price">
                            <strong>${item.subtotal.toFixed(2)}</strong>
                          </div>

                          <button
                            type="button"
                            className="button button-link"
                            disabled={processingItemId === item.cartItemId}
                            onClick={() => handleRemoveItem(item.cartItemId)}
                          >
                            Remove
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <aside className="card">
                <h3>Price summary</h3>

                <div className="summary-line">
                  <span>Subtotal</span>
                  <span>${subtotal.toFixed(2)}</span>
                </div>

                <button
                  className="button button-primary"
                  type="button"
                  disabled={cartItems.length === 0}
                  onClick={() => navigate("/checkout")}
                >
                  Proceed to checkout
                </button>
              </aside>
            </div>

            <div className="card" style={{ marginTop: "1rem" }}>
              <h3>Products often ordered with your items</h3>

              {recommendedProducts.length === 0 ? (
                <div className="status-message">We couldn't find recommendations right now.</div>
              ) : (
                <div className="grid grid-4">
                  {recommendedProducts.map((product) => (
                    <ProductCard
                      key={product.id}
                      product={product}
                      onAddedToCart={loadSummary}
                    />
                  ))}
                </div>
              )}
            </div>
          </>
        )}
      </section>
    </div>
  );
}