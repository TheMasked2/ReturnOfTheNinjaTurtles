type RefreshListener = () => void;
const listeners = new Set<RefreshListener>();

export const subscribeHeaderRefresh = (listener: RefreshListener) => {
  listeners.add(listener);
  return () => listeners.delete(listener);
};

export const publishHeaderRefresh = () => {
  listeners.forEach((listener) => listener());
};