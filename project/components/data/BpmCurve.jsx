import React from "react";
export function BpmCurve({ surface = "dark", peak = 187, gap = true, cohort = false, height = 96, width = "100%", style, ...rest }) {
  const W = 1000, H = 260, lo = 52, hi = Math.max(peak + 8, 195);
  const y = v => H - 14 - ((v - lo) / (hi - lo)) * (H - 34);
  const val = t => t > 0.93
    ? 108 + Math.pow((t - 0.93) / 0.07, 2) * (peak - 108)
    : 84 + 26 * t + 13 * Math.sin(t * 21.7) + 7 * Math.sin(t * 6.1 + 1.2) + 5 * Math.sin(t * 47);
  const cval = t => t > 0.93 ? 100 + Math.pow((t - 0.93) / 0.07, 2) * 48
    : 82 + 20 * t + 9 * Math.sin(t * 17.3 + 2) + 5 * Math.sin(t * 5.1);
  const seg = (f, t0, t1) => {
    let d = ""; const n = Math.max(2, Math.round((t1 - t0) * 150));
    for (let i = 0; i <= n; i++) {
      const t = t0 + (t1 - t0) * (i / n);
      d += (i ? " L" : "M") + (10 + t * 968).toFixed(1) + " " + y(f(t)).toFixed(1);
    }
    return d;
  };
  const line = surface === "dark" ? "var(--data-line)" : "var(--tt-black)";
  const marker = surface === "dark" ? "var(--data-marker)" : "var(--tt-pink)";
  const paths = gap ? [seg(val, 0, 0.26), seg(val, 0.39, 1)] : [seg(val, 0, 1)];
  return (
    <svg {...rest} viewBox="0 0 1000 260" style={{ width, height, display: "block", overflow: "visible", ...style }}>
      {cohort ? <path d={seg(cval, 0, 1)} fill="none" stroke="var(--data-secondary)" strokeWidth="5" strokeLinecap="round" strokeLinejoin="round" /> : null}
      {paths.map((d, i) => <path key={i} d={d} fill="none" stroke={line} strokeWidth="6" strokeLinecap="round" strokeLinejoin="round" />)}
      {gap ? <line x1={10 + 0.26 * 968} y1="248" x2={10 + 0.39 * 968} y2="248" stroke="var(--data-gap)" strokeWidth="2" strokeDasharray="4 7" /> : null}
      <circle cx="978" cy={y(peak)} r="11" fill={marker} />
    </svg>
  );
}
