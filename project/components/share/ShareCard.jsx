import React from "react";
import { BpmCurve } from "../data/BpmCurve.jsx";
import { WordmarkPlate } from "../core/WordmarkPlate.jsx";
export function ShareCard({ skin = "pink", title = "EU TAVA TRANQUILO.\nAÍ VEIO ISSO.", value = 187, meta = "bpm às 23h47", chip, curve = false, width = 248, assetsPath = "assets", style, ...rest }) {
  const S = {
    black:  { bg: "var(--tt-black)",  fg: "var(--tt-white)", num: "var(--tt-pink)" },
    pink:   { bg: "var(--tt-pink)",   fg: "var(--tt-black)", num: "var(--tt-black)" },
    yellow: { bg: "var(--tt-yellow)", fg: "var(--tt-black)", num: "var(--tt-black)" },
    white:  { bg: "var(--tt-white)",  fg: "var(--tt-black)", num: "var(--tt-black)", border: "1px solid #E6E6E6" }
  }[skin];
  const pad = Math.round(width * 0.09);
  const lines = String(title).split("\n");
  return (
    <div {...rest} style={{ width, aspectRatio: "9 / 16", background: S.bg, border: S.border,
      color: S.fg, fontFamily: "var(--font-ui)", display: "flex", flexDirection: "column",
      padding: pad, boxSizing: "border-box", ...style }}>
      {chip ? <span style={{ alignSelf: "flex-start", fontSize: Math.max(8, Math.round(width * .038)),
        fontWeight: "var(--fw-semibold)", letterSpacing: ".14em", textTransform: "uppercase",
        color: "var(--tt-black)", background: "var(--tt-yellow)", padding: "4px 8px" }}>{chip}</span> : null}
      <div style={{ flex: 1 }}></div>
      <div style={{ fontSize: Math.round(width * .062), fontWeight: "var(--fw-bold)", lineHeight: 1.06 }}>
        {lines.map((l, i) => <div key={i}>{l}</div>)}
      </div>
      <div style={{ fontSize: Math.round(width * .36), fontWeight: "var(--fw-bold)", color: S.num,
        letterSpacing: "-.05em", lineHeight: .78, fontVariantNumeric: "tabular-nums",
        marginTop: Math.round(width * .045), marginLeft: -2 }}>{value}</div>
      {curve ? <BpmCurve surface={skin === "black" ? "dark" : "light"} peak={Number(value) || 187}
        height={Math.round(width * .19)} style={{ marginTop: Math.round(width * .05) }} /> : null}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end",
        marginTop: Math.round(width * .04) }}>
        <span style={{ fontSize: Math.max(10, Math.round(width * .048)), fontWeight: "var(--fw-semibold)" }}>{meta}</span>
        <WordmarkPlate on={skin} width={Math.round(width * .24)} assetsPath={assetsPath} />
      </div>
    </div>
  );
}
