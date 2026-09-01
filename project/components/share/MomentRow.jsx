import React from "react";
export function MomentRow({ bpm = 187, meta = "às 23h47 · 14s", badge, surface = "dark", style, ...rest }) {
  const fg = surface === "dark" ? "var(--tt-white)" : "var(--tt-black)";
  const sub = surface === "dark" ? "var(--text-secondary)" : "#4A4A4A";
  const line = surface === "dark" ? "var(--border-hairline)" : "#E6E6E6";
  return (
    <div {...rest} style={{ display: "flex", alignItems: "center", gap: 12, padding: "12px 0",
      borderBottom: "1px solid " + line, fontFamily: "var(--font-ui)", ...style }}>
      <span style={{ fontSize: 22, fontWeight: "var(--fw-bold)", color: fg,
        fontVariantNumeric: "tabular-nums", width: 52 }}>{bpm}</span>
      <span style={{ fontSize: 13, color: sub, flex: 1 }}>{meta}</span>
      {badge ? <span style={{ fontSize: 10, fontWeight: "var(--fw-bold)", letterSpacing: ".1em",
        color: "var(--tt-black)", background: "var(--tt-yellow)", padding: "3px 7px",
        textTransform: "uppercase" }}>{badge}</span> : null}
    </div>
  );
}
