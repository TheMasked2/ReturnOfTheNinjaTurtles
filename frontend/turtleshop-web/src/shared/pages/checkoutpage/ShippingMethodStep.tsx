import type { ShippingOption } from "./checkoutTypes";

type ShippingMethodStepProps = {
  options: ShippingOption[];
  selectedOption: string;
  onSelect: (id: string) => void;
};

export default function ShippingMethodStep({
  options,
  selectedOption,
  onSelect,
}: ShippingMethodStepProps) {
  return (
    <>
      <h3>Shipping method</h3>
      <p className="text-muted" style={{ marginTop: "-0.5rem", marginBottom: "1rem" }}>
        Choose how fast you'd like your order to arrive.
      </p>

      <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
        {options.map((option) => {
          const isSelected = selectedOption === option.id;
          return (
            <label
              key={option.id}
              className="radio-row"
              style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                gap: "0.75rem",
                padding: "1rem",
                borderRadius: "0.85rem",
                border: isSelected ? "1px solid var(--color-primary)" : "1px solid var(--border)",
                background: isSelected ? "rgba(0, 96, 255, 0.06)" : "#fff",
                cursor: "pointer",
              }}
            >
              <span style={{ display: "flex", alignItems: "center", gap: "0.6rem" }}>
                <input
                  type="radio"
                  name="shippingMethod"
                  value={option.id}
                  checked={isSelected}
                  onChange={() => onSelect(option.id)}
                />
                {option.label}
              </span>
              <strong>${option.price.toFixed(2)}</strong>
            </label>
          );
        })}
      </div>
    </>
  );
}
