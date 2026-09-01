/**
 * Every string on the public site, in both languages.
 *
 * The v0.4 design handoff names its reference file the canonical string
 * source, so these are transcribed from it rather than rewritten. Keeping
 * them in one module means the two routes share a single layout and differ
 * only in the object they are handed — a section added to one language can
 * never silently miss the other, because the type demands both.
 *
 * Language is a route (`/` and `/en`), not client state. The handoff's
 * prototype persisted a choice in localStorage and it says so plainly:
 * production should prefer a real route for SEO. A page whose content is
 * swapped by script is a page search engines and link previews only ever see
 * in one language.
 */

export type Lang = 'pt' | 'en'

export interface SiteCopy {
  lang: Lang
  /** Path to the other language, for the switcher. */
  otherHref: string
  nav: { how: string; cards: string; feed: string; gallery: string; signIn: string; cta: string }
  hero: {
    title: string[]
    sub: string
    ctaPrimary: string
    ctaSecondary: string
    card: { event: string; copy: string[]; unit: string }
  }
  proof: { value: string; caption: string; meta: string }[]
  does: { eyebrow: string; title: string[]; body: string; highlight: string; bodyEnd: string }
  how: { eyebrow: string; title: string[]; steps: { n: string; title: string; body: string }[] }
  cards: {
    eyebrow: string
    title: string[]
    intro: string
    disclaimer: string
    items: {
      label: string
      copy: string[]
      value: string
      meta?: string
      you?: string
      crowd?: string
      youLabel?: string
      crowdLabel?: string
    }[]
  }
  feed: {
    eyebrow: string
    title: string
    body: string[]
    button: string
    bodyEnd: string
    stats: { value: string; text: string }[]
  }
  gallery: {
    eyebrow: string
    title: string
    body: string
    stats: { value: string; label: string }[]
    minis: { time: string; value: string; label: string }[]
  }
  waitlist: {
    eyebrow: string
    title: string[]
    body: string
    placeholder: string
    button: string
    privacy: string
    sending: string
    joined: string
    already: string
    joinedBody: string
    alreadyBody: string
    errorEmpty: string
    errorInvalid: string
    errorServer: string
  }
  footer: { privacy: string; line: string; signIn: string }
  meta: { title: string; description: string }
}

export const PT: SiteCopy = {
  lang: 'pt',
  otherHref: '/en',
  nav: {
    how: 'Como funciona',
    cards: 'Os cards',
    feed: 'O feed',
    gallery: 'Galeria',
    signIn: 'Entrar',
    cta: 'Entrar na lista',
  },
  hero: {
    title: ['VOCÊ SENTIU.', 'AGORA TEM', 'PROVA.'],
    sub: 'Os momentos mais marcantes da sua vida têm hora, lugar e batida. A TumTum guarda os três.',
    ctaPrimary: 'Quero no meu próximo evento',
    ctaSecondary: 'Como funciona',
    card: {
      event: 'LOLLAPALOOZA — DIA 2 · 23H47',
      copy: ['EU TAVA TRANQUILO.', 'AÍ VEIO ISSO.'],
      unit: 'bpm — meu maior da noite',
    },
  },
  proof: [
    { value: '176', caption: 'A torcida foi junto.', meta: 'Pênalti · 94:12' },
    { value: '191', caption: 'Quando entrou, meu coração foi junto.', meta: 'Abertura do show · 21:03' },
    { value: '183', caption: 'Ninguém tava tranquilo.', meta: 'O drop · 23:41' },
  ],
  does: {
    eyebrow: 'O QUE A TUMTUM FAZ',
    title: ['Todo mundo filmou o show.', 'Ninguém registrou o que sentiu.'],
    body: 'Você guarda o ingresso, a pulseira, o print da setlist. A TumTum guarda a parte que importa: ',
    highlight: 'o exato momento em que você perdeu a compostura',
    bodyEnd: ' — com hora, música e prova.',
  },
  how: {
    eyebrow: 'COMO FUNCIONA',
    title: ['Você vive a noite.', 'A gente cuida do resto.'],
    steps: [
      {
        n: '01',
        title: 'Conecta o relógio que você já usa',
        body: 'Sem aparelho novo, sem complicação. Seu smartwatch de sempre já registra tudo o que a gente precisa.',
      },
      {
        n: '02',
        title: 'Vai pro evento e esquece da gente',
        body: 'Canta, grita, sofre no pênalti. O relógio fica no pulso fazendo o trabalho dele, como sempre fez.',
      },
      {
        n: '03',
        title: 'Depois, a revelação',
        body: 'A TumTum cruza sua noite com a timeline do evento e te mostra qual momento foi o seu. Aí é só escolher o card e postar.',
      },
    ],
  },
  cards: {
    eyebrow: 'O CARD É A PROVA',
    title: ['Um momento.', 'Vários jeitos de contar.'],
    intro:
      'Cada noite gera cards diferentes do mesmo instante — e cada noite escolhe uma pele. Você decide qual história vai pro feed.',
    disclaimer:
      'Os cards acima são exemplos ilustrativos. Cada formato aparece pra você quando os dados daquela noite permitem.',
    items: [
      { label: 'SÓ O MOMENTO', copy: ['AQUI ACABOU', 'MEU PSICOLÓGICO.'], value: '187', meta: '22:47 · A Sky Full of Stars' },
      { label: 'MINHA NOITE', copy: ['DO INÍCIO AO FIM,', 'TUDO FEZ SENTIDO.'], value: '187', meta: 'O pico marcado no tempo real da noite.' },
      {
        label: 'A GALERA',
        copy: ['NINGUÉM TAVA', 'TRANQUILO.'],
        value: '187',
        you: '187',
        crowd: '172',
        youLabel: 'VOCÊ',
        crowdLabel: 'A TORCIDA',
        meta: 'Seu momento comparado com todo mundo que estava lá.',
      },
      { label: 'NA MESMA VIBE · EM BREVE', copy: ['NA MESMA VIBE.', 'LITERALMENTE.'], value: '187', meta: 'Você sentiu mais que quem tava no palco.' },
    ],
  },
  feed: {
    eyebrow: 'O FEED DO EVENTO',
    title: 'Todo mundo que tava lá, num lugar só.',
    body: [
      'Cada show e cada jogo vira um feed. Quem foi posta o próprio card com uma frase, e reage ao dos outros com um só botão: ',
    ],
    button: 'SENTI TB',
    bodyEnd: '. Seus amigos num feed, a noite inteira no outro.',
    stats: [
      { value: '8.734', text: 'pessoas compartilharam a noite da Taylor em São Paulo' },
      {
        value: '64%',
        text: 'bateram o próprio pico durante a mesma música — cada corpo no seu número, nunca um ranking',
      },
    ],
  },
  gallery: {
    eyebrow: 'A GALERIA DE SENTIMENTOS',
    title: 'Tudo que você sentiu, desde que chegou.',
    body: 'Cada noite vira um card, cada card guarda uma pele. Com o tempo, sua galeria conta a história: 14 noites, 312 momentos, um recorde. A forma fica, a pele muda — e a coleção é obviamente sua.',
    stats: [
      { value: '14', label: 'NOITES' },
      { value: '312', label: 'MOMENTOS' },
      { value: '187', label: 'SEU RECORDE' },
    ],
    minis: [
      { time: '23H47', value: '187', label: 'LOLLA — DIA 2' },
      { time: '94:12', value: '176', label: 'PALMEIRAS 2×1' },
      { time: '21H03', value: '142', label: 'COLDPLAY' },
      { time: '01H24', value: '116', label: 'REALNESS FEST' },
    ],
  },
  waitlist: {
    eyebrow: 'LISTA DE ESPERA',
    title: ['Quero isso no meu', 'próximo evento.'],
    body: 'A TumTum está começando em São Paulo, evento por evento. Entra na lista e a gente te chama quando for a sua vez.',
    placeholder: 'seu@email.com',
    button: 'Entrar na lista',
    privacy: 'A gente só usa seu e-mail pra te avisar dos próximos eventos. Nada além disso.',
    sending: 'Entrando…',
    joined: 'Pronto. Você está na lista.',
    already: 'Você já estava na lista.',
    joinedBody: 'A gente te chama quando a TumTum chegar num evento perto de você.',
    alreadyBody: 'Nada mudou, e é uma boa notícia: a gente já sabe onde te achar.',
    errorEmpty: 'Falta o e-mail.',
    errorInvalid: 'Esse e-mail não parece completo. Confere?',
    errorServer: 'Não deu pra salvar agora. Tenta de novo em um minuto.',
  },
  footer: {
    privacy:
      'Seus dados de batimento são seus. A TumTum só acessa o que você autorizar, só no intervalo do evento, e transforma em história apenas com a sua permissão.',
    line: 'TumTum · São Paulo · 2026',
    signIn: 'Entrar na sua conta',
  },
  meta: {
    title: 'TumTum — Você sentiu. Agora tem prova.',
    description:
      'Os momentos mais marcantes da sua vida têm hora, lugar e batida. A TumTum guarda os três.',
  },
}

export const EN: SiteCopy = {
  lang: 'en',
  otherHref: '/',
  nav: {
    how: 'How it works',
    cards: 'The cards',
    feed: 'The feed',
    gallery: 'Gallery',
    signIn: 'Sign in',
    cta: 'Join the list',
  },
  hero: {
    title: ['YOU FELT IT.', 'NOW YOU HAVE', 'PROOF.'],
    sub: 'The most defining moments of your life have a time, a place and a heartbeat. TumTum keeps all three.',
    ctaPrimary: 'I want this at my next event',
    ctaSecondary: 'How it works',
    card: {
      event: 'LOLLAPALOOZA — DAY 2 · 11:47PM',
      copy: ['I WAS FINE.', 'THEN THIS.'],
      unit: 'bpm — my highest of the night',
    },
  },
  proof: [
    { value: '176', caption: 'The whole crowd went with it.', meta: 'Penalty · 94:12' },
    { value: '191', caption: 'When they walked on, my heart went too.', meta: 'Show opener · 21:03' },
    { value: '183', caption: 'Nobody was calm.', meta: 'The drop · 23:41' },
  ],
  does: {
    eyebrow: 'WHAT TUMTUM DOES',
    title: ['Everyone filmed the show.', 'Nobody recorded what they felt.'],
    body: 'You keep the ticket stub, the wristband, the setlist screenshot. TumTum keeps the part that matters: ',
    highlight: 'the exact moment you lost your composure',
    bodyEnd: ' — with the time, the song and the proof.',
  },
  how: {
    eyebrow: 'HOW IT WORKS',
    title: ['You live the night.', 'We handle the rest.'],
    steps: [
      {
        n: '01',
        title: 'Connect the watch you already own',
        body: 'No new gadget, no fuss. The smartwatch you already wear records everything we need.',
      },
      {
        n: '02',
        title: 'Go to the event and forget about us',
        body: 'Sing, scream, suffer through the penalty. The watch stays on your wrist doing its job, like it always has.',
      },
      {
        n: '03',
        title: 'Then, the reveal',
        body: 'TumTum crosses your night with the event timeline and shows you which moment was yours. Then you just pick a card and post it.',
      },
    ],
  },
  cards: {
    eyebrow: 'THE CARD IS THE PROOF',
    title: ['One moment.', 'Many ways to tell it.'],
    intro:
      'Every night generates different cards of the same instant — and every night picks a skin. You decide which story hits the feed.',
    disclaimer:
      'The cards above are illustrative. Each format appears for you when that night’s data allows it.',
    items: [
      { label: 'JUST THE MOMENT', copy: ['THIS IS WHERE', 'I LOST IT.'], value: '187', meta: '22:47 · A Sky Full of Stars' },
      { label: 'MY NIGHT', copy: ['START TO FINISH,', 'IT ALL MADE SENSE.'], value: '187', meta: 'The peak marked on the night’s real timeline.' },
      {
        label: 'THE CROWD',
        copy: ['NOBODY WAS', 'CALM.'],
        value: '187',
        you: '187',
        crowd: '172',
        youLabel: 'YOU',
        crowdLabel: 'THE CROWD',
        meta: 'Your moment next to everyone who was there.',
      },
      { label: 'SAME VIBE · COMING SOON', copy: ['SAME VIBE.', 'LITERALLY.'], value: '187', meta: 'You felt it harder than the person on stage.' },
    ],
  },
  feed: {
    eyebrow: 'THE EVENT FEED',
    title: 'Everyone who was there, in one place.',
    body: [
      'Every show and every match becomes a feed. People who went post their own card with one line, and react to everyone else’s with a single button: ',
    ],
    button: 'FELT IT TOO',
    bodyEnd: '. Your friends in one feed, the whole night in the other.',
    stats: [
      { value: '8,734', text: 'people shared Taylor’s night in São Paulo' },
      {
        value: '64%',
        text: 'hit their own peak during the same song — every body has its own number, never a ranking',
      },
    ],
  },
  gallery: {
    eyebrow: 'THE GALLERY OF FEELINGS',
    title: 'Everything you’ve felt, since you joined.',
    body: 'Every night becomes a card, and every card keeps its skin. Over time your gallery tells the story: 14 nights, 312 moments, one record. The shape stays, the skin changes — and the collection is unmistakably yours.',
    stats: [
      { value: '14', label: 'NIGHTS' },
      { value: '312', label: 'MOMENTS' },
      { value: '187', label: 'YOUR RECORD' },
    ],
    minis: [
      { time: '23H47', value: '187', label: 'LOLLA — DAY 2' },
      { time: '94:12', value: '176', label: 'PALMEIRAS 2×1' },
      { time: '21H03', value: '142', label: 'COLDPLAY' },
      { time: '01H24', value: '116', label: 'REALNESS FEST' },
    ],
  },
  waitlist: {
    eyebrow: 'WAITLIST',
    title: ['I want this at my', 'next event.'],
    body: 'TumTum is starting in São Paulo, one event at a time. Join the list and we’ll call you when it’s your turn.',
    placeholder: 'you@email.com',
    button: 'Join the list',
    privacy: 'We only use your email to tell you about upcoming events. Nothing else.',
    sending: 'Joining…',
    joined: 'Done. You’re on the list.',
    already: 'You were already on the list.',
    joinedBody: 'We’ll call you when TumTum reaches an event near you.',
    alreadyBody: 'Nothing changed, and that’s good news: we already know where to find you.',
    errorEmpty: 'The email is missing.',
    errorInvalid: 'That email doesn’t look complete. Mind checking?',
    errorServer: 'Couldn’t save that right now. Try again in a minute.',
  },
  footer: {
    privacy:
      'Your heartbeat data is yours. TumTum only reads what you authorize, only during the event window, and only turns it into a story with your permission.',
    line: 'TumTum · São Paulo · 2026',
    signIn: 'Sign in to your account',
  },
  meta: {
    title: 'TumTum — You felt it. Now you have proof.',
    description:
      'The most defining moments of your life have a time, a place and a heartbeat. TumTum keeps all three.',
  },
}
