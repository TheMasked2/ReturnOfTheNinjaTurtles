import type { PaymentDetailsForm } from "./checkoutTypes";
import type { PaymentMethod } from "../../api/paymentApi";
import type { FieldErrors } from "./checkoutValidation";
import { FieldError, RequiredMark, errorInputStyle } from "./CheckoutFormControls";

type PaymentDetailsStepProps = {
  paymentDetails: PaymentDetailsForm;
  paymentMethods: PaymentMethod[];
  selectedPaymentMethodId: number | null;
  errors: FieldErrors;
  showErrors: boolean;
  onPaymentDetailChange: (field: keyof PaymentDetailsForm, value: string) => void;
  onSelectPaymentMethod: (id: number) => void;
};

export default function PaymentDetailsStep({
  paymentDetails,
  paymentMethods,
  selectedPaymentMethodId,
  errors,
  showErrors,
  onPaymentDetailChange,
  onSelectPaymentMethod,
}: PaymentDetailsStepProps) {
  return (
    <>
      <h3>Payment details</h3>
      <p className="text-muted" style={{ marginTop: "-0.5rem", marginBottom: "1rem" }}>
        Your card information is encrypted and never stored in plain text.
      </p>

      <div
        className="form-grid"
        style={{ display: "grid", gridTemplateColumns: "repeat(2, minmax(0, 1fr))", gap: "1rem" }}
      >
        <label style={{ gridColumn: "1 / -1" }}>
          Cardholder name<RequiredMark />
          <input
            type="text"
            value={paymentDetails.nameOnCard}
            onChange={(event) => onPaymentDetailChange("nameOnCard", event.target.value)}
            style={showErrors && errors.nameOnCard ? errorInputStyle : undefined}
          />
          {showErrors && <FieldError message={errors.nameOnCard} />}
        </label>

        <label style={{ gridColumn: "1 / -1" }}>
          Card number<RequiredMark />
          <input
            type="text"
            inputMode="numeric"
            placeholder="1234 5678 9012 3456"
            value={paymentDetails.cardNumber}
            onChange={(event) => onPaymentDetailChange("cardNumber", event.target.value)}
            style={showErrors && errors.cardNumber ? errorInputStyle : undefined}
          />
          {showErrors && <FieldError message={errors.cardNumber} />}
        </label>

        <label>
          Expiry<RequiredMark />
          <input
            type="text"
            placeholder="MM/YY"
            value={paymentDetails.expiry}
            onChange={(event) => onPaymentDetailChange("expiry", event.target.value)}
            style={showErrors && errors.expiry ? errorInputStyle : undefined}
          />
          {showErrors && <FieldError message={errors.expiry} />}
        </label>

        <label>
          CVV<RequiredMark />
          <input
            type="text"
            inputMode="numeric"
            placeholder="123"
            value={paymentDetails.cvv}
            onChange={(event) => onPaymentDetailChange("cvv", event.target.value)}
            style={showErrors && errors.cvv ? errorInputStyle : undefined}
          />
          {showErrors && <FieldError message={errors.cvv} />}
        </label>

        <div style={{ gridColumn: "1 / -1", marginTop: "0.5rem" }}>
          <h4>Saved payment methods</h4>
          {paymentMethods.length === 0 ? (
            <div className="status-message">No saved payment methods. Your new card will be used.</div>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: "0.6rem" }}>
              {paymentMethods.map((method) => {
                const isSelected = selectedPaymentMethodId === method.paymentMethodId;
                return (
                  <label
                    key={method.paymentMethodId}
                    className="radio-row"
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: "0.6rem",
                      padding: "0.75rem 1rem",
                      borderRadius: "0.75rem",
                      border: isSelected ? "1px solid var(--color-primary)" : "1px solid var(--border)",
                      background: isSelected ? "rgba(0, 96, 255, 0.06)" : "#fff",
                      cursor: "pointer",
                    }}
                  >
                    <input
                      type="radio"
                      name="paymentMethod"
                      checked={isSelected}
                      onChange={() => onSelectPaymentMethod(method.paymentMethodId)}
                    />
                    <span>
                      {method.provider} {method.type ? `(${method.type})` : ""}
                    </span>
                  </label>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </>
  );
}
