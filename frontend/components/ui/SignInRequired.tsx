import Link from 'next/link'

import Button from './Button'
import Card from './Card'

/**
 * What to show when the API answered 401.
 *
 * The bare version of this was a red box reading "Sua sessão expirou. Entre na
 * sua conta para continuar." — an instruction with nothing to act on. The
 * person is told to sign in on a page that offers no way to sign in, so the
 * only way forward is to know the URL. That is the same dead end as a control
 * with no feedback, only politer.
 *
 * It also says less than it seems: a 401 means the request carried no valid
 * token, which is *usually* an expired session but is equally "this browser
 * never signed in" — opening the app on a laptop after using it on a phone.
 * Claiming the session expired in that case is the app being confidently wrong
 * about its own state, so the wording here covers both without guessing.
 */
export function SignInRequired({ what }: { what?: string }) {
  return (
    <Card className="mt-6">
      <Card.Header>
        <Card.Title>Entre na sua conta para ver {what ?? 'esta página'}</Card.Title>
      </Card.Header>
      <Card.Content>
        <p>
          Este navegador não tem uma sessão ativa — ou ela venceu, ou você ainda
          não entrou por aqui. Acontece bastante ao abrir o app no computador
          depois de usar no celular.
        </p>
      </Card.Content>
      <Link href="/login">
        <Button className="mt-4 w-full">Entrar na minha conta</Button>
      </Link>
    </Card>
  )
}
