import React from "react";
import { Button } from "../../components/core/Button.jsx";
import { Chip } from "../../components/core/Chip.jsx";
import { MetaLabel } from "../../components/core/MetaLabel.jsx";
export function LiveScreen({ onEnd }) {
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--tt-black)", padding: "22px 26px 24px", color: "var(--tt-white)" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 9 }}>
        <span style={{ width: 9, height: 9, borderRadius: "50%", background: "var(--tt-yellow)" }}></span>
        <MetaLabel color="var(--tt-yellow)">AO VIVO</MetaLabel>
        <span style={{ flex: 1 }}></span>
        <MetaLabel>POLAR H10 · 98%</MetaLabel>
      </div>
      <div style={{ height: 30 }}></div>
      <div style={{ fontSize: 16, fontWeight: 600 }}>Lollapalooza — Dia 2</div>
      <div style={{ fontSize: 13, color: "var(--text-secondary)", marginTop: 3 }}>Autódromo de Interlagos · começou 17h20</div>
      <div style={{ height: 38 }}></div>
      <MetaLabel>TOCANDO HÁ</MetaLabel>
      <div style={{ fontSize: 54, fontWeight: 700, letterSpacing: "-.04em", lineHeight: 1, fontVariantNumeric: "tabular-nums", marginTop: 8 }}>06:27:44</div>
      <div style={{ height: 40 }}></div>
      <div style={{ display: "flex", alignItems: "flex-end", gap: 12 }}>
        <span style={{ fontSize: 110, fontWeight: 700, color: "var(--tt-pink)", letterSpacing: "-.05em", lineHeight: .8, fontVariantNumeric: "tabular-nums" }}>142</span>
        <span style={{ fontSize: 15, color: "var(--text-secondary)", paddingBottom: 8 }}>bpm agora</span>
      </div>
      <div style={{ height: 22 }}></div>
      <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
        <Chip variant="flash">27 momentos</Chip>
        <Chip variant="outline" style={{ color: "var(--tt-ink-200)" }}>Maior até agora 187</Chip>
      </div>
      <div style={{ flex: 1 }}></div>
      <div style={{ fontSize: 13, color: "var(--text-muted)", marginBottom: 14 }}>A gente só olha depois. Aproveita o show.</div>
      <Button variant="flash" size="lg" full onClick={onEnd}>Encerrar a noite</Button>
    </div>
  );
}
