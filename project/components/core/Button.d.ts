/**
 * @startingPoint section="Core" subtitle="Botão TumTum — rosa, amarelo, preto, quiet" viewport="700x220"
 */
export interface ButtonProps {
  /** primary = rosa (CTA líder) · flash = amarelo (rajada) · dark = preto · inverse = branco · quiet = outline na cor do texto atual */
  variant?: "primary" | "flash" | "dark" | "inverse" | "quiet";
  size?: "sm" | "md" | "lg";
  disabled?: boolean;
  /** ocupa 100% da largura (CTA de tela mobile) */
  full?: boolean;
  children?: React.ReactNode;
  onClick?: () => void;
  style?: React.CSSProperties;
}
