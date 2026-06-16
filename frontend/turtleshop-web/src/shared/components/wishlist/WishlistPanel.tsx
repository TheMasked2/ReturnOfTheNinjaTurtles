import { useEffect, useRef, type RefObject } from "react";
import { PanelItemList } from "../panel/PanelItemList";

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
                    <PanelItemList 
                      items={items} 
                      emptyMessage="Your wishlist is empty." 
                    />
                </div>
            </aside>
        </>
    );
}