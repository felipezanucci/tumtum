import React from "react";
export function HeroNumber({ value = 187, unit = "bpm", meta, surface = "black", size = 120, style, ...rest }) {
  const num = surface === "black" ? "var(--tt-pink)" : "var(--tt-black)";
  const fg = surface === "black" ? "var(--tt-white)" : "var(--tt-black)";
  const sub = surface === "black" ? "var(--text-secondary)" : "rgba(0,0,0,.6)";
  return (
    <div {...rest} style={{ fontFamily: "var(--font-ui)", ...style }}>
      <div style={{ display: "flex", alignItems: "flex-end", gap: Math.round(size * .1) }}>
        <span style={{ fontSize: size, fontWeight: "var(--fw-bold)", color: num,
          letterSpacing: "var(--ls-hero)", lineHeight: "var(--lh-hero)",
          fontVariantNumeric: "tabular-nums" }}>{value}</span>
        {unit ? <span style={{ fontSize: Math.max(13, Math.round(size * .14)),
          fontWeight: "var(--fw-semibold)", color: fg, paddingBottom: Math.round(size * .04) }}>{unit}</span> : null}
      </div>
      {meta ? <div style={{ fontSize: Math.max(12, Math.round(size * .12)),
        fontWeight: "var(--fw-medium)", color: sub, marginTop: Math.round(size * .08) }}>{meta}</div> : null}
    </div>
  );
}
