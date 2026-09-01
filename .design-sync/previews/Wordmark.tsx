import { Wordmark } from 'tumtum-frontend'

// Black canvas on purpose: the wordmark ships white, and the manual allows
// only black, white or an approved skin — never a flat coloured one.
export const OnBlack = () => (
  <div className="bg-tumtum-black p-10">
    <Wordmark className="h-8 w-auto text-tumtum-white" />
  </div>
)

export const OnPink = () => (
  <div className="bg-tumtum-pink p-10">
    <Wordmark className="h-8 w-auto text-tumtum-black" />
  </div>
)
