/**
 * The account-deletion page, in both languages.
 *
 * This page exists because the Play Console asks for a public URL that does
 * three specific things: name the app or developer, spell out the steps to
 * request deletion, and say which data is deleted, which is kept, and for how
 * long. The privacy page covers deletion in a paragraph; this one answers the
 * form's three bullets in order so a reviewer can tick them off.
 *
 * Every sentence is a promise `POST /api/users/me/delete` has to keep
 * (26/09: it replaced `DELETE /api/users/me`, and asks for the password).
 * Since 26/09 the site has the button too, in Perfil → Privacidade; this
 * page keeps explaining, and points there.
 */

import type { PrivacyCopy } from '@/lib/privacy-copy'

/** Same shape as the privacy page, because it is rendered by the same component. */
export type DeleteAccountCopy = PrivacyCopy

export const DELETE_ACCOUNT_PT: DeleteAccountCopy = {
  lang: 'pt-BR',
  title: 'Apagar sua conta',
  intro:
    'O TumTum é feito pela TumTum Tecnologia. Esta página diz como apagar sua conta do app TumTum (cc.tumtum.app), o que some e o que fica.',
  updated: 'Atualizada em 26 de setembro de 2026.',
  sections: [
    {
      heading: 'Apagar pelo app',
      paragraphs: ['Sem pedir para ninguém:'],
      items: [
        'Abra o TumTum e vá na aba VOCÊ.',
        'Toque em Configurações.',
        'Role até o fim, toque em Apagar minha conta, confirme com sua senha e toque em Apagar tudo.',
      ],
    },
    {
      heading: 'Apagar pelo site',
      paragraphs: ['O site tem o mesmo botão:'],
      items: [
        'Entre na sua conta em tumtum.cc e abra o Perfil.',
        'Na seção Privacidade, toque em Apagar minha conta.',
        'Confirme com sua senha e toque em Apagar tudo, de vez.',
      ],
    },
    {
      heading: 'Apagar só uma parte',
      paragraphs: [
        'Não precisa apagar tudo. Uma noite sozinha sai em "Apagar esta noite" (em Suas sessões no site, ou no app), com as leituras, os momentos, os cards e os posts dela. Um card sai do ar com "Despublicar". E cada consentimento se desliga em Perfil → Privacidade.',
      ],
    },
    {
      heading: 'Apagar por e-mail',
      paragraphs: [
        'Se você não consegue entrar na conta, escreva para oi@tumtum.cc do mesmo e-mail da sua conta, com o assunto "Privacidade", pedindo para apagar. A gente apaga e confirma por e-mail em até 7 dias.',
      ],
    },
    {
      heading: 'O que é apagado',
      paragraphs: ['Tudo que é seu sai dos nossos servidores na hora, sem cópia guardada:'],
      items: [
        'Sua conta: e-mail, nome e data de nascimento.',
        'Seus batimentos: todas as leituras que você capturou ou importou.',
        'Suas noites, os momentos que a TumTum encontrou nelas e os cards que você criou, incluindo a imagem guardada de cada card.',
        'O registro de onde você compartilhou cada card.',
        'Seus posts no feed, suas reações, as denúncias que você fez e seus bloqueios.',
        'Seus consentimentos, seus pedidos ao encarregado e o registro de quem acessou seus dados.',
        'Seu e-mail na lista de espera do site, se estiver lá, e qualquer código de cadastro pendente.',
        'Tudo que o app guardou no seu celular, incluindo noites que nunca chegaram a subir.',
      ],
    },
    {
      heading: 'O que fica',
      paragraphs: [
        'Os eventos em si — o show, o jogo, a data, a linha do tempo — continuam existindo, porque são de todo mundo que estava lá, não seus. Depois que sua conta some, nada neles aponta para você.',
        'Cards que você já postou no Instagram, no WhatsApp ou em qualquer outro lugar ficam onde você postou. Esses são seus para apagar lá.',
        'Fica só um registro de que uma conta foi apagada, com a data e nada que identifique você.',
        'Não guardamos nada seu por um período extra depois da exclusão. Não há cópia em espera nem conta suspensa esperando você mudar de ideia. Apagou, acabou.',
      ],
    },
  ],
  contact: { lead: 'Dúvida sobre isso, ou quer que a gente apague para você:', email: 'oi@tumtum.cc', subject: 'Privacidade' },
  backHome: 'Voltar para a TumTum',
  meta: {
    title: 'Apagar sua conta — TumTum',
    description: 'Como apagar sua conta TumTum e seus dados de batimento: o que some, o que fica, e em quanto tempo.',
  },
}

export const DELETE_ACCOUNT_EN: DeleteAccountCopy = {
  lang: 'en',
  title: 'Delete your account',
  intro:
    'TumTum is made by TumTum Tecnologia. This page explains how to delete your account in the TumTum app (cc.tumtum.app), what is erased and what stays.',
  updated: 'Updated 26 September 2026.',
  sections: [
    {
      heading: 'Delete in the app',
      paragraphs: ['No need to ask anyone:'],
      items: [
        'Open TumTum and go to the VOCÊ (You) tab.',
        'Tap Configurações (Settings).',
        'Scroll to the bottom, tap Apagar minha conta (Delete my account), confirm with your password, then tap Apagar tudo (Delete everything).',
      ],
    },
    {
      heading: 'Delete on the website',
      paragraphs: ['The site has the same button:'],
      items: [
        'Sign in at tumtum.cc and open your Perfil (Profile).',
        'In the Privacidade (Privacy) section, tap Apagar minha conta (Delete my account).',
        'Confirm with your password and tap Apagar tudo, de vez (Delete everything, for good).',
      ],
    },
    {
      heading: 'Delete only part of it',
      paragraphs: [
        'You do not have to delete everything. A single night goes with "Apagar esta noite" (Delete this night), in Suas sessões on the site or in the app, together with its readings, moments, cards and posts. A card comes down with "Despublicar" (Unpublish). And each consent can be switched off under Profile → Privacidade.',
      ],
    },
    {
      heading: 'Delete by email',
      paragraphs: [
        'If you cannot sign in, write to oi@tumtum.cc from the same email as your account, with the subject "Privacidade", asking us to delete it. We delete it and confirm by email within 7 days.',
      ],
    },
    {
      heading: 'What gets deleted',
      paragraphs: ['Everything that is yours leaves our servers immediately, with no copy kept:'],
      items: [
        'Your account: email, name and date of birth.',
        'Your heartbeats: every reading you captured or imported.',
        'Your nights, the moments TumTum found in them and the cards you created, including each card’s stored image.',
        'The record of where you shared each card.',
        'Your feed posts, your reactions, the reports you made and your blocks.',
        'Your consents, your requests to the data protection officer and the log of who accessed your data.',
        'Your email on the site waitlist, if it is there, and any pending sign-up code.',
        'Everything the app stored on your phone, including nights that never reached the server.',
      ],
    },
    {
      heading: 'What stays',
      paragraphs: [
        'The events themselves — the show, the match, the date, the timeline — keep existing, because they belong to everyone who was there, not to you. Once your account is gone, nothing in them points to you.',
        'Cards you already posted on Instagram, WhatsApp or anywhere else stay where you posted them. Those are yours to delete there.',
        'Only a record that an account was deleted remains, with the date and nothing that identifies you.',
        'We keep nothing of yours for an extra period after deletion. There is no copy on hold and no suspended account waiting for you to change your mind. Deleted is deleted.',
      ],
    },
  ],
  contact: { lead: 'Questions about this, or want us to delete it for you:', email: 'oi@tumtum.cc', subject: 'Privacidade' },
  backHome: 'Back to TumTum',
  meta: {
    title: 'Delete your account — TumTum',
    description: 'How to delete your TumTum account and heart-rate data: what is erased, what stays, and how long it takes.',
  },
}
