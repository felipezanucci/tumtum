import { Loading } from 'tumtum-frontend'

export const Default = () => (
  <div className="bg-tumtum-black p-6 text-tumtum-white">
  <div className="flex items-center gap-4">
    <Loading />
    <span className="font-body text-tumtum-muted">Procurando seus momentos…</span>
  </div>
  </div>
)
