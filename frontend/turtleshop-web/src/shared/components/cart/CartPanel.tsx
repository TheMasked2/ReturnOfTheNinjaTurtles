import { useEffect, useRef, useState, type RefObject } from "react";
import { useNavigate } from "react-router-dom";
import { cartApi } from "../../api/cartApi";
import { publishHeaderRefresh } from "../../state/refreshBus";

interface CartPanelProps {
  isOpen: boolean;
  items: any[];
  onClose: () => void;
  toggleRef: RefObject<HTMLAnchorElement | null>;
}

export function CartPanel({ isOpen, items, onClose, toggleRef }: CartPanelProps) {
  const panelRef = useRef<HTMLDivElement | null>(null);
  const navigate = useNavigate();
  const [processingItemId, setProcessingItemId] = useState<number | null>(null);

  useEffect(() => {
    if (!isOpen) return;

    const handleDocumentClick = (event: MouseEvent) => {
      const target = event.target as Node;
      if (
        panelRef.current?.contains(target) ||
        toggleRef.current?.contains(target)
      ) {
        return;
      }
      onClose();
    };

    document.addEventListener("mousedown", handleDocumentClick);
    return () => {
      document.removeEventListener("mousedown", handleDocumentClick);
    };
  }, [isOpen, onClose, toggleRef]);

  const handleQuantityChange = async (cartItemId: number, quantity: number) => {
    if (quantity <= 0) {
      await handleRemoveItem(cartItemId);
      return;
    }

    setProcessingItemId(cartItemId);
    try {
      await cartApi.updateItemQuantity(cartItemId, quantity);
      publishHeaderRefresh();
    } catch (error) {
      console.error("Failed to update cart quantity", error);
    } finally {
      setProcessingItemId(null);
    }
  };

  const handleRemoveItem = async (cartItemId: number) => {
    setProcessingItemId(cartItemId);
    try {
      await cartApi.removeItem(cartItemId);
      publishHeaderRefresh();
    } catch (error) {
      console.error("Failed to remove cart item", error);
    } finally {
      setProcessingItemId(null);
    }
  };

  if (!isOpen) return null;

  return (
    <>
      <div className="cart-backdrop" onClick={onClose} />
      <aside className="cart-panel card" ref={panelRef}>
        <div className="cart-panel-header">
          <h3>Shopping cart</h3>
        </div>

        <div className="cart-panel-items">
          {items.length === 0 ? (
            <div className="cart-dropdown-empty">Cart is empty.</div>
          ) : (
            <div
              className="panel-scroll-container"
              style={{ maxHeight: "350px", overflowY: "auto", paddingRight: "0.5rem" }}
            >
              {items.map((item) => (
                <div key={item.cartItemId} className="cart-dropdown-item">
                  <div>
                    <strong>{item.name ?? item.productName ?? `Product #${item.productId}`}</strong>
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
                  </div>

                  <div className="cart-dropdown-actions">
                    {item.totalPrice != null && (
                      <span className="price-tag">${item.totalPrice.toFixed(2)}</span>
                    )}
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

        <button
          className="button button-primary"
          type="button"
          disabled={items.length === 0}
          onClick={() => {
            onClose();
            navigate("/cart");
          }}
        >
          Checkout
        </button>
      </aside>
    </>
  );
}