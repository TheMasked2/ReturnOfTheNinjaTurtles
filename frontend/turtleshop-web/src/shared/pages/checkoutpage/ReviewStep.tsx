import type { CSSProperties } from "react";
import type { AddressForm, PaymentDetailsForm } from "./checkoutTypes";
import type { PaymentMethod } from "../../api/paymentApi";

type ReviewStepProps = {
  subtotal: number;
  shippingCost: number;
  total: number;
  billingInfo: AddressForm;
  shippingInfo: AddressForm;
  shippingSameAsBilling: boolean;
  paymentDetails: PaymentDetailsForm;
  selectedPaymentMethod?: PaymentMethod;
  onEditStep: (step: number) => void;
};

const cardStyle: CSSProperties = {
  padding: "1rem",
  borderRadius: "0.85rem",
  border: "1px solid var(--border)",
};

const headerRowStyle: CSSProperties = {
  display: "flex",
  justifyContent: "space-between",
  alignItems: "baseline",
};

const editButtonStyle: CSSProperties = {
  padding: "0.25rem 0.6rem",
  fontSize: "0.8rem",
};

export default function ReviewStep({
  subtotal,
  shippingCost,
  total,
  billingInfo,
  shippingInfo,
  shippingSameAsBilling,
  paymentDetails,
  selectedPaymentMethod,
  onEditStep,
}: ReviewStepProps) {
  const destination = shippingSameAsBilling ? billingInfo : shippingInfo;
  const cardDigits = paymentDetails.cardNumber.replace(/\s+/g, "");

  return (
    <>
      <h3>Review & pay</h3>

      <div
        className="review-block"
        style={{ ...cardStyle, display: "flex", flexDirection: "column", gap: "0.5rem", background: "#fafafa" }}
      >
        <div className="summary-line">
          <span>Order subtotal</span>
          <span>${subtotal.toFixed(2)}</span>
        </div>
        <div className="summary-line">
          <span>Shipping</span>
          <span>${shippingCost.toFixed(2)}</span>
        </div>
        <div
          className="summary-line summary-total"
          style={{ paddingTop: "0.5rem", borderTop: "1px solid var(--border)" }}
        >
          <span>Total</span>
          <strong>${total.toFixed(2)}</strong>
        </div>
      </div>

      <div style={{ ...cardStyle, marginTop: "1.25rem" }}>
        <div style={headerRowStyle}>
          <strong>Shipping to</strong>
          <button
            type="button"
            className="button button-secondary"
            style={editButtonStyle}
            onClick={() => onEditStep(1)}
          >
            Edit
          </button>
        </div>
        <div style={{ marginTop: "0.5rem" }}>{destination.fullName}</div>
        <div>{destination.addressLine1}</div>
        {destination.addressLine2 && <div>{destination.addressLine2}</div>}
        <div>
          {destination.postalCode} {destination.city}, {destination.state}
        </div>
        <div>{destination.country}</div>
      </div>

      <div style={{ ...cardStyle, marginTop: "1rem" }}>
        <div style={headerRowStyle}>
          <strong>Payment</strong>
          <button
            type="button"
            className="button button-secondary"
            style={editButtonStyle}
            onClick={() => onEditStep(2)}
          >
            Edit
          </button>
        </div>
        <div style={{ marginTop: "0.5rem" }}>
          {selectedPaymentMethod
            ? `${selectedPaymentMethod.provider}${
                selectedPaymentMethod.type ? ` (${selectedPaymentMethod.type})` : ""
              }`
            : "Card"}
        </div>
        <div>{paymentDetails.nameOnCard}</div>
        <div>{"**** **** **** " + cardDigits.slice(-4)}</div>
      </div>
    </>
  );
}
