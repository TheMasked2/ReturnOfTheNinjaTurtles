import { useEffect, useRef, useState, type RefObject } from "react";

interface WishlistPanelProps {
    isOpen: boolean;
    items: any[];
    onClose: () => void;
    toggleRef: RefObject<HTMLAnchorElement | null>;
    onRemoveItem: (wishlistId: number, productId: number) => Promise<void>;
}

export function WishlistPanel({
    isOpen,
    items,
    onClose,
    toggleRef,
    onRemoveItem,
}: WishlistPanelProps) {
    const panelRef = useRef<HTMLDivElement | null>(null);
    const [processingProductId, setProcessingProductId] = useState<number | null>(null);

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

    const handleRemove = async (wishlistId: number, productId: number) => {
        setProcessingProductId(productId);
        try {
            await onRemoveItem(wishlistId, productId);
        } finally {
            setProcessingProductId(null);
        }
    };

    return (
        <>
            <div className="cart-backdrop" onClick={onClose} />
            <aside className="cart-panel card" ref={panelRef}>
                <div className="cart-panel-header">
                    <h3>Wishlist</h3>
                </div>

                <div className="cart-panel-items">
                    {items.length === 0 ? (
                        <div className="cart-dropdown-empty">Your wishlist is empty.</div>
                    ) : (
                        <div
                            className="panel-scroll-container"
                            style={{ maxHeight: "350px", overflowY: "auto", paddingRight: "0.5rem" }}
                        >
                            {items.map((item) => (
                                <div key={item.wishlistItemId ?? `${item.wishlistId}-${item.productId}`} className="cart-dropdown-item">
                                    <div>
                                        <strong>{item.name ?? `Product #${item.productId}`}</strong>
                                    </div>
                                    <div className="cart-dropdown-actions">
                                        <button
                                            type="button"
                                            className="button button-link"
                                            disabled={processingProductId === item.productId}
                                            onClick={() => handleRemove(item.wishlistId, item.productId)}
                                        >
                                            Remove
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </aside>
        </>
    );
}