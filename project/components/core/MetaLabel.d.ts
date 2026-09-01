export interface MetaLabelProps {
  /** true (padrão) usa cinza secundário; false herda a cor corrente */
  muted?: boolean;
  /** sobrescreve a cor (ex.: var(--tt-yellow) para eyebrow no preto) */
  color?: string;
  children?: React.ReactNode;
  style?: React.CSSProperties;
}
