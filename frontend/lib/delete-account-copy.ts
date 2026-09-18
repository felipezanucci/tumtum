/**
 * The account-deletion page, in both languages.
 *
 * This page exists because the Play Console asks for a public URL that does
 * three specific things: name the app or developer, spell out the steps to
 * request deletion, and say which data is deleted, which is kept, and for how
 * long. The privacy page covers deletion in a paragraph; this one answers the
 * form's three bullets in order so a reviewer can tick them off.
 *
 * Every sentence is a promise `DELETE /api/users/me` has to keep.
 */

import type { PrivacyCopy } from '@/lib/privacy-copy'

/** Same shape as the privacy page, because it is rendered by the same component. */
export type DeleteAccountCopy = PrivacyCopy

export const DELETE_ACCOUNT_PT: DeleteAccountCopy = {
  lang: 'pt-BR',
  title: 'Apagar sua conta',
  intro:
    'O TumTum é feito pela TumTum Tecnologia. Esta página diz como apagar sua conta do app TumTum (cc.tumtum.app), o que some e o que fica.',
  updated: 'Atualizada em 18 de setembro de 2026.',
  sections: [
    {
      heading: 'Apagar pelo app',
      paragraphs: ['Três toques, sem pedir para ninguém:'],
      items: [
        'Abra o TumTum e vá na aba VOCÊ.',
        'Toque em Configurações.',
        'Role até o fim e toque em Apagar minha conta, depois em Apagar tudo.',
      ],
    },
    {
      heading: 'Apagar por e-mail',
      paragraphs: [
        'Se você não tem mais o app instalado, escreva para oi@tumtum.cc do mesmo e-mail da sua conta, pedindo para apagar. A gente apaga e confirma por e-mail em até 7 dias.',
      ],
    },
    {
      heading: 'O que é apagado',
      paragraphs: ['Tudo que é seu sai dos nossos servidores na hora, sem cópia guardada:'],
      items: [
        'Sua conta: e-mail e nome.',
        'Seus batimentos: todas as leituras de frequência cardíaca que você capturou ou importou.',
        'Suas noites, os momentos que a TumTum encontrou nelas e os cards que você criou.',
        'O registro de onde você compartilhou cada card.',
        'Tudo que o app guardou no seu celular, incluindo noites que nunca chegaram a subir.',
      ],
    },
    {
      heading: 'O que fica',
      paragraphs: [
        'Os eventos em si — o show, o jogo, a data, a linha do tempo — continuam existindo, porque são de todo mundo que estava lá, não seus. Depois que sua conta some, nada neles aponta para você.',
        'Cards que você já postou no Instagram, no WhatsApp ou em qualquer outro lugar ficam onde você postou. Esses são seus para apagar lá.',
        'Não guardamos nada seu por um período extra depois da exclusão. Não há backup com prazo, não há cópia em espera, não há conta suspensa esperando você mudar de ideia. Apagou, acabou.',
      ],
    },
  ],
  contact: { lead: 'Dúvida sobre isso, ou quer que a gente apague para você:', email: 'oi@tumtum.cc' },
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
  updated: 'Updated 18 September 2026.',
  sections: [
    {
      heading: 'Delete in the app',
      paragraphs: ['Three taps, no need to ask anyone:'],
      items: [
        'Open TumTum and go to the VOCÊ (You) tab.',
        'Tap Configurações (Settings).',
        'Scroll to the bottom, tap Apagar minha conta (Delete my account), then Apagar tudo (Delete everything).',
      ],
    },
    {
      heading: 'Delete by email',
      paragraphs: [
        'If you no longer have the app installed, write to oi@tumtum.cc from the same email as your account, asking us to delete it. We delete it and confirm by email within 7 days.',
      ],
    },
    {
      heading: 'What gets deleted',
      paragraphs: ['Everything that is yours leaves our servers immediately, with no copy kept:'],
      items: [
        'Your account: email and name.',
        'Your heartbeats: every heart-rate reading you captured or imported.',
        'Your nights, the moments TumTum found in them and the cards you created.',
        'The record of where you shared each card.',
        'Everything the app stored on your phone, including nights that never reached the server.',
      ],
    },
    {
      heading: 'What stays',
      paragraphs: [
        'The events themselves — the show, the match, the date, the timeline — keep existing, because they belong to everyone who was there, not to you. Once your account is gone, nothing in them points to you.',
        'Cards you already posted on Instagram, WhatsApp or anywhere else stay where you posted them. Those are yours to delete there.',
        'We keep nothing of yours for an extra period after deletion. There is no backup with a retention window, no copy on hold, no suspended account waiting for you to change your mind. Deleted is deleted.',
      ],
    },
  ],
  contact: { lead: 'Questions about this, or want us to delete it for you:', email: 'oi@tumtum.cc' },
  backHome: 'Back to TumTum',
  meta: {
    title: 'Delete your account — TumTum',
    description: 'How to delete your TumTum account and heart-rate data: what is erased, what stays, and how long it takes.',
  },
}
