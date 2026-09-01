export interface BpmCurveProps {
  /** dark: linha rosa + marcador amarelo · light: linha preta + marcador rosa (amarelo some no branco, 1,11:1) */
  surface?: "dark" | "light";
  /** valor do pico — vira a posição do marcador */
  peak?: number;
  /** buraco de captura VISÍVEL (a linha quebra; nunca interpolar) */
  gap?: boolean;
  /** série secundária cinza (a galera) */
  cohort?: boolean;
  height?: number | string;
  width?: number | string;
  style?: React.CSSProperties;
}
