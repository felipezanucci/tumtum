/**
 * @startingPoint section="Share" subtitle="Card 9:16 nas quatro peles institucionais" viewport="700x460"
 */
export interface ShareCardProps {
  /** a pele da noite — composição idêntica, superfície diferente (Mutante Pop) */
  skin?: "black" | "pink" | "yellow" | "white";
  /** copy-herói; "\n" quebra linha. Use o banco de copy aprovado */
  title?: string;
  value?: number | string;
  meta?: string;
  /** chip amarelo de evento no topo (ex.: "LOLLA — DIA 2 · 23H47") */
  chip?: string;
  /** inclui a curva da noite (formato "Minha noite") */
  curve?: boolean;
  /** largura em px; altura segue 9:16 */
  width?: number;
  /** caminho até assets/ a partir da página consumidora */
  assetsPath?: string;
  style?: React.CSSProperties;
}
