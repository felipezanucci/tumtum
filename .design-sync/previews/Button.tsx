import { Button } from 'tumtum-frontend'

export const Variants = () => (
  <div className="bg-tumtum-black p-6 text-tumtum-white">
  <div className="flex flex-wrap items-center gap-3">
    <Button variant="primary">Compartilhar</Button>
    <Button variant="secondary">Ver a noite</Button>
    <Button variant="ghost">Agora não</Button>
    <Button variant="danger">Apagar captura</Button>
  </div>
  </div>
)

export const Sizes = () => (
  <div className="bg-tumtum-black p-6 text-tumtum-white">
  <div className="flex flex-wrap items-center gap-3">
    <Button size="sm">Pequeno</Button>
    <Button size="md">Médio</Button>
    <Button size="lg">Grande</Button>
  </div>
  </div>
)

export const States = () => (
  <div className="bg-tumtum-black p-6 text-tumtum-white">
  <div className="flex flex-wrap items-center gap-3">
    <Button loading>Procurando seus momentos</Button>
    <Button disabled>Indisponível</Button>
  </div>
  </div>
)
