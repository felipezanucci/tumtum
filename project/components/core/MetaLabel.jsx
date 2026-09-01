import React from "react";
export function MetaLabel({ muted = true, color, children, style, ...rest }) {
  return (
    <span {...rest} style={{
      fontFamily: "var(--font-ui)", fontWeight: "var(--fw-semibold)",
      fontSize: 11, letterSpacing: "var(--ls-meta)", textTransform: "uppercase",
      color: color || (muted ? "var(--text-secondary)" : "currentColor"),
      fontVariantNumeric: "tabular-nums", ...style
    }}>{children}</span>
  );
}
