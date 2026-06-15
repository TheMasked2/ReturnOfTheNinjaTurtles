import { useEffect, useRef, type RefObject } from "react";
import { useNavigate } from "react-router-dom";

interface CartPanelProps {
  isOpen: boolean;
  items: any[];
  onClose: () => void;
  toggleRef: RefObject<HTMLAnchorElement | null>;
}

export function CartPanel({ isOpen, items, onClose, toggleRef }: CartPanelProps) {
    const panelRef = useRef<HTMLDivElement | null>(null);
    const navigate = useNavigate();

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
                        items.map((item, index) => (
                            <div key={item.id ?? index} className="cart-dropdown-item">
                                <div>
                                    <strong>{item.productName ?? item.name ?? "Item"}</strong>
                                    <div className="text-muted">
                                        {item.quantity ? `Qty: ${item.quantity}` : "Qty: 1"}
                                    </div>
                                </div>
                                {item.totalPrice != null && (
                                    <span className="price-tag">
                                        ${item.totalPrice.toFixed(2)}
                                    </span>
                                )}
                            </div>
                        ))
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