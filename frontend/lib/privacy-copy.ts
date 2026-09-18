/**
 * The privacy page, in both languages.
 *
 * This is the one place the brand goes quiet and careful. Every sentence here
 * is a promise the code has to keep, so the page describes what the product
 * does today — not what it might do — and the type demands both languages so
 * a promise cannot exist in one and silently miss the other.
 *
 * What it must stay true to: the Play Console data-safety form, the Health
 * Connect rationale screen in the Android app (strings.xml, `rationale_*`),
 * and the waitlist line "a gente só usa seu e-mail pra te avisar dos próximos
 * eventos".
 */

export interface PrivacyCopy {
  lang: 'pt-BR' | 'en'
  title: string
  intro: string
  updated: string
  sections: { heading: string; paragraphs: string[]; items?: string[] }[]
  contact: { lead: string; email: string }
  backHome: string
  meta: { title: string; description: string }
}

export const PRIVACY_PT: PrivacyCopy = {
  lang: 'pt-BR',
  title: 'Privacidade',
  intro:
    'Seus dados de batimento são seus. Esta página diz, sem rodeio, o que a TumTum guarda, o que faz com isso e como você apaga tudo.',
  updated: 'Atualizada em 17 de setembro de 2026.',
  sections: [
    {
      heading: 'O que a TumTum guarda',
      paragraphs: ['Só o que a experiência precisa:'],
      items: [
        'Sua conta: e-mail e nome. Na lista de espera do site, só o e-mail.',
        'Seus batimentos: as leituras de frequência cardíaca que você captura com o app durante um evento, ou que importa do seu relógio.',
        'O evento que você escolheu, os momentos que a TumTum encontrou e os cards que você criou.',
        'Quando você compartilha um card, em qual rede foi — para a gente entender o que as pessoas gostam de mostrar.',
      ],
    },
    {
      heading: 'O que a TumTum faz com isso',
      paragraphs: [
        'Desenha a curva da sua noite, encontra os momentos em que seu coração subiu e monta o card. É isso.',
        'A TumTum não é um serviço de saúde. Nada aqui é diagnóstico, alerta ou avaliação médica — é a história de uma noite, contada pelo seu coração.',
      ],
    },
    {
      heading: 'O que a TumTum não faz',
      items: [
        'Não vende seus dados. Nunca.',
        'Não mostra anúncios nem entrega seus dados para quem faz.',
        'Não lê nada do seu relógio fora do intervalo do evento que você escolheu.',
        'Não publica nada sem você apertar o botão.',
      ],
      paragraphs: [],
    },
    {
      heading: 'Seu relógio e o Health Connect',
      paragraphs: [
        'No Android, o app da TumTum pede uma única permissão do Health Connect: ler o batimento cardíaco que seu relógio já gravou. Nada de passos, sono, peso ou qualquer outro dado.',
        'Ele lê só o período do evento que você escolheu, e só depois que você autoriza. A permissão pode ser retirada a qualquer momento nas configurações do Health Connect — e a TumTum para de ler na hora.',
        'Com uma cinta cardíaca, a captura acontece direto no seu celular, por Bluetooth, e só sobe para a TumTum quando você manda.',
      ],
    },
    {
      heading: 'Quem vê',
      paragraphs: [
        'Você. Sua curva, seus momentos e seus cards ficam na sua conta.',
        'Um card só vira público quando você compartilha — e a página dele mostra exatamente o que a imagem mostra: o número, o momento, o evento. Não mostra sua curva inteira nem sua conta.',
        'A equipe da TumTum acessa dados individuais só para resolver um problema que você reportou, ou para verificar um erro técnico.',
      ],
    },
    {
      heading: 'Onde fica e por quanto tempo',
      paragraphs: [
        'Em servidores que a TumTum aluga, com acesso restrito à equipe. Quando algo quebra, um serviço de monitoramento de erros recebe o erro técnico, não os seus batimentos.',
        'Seus dados ficam enquanto sua conta existir, porque a graça é reviver a noite depois. Apagou a conta, apagou tudo.',
      ],
    },
    {
      heading: 'Como apagar',
      paragraphs: [
        'No app: Configurações → Apagar minha conta. Some na hora a conta, os batimentos, os momentos e os cards, no seu celular e nos nossos servidores. Se preferir, escreva para oi@tumtum.cc do e-mail da sua conta e a gente apaga e confirma por e-mail em até 7 dias. Cards que você já postou em outras redes ficam onde você postou — esses são seus para apagar lá.',
        'Dá para retirar a permissão do Health Connect a qualquer momento, sem apagar a conta — a TumTum para de ler seus batimentos na hora. Apagar uma noite sozinha, mantendo a conta, ainda não existe: hoje é tudo ou nada.',
      ],
    },
  ],
  contact: {
    lead: 'Dúvida, pedido ou desconforto com qualquer coisa daqui: fala com a gente.',
    email: 'oi@tumtum.cc',
  },
  backHome: '← Voltar para a TumTum',
  meta: {
    title: 'Privacidade — TumTum',
    description:
      'O que a TumTum guarda dos seus batimentos, o que faz com isso e como você apaga tudo. Curto, porque é sério.',
  },
}

export const PRIVACY_EN: PrivacyCopy = {
  lang: 'en',
  title: 'Privacy',
  intro:
    'Your heartbeat data is yours. This page says plainly what TumTum keeps, what it does with it, and how you delete everything.',
  updated: 'Updated 17 September 2026.',
  sections: [
    {
      heading: 'What TumTum keeps',
      paragraphs: ['Only what the experience needs:'],
      items: [
        'Your account: email and name. On the site waitlist, only the email.',
        'Your heartbeats: the heart-rate readings you capture with the app during an event, or import from your watch.',
        'The event you picked, the moments TumTum found, and the cards you made.',
        'When you share a card, which network it went to — so we understand what people like to show.',
      ],
    },
    {
      heading: 'What TumTum does with it',
      paragraphs: [
        'Draws the curve of your night, finds the moments your heart rose, and builds the card. That is all.',
        'TumTum is not a health service. Nothing here is a diagnosis, an alert or a medical assessment — it is the story of one night, told by your heart.',
      ],
    },
    {
      heading: 'What TumTum does not do',
      items: [
        'Does not sell your data. Ever.',
        'Does not show ads or hand your data to anyone who does.',
        'Does not read anything from your watch outside the window of the event you chose.',
        'Does not publish anything unless you press the button.',
      ],
      paragraphs: [],
    },
    {
      heading: 'Your watch and Health Connect',
      paragraphs: [
        'On Android, the TumTum app asks for a single Health Connect permission: to read the heart rate your watch already recorded. No steps, sleep, weight or any other data.',
        'It reads only the period of the event you chose, and only after you allow it. The permission can be withdrawn at any time in Health Connect settings — and TumTum stops reading at once.',
        'With a chest strap, the capture happens on your phone, over Bluetooth, and only reaches TumTum when you send it.',
      ],
    },
    {
      heading: 'Who sees it',
      paragraphs: [
        'You. Your curve, your moments and your cards stay in your account.',
        'A card becomes public only when you share it — and its page shows exactly what the image shows: the number, the moment, the event. Not your whole curve, not your account.',
        'The TumTum team looks at individual data only to solve a problem you reported, or to check a technical error.',
      ],
    },
    {
      heading: 'Where it lives, and for how long',
      paragraphs: [
        'On servers TumTum rents, with access restricted to the team. When something breaks, an error-monitoring service receives the technical error, not your heartbeats.',
        'Your data stays as long as your account exists, because the point is reliving the night later. Delete the account and everything goes with it.',
      ],
    },
    {
      heading: 'How to delete',
      paragraphs: [
        'In the app: Settings → Delete my account. The account, the heartbeats, the moments and the cards go at once, on your phone and on our servers. If you prefer, write to oi@tumtum.cc from your account email and we delete and confirm by email within 7 days. Cards you already posted elsewhere stay where you posted them — those are yours to delete there.',
        'You can withdraw the Health Connect permission at any time without deleting your account — TumTum stops reading your heartbeats immediately. Deleting a single night while keeping the account does not exist yet: today it is all or nothing.',
      ],
    },
  ],
  contact: {
    lead: 'A question, a request, or anything here that does not sit right: talk to us.',
    email: 'oi@tumtum.cc',
  },
  backHome: '← Back to TumTum',
  meta: {
    title: 'Privacy — TumTum',
    description:
      'What TumTum keeps of your heartbeats, what it does with it, and how you delete everything. Short, because it matters.',
  },
}
