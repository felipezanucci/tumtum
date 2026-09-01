import React from "react";
import { MetaLabel } from "../../components/core/MetaLabel.jsx";
import { ShareCard } from "../../components/share/ShareCard.jsx";
export function ChooseScreen({ onPick }) {
  const skins = ["pink", "black", "yellow", "white"];
  const names = { pink: "A ASSINATURA", black: "A NOITE", yellow: "O GRITO", white: "O LIMPO" };
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--tt-white)", padding: "22px 26px 24px", color: "var(--tt-black)" }}>
      <MetaLabel color="#8A8A8A">ESCOLHA COMO CONTAR</MetaLabel>
      <div style={{ fontSize: 24, fontWeight: 700, letterSpacing: "-.02em", marginTop: 10 }}>Um momento.<br/>Qual pele?</div>
      <div style={{ fontSize: 13, color: "#8A8A8A", marginTop: 6 }}>A forma fica, a pele muda. Toque para escolher.</div>
      <div style={{ height: 20 }}></div>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14, flex: 1, alignContent: "start" }}>
        {skins.map(s => (
          <div key={s} onClick={() => onPick(s)} style={{ cursor: "pointer", display: "flex", flexDirection: "column", gap: 6 }}>
            <ShareCard skin={s} width={150} assetsPath="../../assets" />
            <span style={{ fontSize: 10, fontWeight: 600, letterSpacing: ".1em", color: "#4A4A4A" }}>{names[s]}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
