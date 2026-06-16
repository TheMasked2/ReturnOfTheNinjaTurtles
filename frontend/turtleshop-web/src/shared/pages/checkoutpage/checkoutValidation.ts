import type { AddressForm, PaymentDetailsForm } from "./checkoutTypes";

export type FieldErrors = Record<string, string>;

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const EXPIRY_PATTERN = /^(0[1-9]|1[0-2])\/\d{2}$/;

function requireField(value: string, message: string, errors: FieldErrors, field: string) {
  if (!value || !value.trim()) {
    errors[field] = message;
  }
}

export function validateAddress(
  form: AddressForm,
  options: { requireContact?: boolean } = {}
): FieldErrors {
  const errors: FieldErrors = {};

  requireField(form.fullName, "Full name is required.", errors, "fullName");

  if (options.requireContact) {
    requireField(form.email, "Email is required.", errors, "email");
    if (!errors.email && !EMAIL_PATTERN.test(form.email.trim())) {
      errors.email = "Enter a valid email address.";
    }
  }

  requireField(form.addressLine1, "Address is required.", errors, "addressLine1");
  requireField(form.city, "City is required.", errors, "city");
  requireField(form.postalCode, "Postal code is required.", errors, "postalCode");
  requireField(form.country, "Country is required.", errors, "country");

  return errors;
}

function luhnCheck(digits: string): boolean {
  let sum = 0;
  let shouldDouble = false;

  for (let i = digits.length - 1; i >= 0; i -= 1) {
    let digit = parseInt(digits[i], 10);
    if (shouldDouble) {
      digit *= 2;
      if (digit > 9) digit -= 9;
    }
    sum += digit;
    shouldDouble = !shouldDouble;
  }

  return sum % 10 === 0;
}

export function validatePaymentDetails(form: PaymentDetailsForm): FieldErrors {
  const errors: FieldErrors = {};

  requireField(form.nameOnCard, "Cardholder name is required.", errors, "nameOnCard");

  const cardDigits = form.cardNumber.replace(/\s+/g, "");
  if (!cardDigits) {
    errors.cardNumber = "Card number is required.";
  } else if (!/^\d{13,19}$/.test(cardDigits)) {
    errors.cardNumber = "Card number must be 13-19 digits.";
  } else if (!luhnCheck(cardDigits)) {
    errors.cardNumber = "Card number looks invalid.";
  }

  const expiry = form.expiry.trim();
  if (!expiry) {
    errors.expiry = "Expiry date is required.";
  } else if (!EXPIRY_PATTERN.test(expiry)) {
    errors.expiry = "Use MM/YY format.";
  } else {
    const [monthStr, yearStr] = expiry.split("/");
    const month = parseInt(monthStr, 10);
    const year = parseInt(yearStr, 10) + 2000;
    const firstInvalidDate = new Date(year, month); // month after the card's expiry month
    if (firstInvalidDate.getTime() <= Date.now()) {
      errors.expiry = "Card has expired.";
    }
  }

  const cvv = form.cvv.trim();
  if (!cvv) {
    errors.cvv = "CVV is required.";
  } else if (!/^\d{3,4}$/.test(cvv)) {
    errors.cvv = "CVV must be 3-4 digits.";
  }

  return errors;
}

export function hasErrors(errors: FieldErrors): boolean {
  return Object.keys(errors).length > 0;
}
