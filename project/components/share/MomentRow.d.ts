export interface MomentRowProps {
  bpm?: number | string;
  /** contexto: hora e duração (ex.: "às 23h47 · 14s") */
  meta?: string;
  /** carimbo amarelo opcional (ex.: "MAIOR") */
  badge?: string;
  surface?: "dark" | "light";
  style?: React.CSSProperties;
}
