export type CartSummaryItem = {
  cartItemId: number;
  productId: number;
  quantity: number;
  name: string;
  unitPrice: number;
  subtotal: number;
};

export type AddressForm = {
  fullName: string;
  email: string;
  phone: string;
  addressLine1: string;
  addressLine2: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
};

export type PaymentDetailsForm = {
  nameOnCard: string;
  cardNumber: string;
  expiry: string;
  cvv: string;
};

export type ShippingOption = {
  id: string;
  label: string;
  price: number;
};

export const SHIPPING_OPTIONS: ShippingOption[] = [
  { id: "standard", label: "Standard shipping (3-5 days)", price: 4.99 },
  { id: "express", label: "Express shipping (1-2 days)", price: 12.99 },
];

export const EMPTY_ADDRESS: AddressForm = {
  fullName: "",
  email: "",
  phone: "",
  addressLine1: "",
  addressLine2: "",
  city: "",
  state: "",
  postalCode: "",
  country: "",
};

export const CHECKOUT_STEPS = [
  { step: 1, title: "Shipping / billing", description: "Enter billing and shipping details" },
  { step: 2, title: "Payment details", description: "Provide payment information" },
  { step: 3, title: "Shipping method", description: "Choose shipping method" },
  { step: 4, title: "Review & pay", description: "Review totals before payment" },
] as const;
