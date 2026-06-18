interface PanelItemListProps {
  items: any[];
  emptyMessage: string;
  maxHeight?: string;
}

export function PanelItemList({ items, emptyMessage, maxHeight = "350px" }: PanelItemListProps) {
  if (items.length === 0) {
    return <div className="cart-dropdown-empty">{emptyMessage}</div>;
  }

  return (
    <div 
      className="panel-scroll-container" 
      style={{ 
        maxHeight, 
        overflowY: "auto",
        paddingRight: "0.5rem" /* Add small padding for scrollbar */
      }}
    >
      {items.map((item, index) => (
        <div key={item.id ?? index} className="cart-dropdown-item">
          <div>
            <strong>{item.productName ?? item.name ?? "Item"}</strong>
            <div className="text-muted">
              {item.quantity ? `Qty: ${item.quantity}` : "Qty: 1"}
            </div>
          </div>
          {item.totalPrice != null && (
            <span className="price-tag">${item.totalPrice.toFixed(2)}</span>
          )}
        </div>
      ))}
    </div>
  );
}