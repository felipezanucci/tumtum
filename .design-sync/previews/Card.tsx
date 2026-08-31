import { Card } from 'tumtum-frontend'

export const Default = () => (
  <div className="bg-tumtum-black p-6 text-tumtum-white">
  <div className="max-w-md">
    <Card>
      <p className="font-label text-sm text-tumtum-muted">Realness Festival 2026</p>
      <p className="mt-2 font-hero text-5xl tabular-nums text-tumtum-pink">116</p>
      <p className="mt-1 font-body text-tumtum-muted">às 01h24 · 20 momentos</p>
    </Card>
  </div>
  </div>
)

export const Hoverable = () => (
  <div className="bg-tumtum-black p-6 text-tumtum-white">
  <div className="max-w-md">
    <Card hoverable>
      <p className="font-headline text-tumtum-white">Coldplay — Allianz Parque</p>
      <p className="mt-1 font-body text-tumtum-muted">12/09/2026 · toque para abrir</p>
    </Card>
  </div>
  </div>
)
