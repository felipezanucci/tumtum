import { Input } from 'tumtum-frontend'

export const Default = () => (
  <div className="bg-tumtum-black p-6 text-tumtum-white">
  <div className="max-w-sm space-y-4">
    <Input placeholder="seu@email.com" defaultValue="felipe@tumtum.cc" />
    <Input placeholder="Nome do evento" />
  </div>
  </div>
)
