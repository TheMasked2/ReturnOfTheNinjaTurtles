import type { CSSProperties } from "react";

export function RequiredMark() {
  return <span style={{ color: "#d92d20" }}> *</span>;
}

export function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return (
    <div style={{ color: "#d92d20", fontSize: "0.8rem", marginTop: "0.3rem" }}>{message}</div>
  );
}

export const errorInputStyle: CSSProperties = { borderColor: "#d92d20" };
