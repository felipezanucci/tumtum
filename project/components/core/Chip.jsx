import React from "react";
export function Chip({ variant = "flash", children, style, ...rest }) {
  const V = {
    flash:   { background: "var(--tt-yellow)", color: "var(--tt-black)" },
    pink:    { background: "var(--tt-pink)", color: "var(--tt-black)" },
    dark:    { background: "var(--tt-black)", color: "var(--tt-yellow)" },
    outline: { background: "transparent", color: "currentColor", border: "1px solid currentColor", opacity: 0.75 },
    scrim:   { background: "rgba(0,0,0,.6)", color: "var(--tt-white)" }
  }[variant] || {};
  return (
    <span {...rest} style={{
      display: "inline-block", fontFamily: "var(--font-ui)",
      fontWeight: "var(--fw-semibold)", fontSize: 11, letterSpacing: ".08em",
      textTransform: "uppercase", padding: "5px 9px", borderRadius: 0,
      fontVariantNumeric: "tabular-nums", ...V, ...style
    }}>{children}</span>
  );
}
