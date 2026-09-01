import React from "react";
export function Button({ variant = "primary", size = "md", disabled = false, full = false, children, style, ...rest }) {
  const V = {
    primary: { background: "var(--tt-pink)", color: "var(--tt-black)", border: "none" },
    flash:   { background: "var(--tt-yellow)", color: "var(--tt-black)", border: "none" },
    dark:    { background: "var(--tt-black)", color: "var(--tt-white)", border: "none" },
    inverse: { background: "var(--tt-white)", color: "var(--tt-black)", border: "none" },
    quiet:   { background: "transparent", color: "currentColor", border: "1px solid currentColor" }
  }[variant] || {};
  const S = {
    sm: { fontSize: 12, padding: "10px 16px" },
    md: { fontSize: 15, padding: "14px 22px" },
    lg: { fontSize: 16, padding: "18px 28px" }
  }[size] || {};
  return (
    <button {...rest} disabled={disabled} style={{
      fontFamily: "var(--font-ui)", fontWeight: "var(--fw-semibold)",
      borderRadius: "var(--r-2)", cursor: disabled ? "default" : "pointer",
      opacity: disabled ? 0.4 : 1, width: full ? "100%" : undefined,
      textAlign: "center", lineHeight: 1, ...V, ...S, ...style
    }}>{children}</button>
  );
}
