import { Badge } from 'tumtum-frontend'

export const Variants = () => (
  <div className="bg-tumtum-black p-6 text-tumtum-white">
  <div className="flex flex-wrap items-center gap-3">
    <Badge>20 momentos</Badge>
    <Badge variant="accent">116 bpm</Badge>
    <Badge variant="success">Qualidade 78%</Badge>
    <Badge variant="warning">Captura incompleta</Badge>
    <Badge variant="danger">Sem leitura</Badge>
  </div>
  </div>
)
