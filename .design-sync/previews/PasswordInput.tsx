import { PasswordInput } from 'tumtum-frontend'

export const Default = () => (
  <div className="bg-tumtum-black p-6 text-tumtum-white">
  <div className="max-w-sm">
    <PasswordInput placeholder="Sua senha" defaultValue="uma-senha-qualquer" />
  </div>
  </div>
)
