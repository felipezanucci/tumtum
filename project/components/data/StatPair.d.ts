export interface StatPairStat { value: number | string; label: string; }
export interface StatPairProps {
  /** o dado da pessoa — sempre o mais forte visualmente */
  a?: StatPairStat;
  /** o dado de comparação (coletivo) — esmaecido; nunca é ranking entre corpos */
  b?: StatPairStat;
  surface?: "black" | "white" | "pink" | "yellow";
  /** font-size dos números em px */
  size?: number;
  style?: React.CSSProperties;
}
