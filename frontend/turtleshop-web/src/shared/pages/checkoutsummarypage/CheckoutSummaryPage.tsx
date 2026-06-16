import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { cartApi } from "../../api/cartApi";
import { productApi, type Product } from "../../api/productApi";
import { recommendationApi, type RecommendedProduct } from "../../api/recommendationApi";
import { publishHeaderRefresh } from "../../state/refreshBus";
import { ProductCard } from "../../components/product/ProductCard";

const SHIPPING_RATE = 4.99;
const FREE_SHIPPING_THRESHOLD = 100;

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

  useEffect(() => {
    if (!isAuthenticated || !user?.id) {
      setCartItems([]);
      setRecommendedProducts([]);
      setLoading(false);
      return;
    }

    const loadSummary = async () => {
      setLoading(true);
      setError(null);

      try {
        const [products, cart] = await Promise.all([
          productApi.getProducts(),
          cartApi.getActiveCart(user.id),
        ]);

        const cartProductIds = new Set<number>();

        const mappedCartItems: CartSummaryItem[] = (cart.items ?? [])
          .map((item: any) => {
            const product = products.find((p) => p.id === item.productId);
            const unitPrice = product?.price ?? 0;
            cartProductIds.add(item.productId);

            return {
              cartItemId: item.cartItemId,
              productId: item.productId,
              quantity: item.quantity ?? 1,
              name: product?.name ?? `Product #${item.productId}`,
              unitPrice,
              subtotal: unitPrice * (item.quantity ?? 1),
            };
          })
          .sort((a: CartSummaryItem, b: CartSummaryItem) => a.cartItemId - b.cartItemId);

        setCartItems(mappedCartItems);

        let recommended: Product[] = [];
        try {
          const recommendationResponse = await recommendationApi.getSeasonalRecommendations(user.id);
          recommended = recommendationResponse
            .map((item: RecommendedProduct) => {
              const product = products.find((p) => p.id === (item.id ?? item.productId));
              const id = item.id ?? item.productId ?? 0;
              return {
                id,
                name: item.name ?? product?.name ?? `Product #${id}`,
                description: item.description ?? product?.description ?? "",
                specs: item.specs ?? product?.specs ?? "",
                price: item.price ?? product?.price ?? 0,
                availableSince: item.availableSince ?? product?.availableSince ?? "",
                suggestedProducts: item.suggestedProducts ?? product?.suggestedProducts ?? [],
              } as Product;
            })
            .filter((product) => product.id && !cartProductIds.has(product.id))
            .slice(0, 4);
        } catch {
          recommended = [];
        }

        if (recommended.length === 0) {
          recommended = products
            .filter((product) => !cartProductIds.has(product.id))
            .slice(0, 4);
        }

        setRecommendedProducts(recommended);
      } catch (err: any) {
        setError(err.message || "Unable to load order summary.");
      } finally {
        setLoading(false);
      }
    };

    loadSummary();
  }, [isAuthenticated, user?.id]);

  const subtotal = cartItems.reduce((sum, item) => sum + item.subtotal, 0);
  const shipping = subtotal === 0 ? 0 : subtotal >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_RATE;
  const total = subtotal + shipping;
  const shippingLabel = subtotal === 0 ? "$0.00" : shipping === 0 ? "Free" : `$${shipping.toFixed(2)}`;

  const handleQuantityChange = async (cartItemId: number, quantity: number) => {
    if (quantity <= 0) {
      await handleRemoveItem(cartItemId);
      return;
    }

    setProcessingItemId(cartItemId);
    try {
      await cartApi.updateItemQuantity(cartItemId, quantity);
      setCartItems((prev) =>
        prev.map((item) =>
          item.cartItemId === cartItemId
            ? { ...item, quantity, subtotal: item.unitPrice * quantity }
            : item
        )
      );
      publishHeaderRefresh();
    } catch (err) {
      console.error("Failed to update cart quantity", err);
    } finally {
      setProcessingItemId(null);
    }
  };

  const handleRemoveItem = async (cartItemId: number) => {
    setProcessingItemId(cartItemId);
    try {
      await cartApi.removeItem(cartItemId);
      setCartItems((prev) => prev.filter((item) => item.cartItemId !== cartItemId));
      publishHeaderRefresh();
    } catch (err) {
      console.error("Failed to remove cart item", err);
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
                <div className="summary-line">
                  <span>Shipping</span>
                  <span>{shippingLabel}</span>
                </div>
                <div className="summary-line summary-total">
                  <span>Total</span>
                  <strong>${total.toFixed(2)}</strong>
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
                <div className="status-message">
                  We couldn't find recommendations right now.
                </div>
              ) : (
                <div className="grid grid-4">
                  {recommendedProducts.map((product) => (
                    <ProductCard key={product.id} product={product} />
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