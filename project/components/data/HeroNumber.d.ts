export interface HeroNumberProps {
  value?: number | string;
  /** unidade ao pé do número; "" para omitir */
  unit?: string;
  /** linha de contexto abaixo (ex.: "às 23h47") */
  meta?: string;
  /** REGRA: o número só é rosa no preto; nas demais superfícies é preto */
  surface?: "black" | "white" | "pink" | "yellow";
  /** font-size do número em px */
  size?: number;
  style?: React.CSSProperties;
}
