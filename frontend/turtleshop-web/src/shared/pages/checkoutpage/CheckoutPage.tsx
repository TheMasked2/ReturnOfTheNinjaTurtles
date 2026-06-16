import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { cartApi } from "../../api/cartApi";
import { customerApi, type Customer } from "../../api/customerApi";
import { paymentApi, type PaymentMethod } from "../../api/paymentApi";
import { productApi, type Product } from "../../api/productApi";
import { checkoutApi } from "../../api/checkoutApi";
import  type { PlaceOrderPayload } from "../../api/checkoutApi";
import { publishHeaderRefresh } from "../../state/refreshBus";

import OrderSummary from "./OrderSummary";
import CheckoutStepper from "./CheckoutStepper";
import ShippingBillingStep from "./ShippingBillingStep";
import PaymentDetailsStep from "./PaymentDetailsStep";
import ShippingMethodStep from "./ShippingMethodStep";
import ReviewStep from "./ReviewStep";
import {
  SHIPPING_OPTIONS,
  EMPTY_ADDRESS,
  type AddressForm,
  type CartSummaryItem,
  type PaymentDetailsForm,
} from "./checkoutTypes";
import { validateAddress, validatePaymentDetails, hasErrors } from "./checkoutValidation";

export default function CheckoutPage() {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  const [activeStep, setActiveStep] = useState(1);
  const [maxUnlockedStep, setMaxUnlockedStep] = useState(1);
  const [stepAttempted, setStepAttempted] = useState<Record<number, boolean>>({});

  const [cartItems, setCartItems] = useState<CartSummaryItem[]>([]);
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [paymentMethods, setPaymentMethods] = useState<PaymentMethod[]>([]);
  const [selectedPaymentMethodId, setSelectedPaymentMethodId] = useState<number | null>(null);
  const [selectedShippingOption, setSelectedShippingOption] = useState(SHIPPING_OPTIONS[0].id);
  const [shippingSameAsBilling, setShippingSameAsBilling] = useState(true);
  const [billingInfo, setBillingInfo] = useState<AddressForm>(EMPTY_ADDRESS);
  const [shippingInfo, setShippingInfo] = useState<AddressForm>(EMPTY_ADDRESS);
  const [paymentDetails, setPaymentDetails] = useState<PaymentDetailsForm>({
    nameOnCard: "",
    cardNumber: "",
    expiry: "",
    cvv: "",
  });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated || !user?.id) {
      setLoading(false);
      return;
    }

    const loadData = async () => {
      setLoading(true);
      setError(null);

      try {
        const [cart, customerResponse, methods, products] = await Promise.all([
          cartApi.getActiveCart(user.id),
          customerApi.getCustomer(user.id),
          paymentApi.listPaymentMethods(),
          productApi.getProducts(),
        ]);

        setCustomer(customerResponse);
        setPaymentMethods(methods);
        setSelectedPaymentMethodId(methods[0]?.paymentMethodId ?? null);

        const customerName = [customerResponse.firstName, customerResponse.lastName]
          .filter(Boolean)
          .join(" ")
          .trim();

        const defaultAddress: AddressForm = {
          fullName: customerName || "",
          email: customerResponse.email ?? "",
          phone: customerResponse.phone ?? "",
          addressLine1: customerResponse.addressLine1 ?? "",
          addressLine2: customerResponse.addressLine2 ?? "",
          city: customerResponse.city ?? "",
          state: customerResponse.state ?? "",
          postalCode: customerResponse.postalCode ?? "",
          country: customerResponse.country ?? "",
        };
        setBillingInfo(defaultAddress);
        setShippingInfo(defaultAddress);

        const mappedItems: CartSummaryItem[] = (cart.items ?? [])
          .map((item: any) => {
            const product = products.find((p: Product) => p.id === item.productId);
            const unitPrice = product?.price ?? item.unitPrice ?? 0;
            return {
              cartItemId: item.cartItemId,
              productId: item.productId,
              quantity: item.quantity ?? 1,
              name: product?.name ?? item.name ?? `Product #${item.productId}`,
              unitPrice,
              subtotal: unitPrice * (item.quantity ?? 1),
            };
          })
          .sort((a: CartSummaryItem, b: CartSummaryItem) => a.cartItemId - b.cartItemId);

        setCartItems(mappedItems);
      } catch (err: any) {
        setError(err.message || "Unable to load checkout data.");
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [isAuthenticated, user?.id]);

  useEffect(() => {
    if (shippingSameAsBilling) {
      setShippingInfo(billingInfo);
    }
  }, [billingInfo, shippingSameAsBilling]);

  const subtotal = useMemo(
    () => cartItems.reduce((sum, item) => sum + item.subtotal, 0),
    [cartItems]
  );

  const shippingCost =
    SHIPPING_OPTIONS.find((option) => option.id === selectedShippingOption)?.price ?? 0;

  const total = subtotal + shippingCost;

  const billingErrors = useMemo(
    () => validateAddress(billingInfo, { requireContact: true }),
    [billingInfo]
  );
  const shippingErrors = useMemo(
    () => (shippingSameAsBilling ? {} : validateAddress(shippingInfo)),
    [shippingInfo, shippingSameAsBilling]
  );
  const step1Valid = !hasErrors(billingErrors) && !hasErrors(shippingErrors);

  const selectedPaymentMethod = paymentMethods.find(
    (method) => method.paymentMethodId === selectedPaymentMethodId
  );

  const requiresCard = selectedPaymentMethod?.type === "Credit Card";

  const paymentErrors = useMemo(
    () => (requiresCard ? validatePaymentDetails(paymentDetails) : {}),
    [paymentDetails, requiresCard]
  );

const step2Valid = requiresCard ? !hasErrors(paymentErrors) : true;

  const handleBillingChange = (field: keyof AddressForm, value: string) => {
    setBillingInfo((prev) => ({ ...prev, [field]: value }));
  };

  const handleShippingChange = (field: keyof AddressForm, value: string) => {
    setShippingInfo((prev) => ({ ...prev, [field]: value }));
  };

  const handlePaymentDetailChange = (field: keyof PaymentDetailsForm, value: string) => {
    setPaymentDetails((prev) => ({ ...prev, [field]: value }));
  };

  const goToStep = (step: number) => {
    if (step <= maxUnlockedStep) {
      setActiveStep(step);
    }
  };

  const handleContinue = () => {
    if (activeStep === 1 && !step1Valid) {
      setStepAttempted((prev) => ({ ...prev, 1: true }));
      return;
    }
    if (activeStep === 2 && !step2Valid) {
      setStepAttempted((prev) => ({ ...prev, 2: true }));
      return;
    }

    const next = activeStep + 1;
    setMaxUnlockedStep((prev) => Math.max(prev, next));
    setActiveStep(next);
  };

  const handleSubmitOrder = async () => {
    if (!user?.id) return;
    if (!step1Valid || !step2Valid) {
      setStepAttempted((prev) => ({ ...prev, 1: true, 2: true }));
      return;
    }
  
    const paymentMethodString =
      selectedPaymentMethod?.provider || selectedPaymentMethod?.type;
  
    if (!paymentMethodString) {
      setError("Please select a payment method.");
      return;
    }
  
    setSubmitting(true);
    setError(null);
  
    try {
      // Format the shipping address into a single string for the payload consisting of: Fullname,country, postal code and housenumber.
      // This will be enough for most GPS systems to find the address.
      const formatAddress = (address: AddressForm) =>
        [
          address.fullName,
          address.addressLine1,
          address.addressLine2,
          `${address.postalCode} ${address.city}`,
          address.state,
          address.country,
        ]
          .filter(Boolean)
          .join("\n");

      const payload: PlaceOrderPayload = {
        paymentMethod: paymentMethodString,
        shippingMethod: selectedShippingOption,
        shippingAddress: formatAddress(shippingSameAsBilling ? billingInfo : shippingInfo),
      };

      const response = await checkoutApi.placeOrder(user.id, payload);
      await checkoutApi.confirmOrder(response.orderId, response.transactionId);
      publishHeaderRefresh();
      navigate(`/checkout/confirmation/${response.orderId}`);
    } catch (err: any) {
      setError(err.message || "Unable to place order.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="page">
        <section className="section">
          <div className="status-message">Loading checkout details...</div>
        </section>
      </div>
    );
  }

  return (
    <div className="page">
      <section className="section">
        <div className="section-heading">
          <div>
            <h2>Checkout</h2>
            <p className="text-muted">
              Complete your order by reviewing cart items, entering your details, and submitting payment.
            </p>
          </div>
        </div>

        {error && <div className="status-message status-error">{error}</div>}

        <div
          className="checkout-layout"
          style={{ display: "grid", gridTemplateColumns: "280px minmax(0, 1fr)", gap: "2rem" }}
        >
          <aside>
            <OrderSummary items={cartItems} subtotal={subtotal} />
          </aside>

          <div className="card" style={{ padding: "1.5rem 1.25rem" }}>
            <CheckoutStepper
              activeStep={activeStep}
              maxUnlockedStep={maxUnlockedStep}
              onStepSelect={goToStep}
            />

            <div className="step-content" style={{ marginTop: "1rem" }}>
              {activeStep === 1 && (
                <ShippingBillingStep
                  billingInfo={billingInfo}
                  shippingInfo={shippingInfo}
                  shippingSameAsBilling={shippingSameAsBilling}
                  billingErrors={billingErrors}
                  shippingErrors={shippingErrors}
                  showErrors={Boolean(stepAttempted[1])}
                  onBillingChange={handleBillingChange}
                  onShippingChange={handleShippingChange}
                  onToggleSameAsBilling={setShippingSameAsBilling}
                />
              )}

              {activeStep === 2 && (
                <PaymentDetailsStep
                  paymentDetails={paymentDetails}
                  paymentMethods={paymentMethods}
                  selectedPaymentMethodId={selectedPaymentMethodId}
                  errors={paymentErrors}
                  showErrors={Boolean(stepAttempted[2])}
                  onPaymentDetailChange={handlePaymentDetailChange}
                  onSelectPaymentMethod={setSelectedPaymentMethodId}
                />
              )}

              {activeStep === 3 && (
                <ShippingMethodStep
                  options={SHIPPING_OPTIONS}
                  selectedOption={selectedShippingOption}
                  onSelect={setSelectedShippingOption}
                />
              )}

              {activeStep === 4 && (
                <ReviewStep
                  subtotal={subtotal}
                  shippingCost={shippingCost}
                  total={total}
                  billingInfo={billingInfo}
                  shippingInfo={shippingInfo}
                  shippingSameAsBilling={shippingSameAsBilling}
                  paymentDetails={paymentDetails}
                  selectedPaymentMethod={selectedPaymentMethod}
                  onEditStep={goToStep}
                />
              )}

              <div className="actions-row" style={{ marginTop: "1.5rem", display: "flex", gap: "0.75rem" }}>
                {activeStep > 1 && (
                  <button
                    type="button"
                    className="button button-secondary"
                    onClick={() => setActiveStep((step) => step - 1)}
                  >
                    Back
                  </button>
                )}
                {activeStep < 4 ? (
                  <button type="button" className="button button-primary" onClick={handleContinue}>
                    Continue
                  </button>
                ) : (
                  <button
                    type="button"
                    className="button button-primary"
                    disabled={submitting || cartItems.length === 0}
                    onClick={handleSubmitOrder}
                  >
                    {submitting ? "Processing…" : "Continue to payment"}
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
