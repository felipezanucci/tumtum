import React from "react";
import { Button } from "../../components/core/Button.jsx";
import { MetaLabel } from "../../components/core/MetaLabel.jsx";
import { ShareCard } from "../../components/share/ShareCard.jsx";
export function CardScreen({ skin = "pink", onBack, onPost, posted }) {
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--tt-black)", padding: "22px 26px 24px", color: "var(--tt-white)" }}>
      <div style={{ display: "flex", alignItems: "center" }}>
        <span onClick={onBack} style={{ fontSize: 14, fontWeight: 600, color: "var(--text-secondary)", cursor: "pointer" }}>←</span>
        <span style={{ flex: 1 }}></span>
        <MetaLabel color="var(--tt-yellow)">SEU CARD</MetaLabel>
      </div>
      <div style={{ flex: 1, display: "flex", alignItems: "center", justifyContent: "center", padding: "14px 0" }}>
        <ShareCard skin={skin} width={214} chip="LOLLA — DIA 2 · 23H47" curve={skin === "black"} assetsPath="../../assets" style={{ boxShadow: "0 24px 48px rgba(0,0,0,.5)" }} />
      </div>
      {posted
        ? <div style={{ textAlign: "center", fontSize: 14, fontWeight: 600, color: "var(--tt-yellow)", padding: 18 }}>No feed. A galera já pode sentir também.</div>
        : <Button variant="primary" size="lg" full onClick={onPost}>Postar no feed</Button>}
    </div>
  );
}
