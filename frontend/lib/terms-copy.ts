import type { PrivacyCopy } from '@/lib/privacy-copy'

/**
 * The terms of use, in both languages (24/09).
 *
 * Asked for by TikTok's developer portal before Share Kit, and by Meta before
 * an app goes Live. Same rule as the privacy page: every sentence describes
 * what the product does today, in the brand's quiet voice, and both languages
 * are required by the type. A draft written for Felipe's review — it names
 * no company or CNPJ, which he adds once TumTum has one to name.
 */
export const TERMS_PT: PrivacyCopy = {
  lang: 'pt-BR',
  title: 'Termos de uso',
  intro:
    'As regras de usar a TumTum, curtas. Ao criar uma conta ou usar o app, você concorda com elas.',
  updated: 'Atualizados em 24 de setembro de 2026.',
  sections: [
    {
      heading: 'O que a TumTum é',
      paragraphs: [
        'Um app que mostra como seu coração reagiu em shows, jogos e festivais: a curva da sua noite, os momentos em que ele subiu, e um card para compartilhar.',
        'A TumTum é entretenimento, não saúde. Nada aqui é diagnóstico, alerta, avaliação médica ou conselho de saúde. As leituras vêm do seu relógio ou da sua cinta e podem ter erro — não use a TumTum para decidir nada sobre o seu corpo.',
      ],
    },
    {
      heading: 'Sua conta',
      paragraphs: [
        'Você precisa ter 18 anos ou mais para usar a TumTum.',
        'A conta é sua e pessoal: use um e-mail seu e guarde sua senha. O que acontece na sua conta é responsabilidade sua.',
      ],
    },
    {
      heading: 'O que você publica',
      paragraphs: [
        'Mostrar um momento no feed de um evento é escolha sua, feita na hora, e pode ser desfeita quando quiser. Quem vê é quem estava naquele evento; numa turnê, quem foi em qualquer data, se você escolher mostrar para a turnê.',
        'Suas fotos, seus vídeos e seus cards continuam seus. Você dá à TumTum só a permissão necessária para guardar, processar e mostrar o que você decidiu mostrar, para quem você decidiu mostrar.',
        'No feed não vale:',
      ],
      items: [
        'Conteúdo ofensivo, que ataque ou assedie alguém.',
        'Fingir ser outra pessoa, ou mostrar uma noite que não é sua.',
        'Foto ou vídeo de outra pessoa sem a permissão dela, ou que não seja seu para publicar.',
      ],
    },
    {
      heading: 'Denúncia, bloqueio e remoção',
      paragraphs: [
        'Qualquer post pode ser denunciado, e qualquer pessoa pode ser bloqueada — vocês deixam de se ver. Uma pessoa da TumTum lê cada denúncia.',
        'A TumTum pode tirar do ar o que quebrar estas regras, e suspender ou encerrar a conta de quem insistir.',
      ],
    },
    {
      heading: 'Compartilhar em outras redes',
      paragraphs: [
        'Quando você manda um card para Instagram, Facebook, Snapchat, TikTok, WhatsApp ou qualquer outro app, ele passa a seguir as regras daquele app. O que você posta lá fica lá, e é seu para apagar lá.',
      ],
    },
    {
      heading: 'Artistas, clubes e eventos',
      paragraphs: [
        'A TumTum não é ligada aos artistas, clubes, campeonatos, festivais ou organizadores dos eventos que aparecem no app, a não ser quando disser isso claramente. Os nomes aparecem só para dizer onde a sua noite aconteceu.',
      ],
    },
    {
      heading: 'Usar direito',
      items: [
        'Não tente acessar contas ou dados de outras pessoas.',
        'Não tente burlar, copiar ou desmontar o app, nem usá-lo com robôs ou em massa.',
        'Não envie batimentos ou noites inventados.',
      ],
      paragraphs: [],
    },
    {
      heading: 'O que a TumTum não garante',
      paragraphs: [
        'A TumTum está começando. O app pode mudar, ficar fora do ar ou perder funções, e as medições podem falhar ou ter buracos. A gente faz o possível para que isso não aconteça, e avisa quando acontecer.',
        'Nos limites da lei, a TumTum não responde por perdas causadas por uso do app para fins que ele não tem, como decisões de saúde. Nada aqui tira os direitos que o Código de Defesa do Consumidor te dá.',
      ],
    },
    {
      heading: 'Sair',
      paragraphs: [
        'Você pode apagar sua conta quando quiser, em Configurações → Apagar minha conta. O que acontece com seus dados está na Política de privacidade.',
      ],
    },
    {
      heading: 'Mudanças e lei',
      paragraphs: [
        'Se estes termos mudarem de um jeito que importa, a gente avisa no app ou por e-mail antes de valer.',
        'Estes termos seguem a lei brasileira. Vale o foro de São Paulo, SP, salvo quando a lei te garantir o do seu domicílio.',
      ],
    },
  ],
  contact: {
    lead: 'Dúvida sobre qualquer coisa daqui: fala com a gente.',
    email: 'oi@tumtum.cc',
  },
  backHome: '← Voltar para a TumTum',
  meta: {
    title: 'Termos de uso — TumTum',
    description: 'As regras de usar a TumTum: o que ela é, o que você publica e o que não vale. Curtas.',
  },
}

export const TERMS_EN: PrivacyCopy = {
  lang: 'en',
  title: 'Terms of use',
  intro:
    'The rules for using TumTum, kept short. By creating an account or using the app, you agree to them.',
  updated: 'Updated 24 September 2026.',
  sections: [
    {
      heading: 'What TumTum is',
      paragraphs: [
        'An app that shows how your heart reacted at concerts, matches and festivals: the curve of your night, the moments it rose, and a card to share.',
        'TumTum is entertainment, not health. Nothing here is a diagnosis, an alert, a medical assessment or health advice. Readings come from your watch or strap and can be wrong — do not use TumTum to decide anything about your body.',
      ],
    },
    {
      heading: 'Your account',
      paragraphs: [
        'You must be 18 or older to use TumTum.',
        'Your account is yours and personal: use your own email and keep your password safe. What happens in your account is your responsibility.',
      ],
    },
    {
      heading: 'What you post',
      paragraphs: [
        'Showing a moment in an event feed is your choice, made at that moment, and can be undone whenever you like. It is seen by the people who were at that event; on a tour, by anyone at any date, if you choose to show it to the tour.',
        'Your photos, videos and cards stay yours. You give TumTum only the permission it needs to store, process and show what you decided to show, to whom you decided to show it.',
        'Not allowed in the feed:',
      ],
      items: [
        'Offensive content, or anything that attacks or harasses someone.',
        'Pretending to be someone else, or showing a night that is not yours.',
        'Someone else’s photo or video without their permission, or anything that is not yours to post.',
      ],
    },
    {
      heading: 'Reports, blocks and removal',
      paragraphs: [
        'Any post can be reported, and anyone can be blocked — you stop seeing each other. A person at TumTum reads every report.',
        'TumTum may take down what breaks these rules, and suspend or close the account of anyone who keeps doing it.',
      ],
    },
    {
      heading: 'Sharing to other networks',
      paragraphs: [
        'When you send a card to Instagram, Facebook, Snapchat, TikTok, WhatsApp or any other app, it follows that app’s rules. What you post there stays there, and is yours to delete there.',
      ],
    },
    {
      heading: 'Artists, clubs and events',
      paragraphs: [
        'TumTum is not affiliated with the artists, clubs, leagues, festivals or organisers of the events in the app, unless it clearly says so. Their names appear only to say where your night happened.',
      ],
    },
    {
      heading: 'Using it properly',
      items: [
        'Do not try to access other people’s accounts or data.',
        'Do not try to bypass, copy or take apart the app, or use it with bots or at scale.',
        'Do not upload made-up heart rates or nights.',
      ],
      paragraphs: [],
    },
    {
      heading: 'What TumTum does not guarantee',
      paragraphs: [
        'TumTum is just starting. The app may change, go down or lose features, and measurements may fail or have gaps. We do our best to prevent that, and we say so when it happens.',
        'To the extent the law allows, TumTum is not liable for losses from using the app for things it is not for, such as health decisions. Nothing here removes the rights consumer law gives you.',
      ],
    },
    {
      heading: 'Leaving',
      paragraphs: [
        'You can delete your account whenever you want, in Settings → Delete my account. What happens to your data is in the Privacy policy.',
      ],
    },
    {
      heading: 'Changes and law',
      paragraphs: [
        'If these terms change in a way that matters, we will tell you in the app or by email before it takes effect.',
        'These terms follow Brazilian law. The courts of São Paulo, Brazil, have jurisdiction, except where the law guarantees you those of your home.',
      ],
    },
  ],
  contact: {
    lead: 'Questions about anything here: talk to us.',
    email: 'oi@tumtum.cc',
  },
  backHome: '← Back to TumTum',
  meta: {
    title: 'Terms of use — TumTum',
    description: 'The rules for using TumTum: what it is, what you post and what is not allowed. Short.',
  },
}
