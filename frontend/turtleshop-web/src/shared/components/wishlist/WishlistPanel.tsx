import { useEffect, useRef, type RefObject } from "react";

interface WishlistPanelProps {
    isOpen: boolean;
    items: any[];
    onClose: () => void;
    toggleRef: RefObject<HTMLAnchorElement | null>;
}

export function WishlistPanel({ isOpen, items, onClose, toggleRef }: WishlistPanelProps) {
    const panelRef = useRef<HTMLDivElement | null>(null);

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
        return () => document.removeEventListener("mousedown", handleDocumentClick);
    }, [isOpen, onClose, toggleRef]);

    if (!isOpen) return null;

    return (
        <>
            <div className="cart-backdrop" onClick={onClose} />
            <aside className="cart-panel card" ref={panelRef}>
                <div className="cart-panel-header">
                    <h3>Wishlist</h3>
                </div>

                <div className="cart-panel-items">
                    {items.length === 0 ? (
                        <div className="cart-dropdown-empty">wishlist empty</div>
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
            </aside>
        </>
    );
}