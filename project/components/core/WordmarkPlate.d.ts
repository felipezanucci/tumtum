export interface WordmarkPlateProps {
  /** superfície onde o logo vai — decide preto ou branco (únicas formas aprovadas): black→wordmark branco; white/pink/yellow→wordmark preto */
  on?: "black" | "white" | "pink" | "yellow";
  width?: number | string;
  /** caminho até assets/ a partir da página consumidora (ex.: "../../assets") */
  assetsPath?: string;
  style?: React.CSSProperties;
}
