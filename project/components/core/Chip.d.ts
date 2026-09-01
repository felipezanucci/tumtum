export interface ChipProps {
  /** flash = amarelo (marcador/dado) · pink = rosa (assinatura) · dark = preto c/ texto amarelo · outline = borda na cor corrente · scrim = sobre foto/vídeo */
  variant?: "flash" | "pink" | "dark" | "outline" | "scrim";
  children?: React.ReactNode;
  style?: React.CSSProperties;
}
