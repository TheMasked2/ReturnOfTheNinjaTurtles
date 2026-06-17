import { CHECKOUT_STEPS } from "./checkoutTypes";

type CheckoutStepperProps = {
  activeStep: number;
  maxUnlockedStep: number;
  onStepSelect: (step: number) => void;
};

export default function CheckoutStepper({
  activeStep,
  maxUnlockedStep,
  onStepSelect,
}: CheckoutStepperProps) {
  return (
    <div
      className="stepper"
      style={{
        display: "grid",
        gridTemplateColumns: "repeat(4, minmax(0, 1fr))",
        gap: "0.75rem",
        alignItems: "start",
        marginBottom: "1.5rem",
      }}
    >
      {CHECKOUT_STEPS.map(({ step, title, description }) => {
        const isActive = activeStep === step;
        const isUnlocked = step <= maxUnlockedStep;
        const isComplete = step < maxUnlockedStep;

        return (
          <button
            key={step}
            type="button"
            disabled={!isUnlocked}
            className={`stepper-step${isActive ? " active" : ""}`}
            onClick={() => onStepSelect(step)}
            style={{
              display: "grid",
              gridTemplateColumns: "auto 1fr",
              gap: "0.75rem",
              alignItems: "center",
              width: "100%",
              minHeight: "72px",
              padding: "1rem",
              borderRadius: "1rem",
              border: isActive ? "1px solid var(--color-primary)" : "1px solid var(--border)",
              background: isActive ? "rgba(0, 96, 255, 0.08)" : "#fff",
              cursor: isUnlocked ? "pointer" : "not-allowed",
              opacity: isUnlocked ? 1 : 0.5,
              textAlign: "left",
            }}
          >
            <span
              style={{
                width: "2.4rem",
                height: "2.4rem",
                borderRadius: "999px",
                border: "1px solid var(--border)",
                background: isActive || isComplete ? "var(--color-primary)" : "transparent",
                color: isActive || isComplete ? "#fff" : "inherit",
                display: "inline-flex",
                alignItems: "center",
                justifyContent: "center",
                fontWeight: 700,
                fontSize: "1rem",
              }}
            >
              {isComplete ? "✓" : step}
            </span>
            <div>
              <strong style={{ display: "block", fontSize: "0.98rem" }}>{title}</strong>
              <div className="text-muted" style={{ fontSize: "0.85rem", marginTop: "0.2rem" }}>
                {description}
              </div>
            </div>
          </button>
        );
      })}
    </div>
  );
}
