import React from "react";
export function StatPair({ a = { value: 187, label: "VOCÊ" }, b = { value: 172, label: "A TORCIDA" }, surface = "pink", size = 40, style, ...rest }) {
  const fg = surface === "black" ? "var(--tt-white)" : "var(--tt-black)";
  const dim = surface === "black" ? "var(--text-secondary)" : "rgba(0,0,0,.45)";
  const lab = surface === "black" ? "var(--text-secondary)" : "rgba(0,0,0,.6)";
  const cell = (s, color) => (
    <div style={{ display: "flex", flexDirection: "column" }}>
      <span style={{ fontSize: size, fontWeight: "var(--fw-bold)", color,
        letterSpacing: "-.03em", lineHeight: .85, fontVariantNumeric: "tabular-nums" }}>{s.value}</span>
      <span style={{ fontSize: Math.max(9, Math.round(size * .26)), fontWeight: "var(--fw-semibold)",
        letterSpacing: ".08em", color: lab, marginTop: 5, textTransform: "uppercase" }}>{s.label}</span>
    </div>
  );
  return (
    <div {...rest} style={{ display: "flex", gap: Math.round(size * .45), fontFamily: "var(--font-ui)", ...style }}>
      {cell(a, surface === "black" ? "var(--tt-pink)" : fg)}
      {cell(b, dim)}
    </div>
  );
}
