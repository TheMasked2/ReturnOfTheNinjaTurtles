import { baseApi } from "./base-api";

export interface Customer {
  id: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
}

export const customerApi = {
  getCustomer: (customerId: string) => baseApi.get<Customer>(`/customer/${customerId}`),
};