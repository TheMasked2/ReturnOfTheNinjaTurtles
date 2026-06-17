import type { AddressForm } from "./checkoutTypes";
import type { FieldErrors } from "./checkoutValidation";
import { FieldError, RequiredMark, errorInputStyle } from "./CheckoutFormControls";

type ShippingBillingStepProps = {
  billingInfo: AddressForm;
  shippingInfo: AddressForm;
  shippingSameAsBilling: boolean;
  billingErrors: FieldErrors;
  shippingErrors: FieldErrors;
  showErrors: boolean;
  onBillingChange: (field: keyof AddressForm, value: string) => void;
  onShippingChange: (field: keyof AddressForm, value: string) => void;
  onToggleSameAsBilling: (value: boolean) => void;
};

const gridStyle = {
  display: "grid",
  gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
  gap: "1rem",
} as const;

export default function ShippingBillingStep({
  billingInfo,
  shippingInfo,
  shippingSameAsBilling,
  billingErrors,
  shippingErrors,
  showErrors,
  onBillingChange,
  onShippingChange,
  onToggleSameAsBilling,
}: ShippingBillingStepProps) {
  return (
    <>
      <h3>Billing details</h3>
      <p className="text-muted" style={{ marginTop: "-0.5rem", marginBottom: "1rem" }}>
        We'll use this for your invoice and to keep you updated on your order.
      </p>

      <div className="form-grid" style={gridStyle}>
        <label style={{ gridColumn: "1 / -1" }}>
          Full name<RequiredMark />
          <input
            type="text"
            value={billingInfo.fullName}
            onChange={(event) => onBillingChange("fullName", event.target.value)}
            style={showErrors && billingErrors.fullName ? errorInputStyle : undefined}
          />
          {showErrors && <FieldError message={billingErrors.fullName} />}
        </label>

        <label>
          Email<RequiredMark />
          <input
            type="email"
            value={billingInfo.email}
            onChange={(event) => onBillingChange("email", event.target.value)}
            style={showErrors && billingErrors.email ? errorInputStyle : undefined}
          />
          {showErrors && <FieldError message={billingErrors.email} />}
        </label>

        <label>
          Phone
          <input
            type="tel"
            value={billingInfo.phone}
            onChange={(event) => onBillingChange("phone", event.target.value)}
          />
        </label>

        <label style={{ gridColumn: "1 / -1" }}>
          Address line 1<RequiredMark />
          <input
            type="text"
            value={billingInfo.addressLine1}
            onChange={(event) => onBillingChange("addressLine1", event.target.value)}
            style={showErrors && billingErrors.addressLine1 ? errorInputStyle : undefined}
          />
          {showErrors && <FieldError message={billingErrors.addressLine1} />}
        </label>

        <label style={{ gridColumn: "1 / -1" }}>
          Address line 2
          <input
            type="text"
            value={billingInfo.addressLine2}
            onChange={(event) => onBillingChange("addressLine2", event.target.value)}
          />
        </label>

        <label>
          City<RequiredMark />
          <input
            type="text"
            value={billingInfo.city}
            onChange={(event) => onBillingChange("city", event.target.value)}
            style={showErrors && billingErrors.city ? errorInputStyle : undefined}
          />
          {showErrors && <FieldError message={billingErrors.city} />}
        </label>

        <label>
          State / province
          <input
            type="text"
            value={billingInfo.state}
            onChange={(event) => onBillingChange("state", event.target.value)}
          />
        </label>

        <label>
          Postal code<RequiredMark />
          <input
            type="text"
            value={billingInfo.postalCode}
            onChange={(event) => onBillingChange("postalCode", event.target.value)}
            style={showErrors && billingErrors.postalCode ? errorInputStyle : undefined}
          />
          {showErrors && <FieldError message={billingErrors.postalCode} />}
        </label>

        <label>
          Country<RequiredMark />
          <input
            type="text"
            value={billingInfo.country}
            onChange={(event) => onBillingChange("country", event.target.value)}
            style={showErrors && billingErrors.country ? errorInputStyle : undefined}
          />
          {showErrors && <FieldError message={billingErrors.country} />}
        </label>
      </div>

      <div className="checkbox-row" style={{ marginTop: "1.25rem" }}>
        <label>
          <input
            type="checkbox"
            checked={shippingSameAsBilling}
            onChange={(event) => onToggleSameAsBilling(event.target.checked)}
          />
          Ship to the same address as billing
        </label>
      </div>

      {!shippingSameAsBilling && (
        <>
          <h3 style={{ marginTop: "1.5rem" }}>Shipping address</h3>
          <div className="form-grid" style={gridStyle}>
            <label style={{ gridColumn: "1 / -1" }}>
              Full name<RequiredMark />
              <input
                type="text"
                value={shippingInfo.fullName}
                onChange={(event) => onShippingChange("fullName", event.target.value)}
                style={showErrors && shippingErrors.fullName ? errorInputStyle : undefined}
              />
              {showErrors && <FieldError message={shippingErrors.fullName} />}
            </label>

            <label style={{ gridColumn: "1 / -1" }}>
              Address line 1<RequiredMark />
              <input
                type="text"
                value={shippingInfo.addressLine1}
                onChange={(event) => onShippingChange("addressLine1", event.target.value)}
                style={showErrors && shippingErrors.addressLine1 ? errorInputStyle : undefined}
              />
              {showErrors && <FieldError message={shippingErrors.addressLine1} />}
            </label>

            <label style={{ gridColumn: "1 / -1" }}>
              Address line 2
              <input
                type="text"
                value={shippingInfo.addressLine2}
                onChange={(event) => onShippingChange("addressLine2", event.target.value)}
              />
            </label>

            <label>
              City<RequiredMark />
              <input
                type="text"
                value={shippingInfo.city}
                onChange={(event) => onShippingChange("city", event.target.value)}
                style={showErrors && shippingErrors.city ? errorInputStyle : undefined}
              />
              {showErrors && <FieldError message={shippingErrors.city} />}
            </label>

            <label>
              State / province
              <input
                type="text"
                value={shippingInfo.state}
                onChange={(event) => onShippingChange("state", event.target.value)}
              />
            </label>

            <label>
              Postal code<RequiredMark />
              <input
                type="text"
                value={shippingInfo.postalCode}
                onChange={(event) => onShippingChange("postalCode", event.target.value)}
                style={showErrors && shippingErrors.postalCode ? errorInputStyle : undefined}
              />
              {showErrors && <FieldError message={shippingErrors.postalCode} />}
            </label>

            <label>
              Country<RequiredMark />
              <input
                type="text"
                value={shippingInfo.country}
                onChange={(event) => onShippingChange("country", event.target.value)}
                style={showErrors && shippingErrors.country ? errorInputStyle : undefined}
              />
              {showErrors && <FieldError message={shippingErrors.country} />}
            </label>
          </div>
        </>
      )}
    </>
  );
}
