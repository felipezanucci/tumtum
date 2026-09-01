import React from "react";
import { Button } from "../../components/core/Button.jsx";
import { MetaLabel } from "../../components/core/MetaLabel.jsx";
import { BpmCurve } from "../../components/data/BpmCurve.jsx";
import { MomentRow } from "../../components/share/MomentRow.jsx";
export function RevealScreen({ onShare }) {
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--tt-black)", padding: "22px 26px 24px", color: "var(--tt-white)" }}>
      <MetaLabel color="var(--tt-yellow)">A NOITE</MetaLabel>
      <div style={{ fontSize: 13, fontWeight: 600, marginTop: 10 }}>Lollapalooza — Dia 2 <span style={{ color: "var(--text-muted)", fontWeight: 500 }}>· 22.03.26</span></div>
      <div style={{ height: 18 }}></div>
      <div style={{ fontSize: 21, fontWeight: 700, lineHeight: 1.06 }}>EU TAVA TRANQUILO.<br/>AÍ VEIO ISSO.</div>
      <div style={{ height: 12 }}></div>
      <div style={{ display: "flex", alignItems: "flex-end", gap: 11 }}>
        <span style={{ fontSize: 96, fontWeight: 700, color: "var(--tt-pink)", letterSpacing: "-.055em", lineHeight: .78, fontVariantNumeric: "tabular-nums" }}>187</span>
        <div style={{ paddingBottom: 6, display: "flex", flexDirection: "column" }}>
          <span style={{ fontSize: 15, fontWeight: 600 }}>bpm</span>
          <span style={{ fontSize: 12, color: "var(--text-secondary)" }}>às 23h47</span>
        </div>
      </div>
      <div style={{ height: 14 }}></div>
      <BpmCurve surface="dark" peak={187} gap height={88} />
      <div style={{ display: "flex", justifyContent: "space-between", marginTop: 6, fontSize: 9, fontWeight: 600, color: "var(--text-muted)" }}>
        <span>17H20</span><span style={{ color: "var(--data-gap)" }}>52 MIN SEM DADO</span><span style={{ color: "var(--tt-yellow)" }}>23H47</span>
      </div>
      <div style={{ height: 12 }}></div>
      <div style={{ borderTop: "1px solid var(--border-hairline)" }}>
        <MomentRow bpm={187} meta="às 23h47 · 14s" badge="MAIOR" surface="dark" />
        <MomentRow bpm={171} meta="às 22h58 · 9s" surface="dark" />
        <MomentRow bpm={158} meta="às 21h33 · 18s" surface="dark" style={{ borderBottom: "none" }} />
      </div>
      <div style={{ flex: 1 }}></div>
      <Button variant="primary" size="lg" full onClick={onShare}>Escolher como compartilhar</Button>
    </div>
  );
}
