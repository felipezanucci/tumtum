/**
 * The consent screen's words, in both languages (26/09, LGPD remediation).
 *
 * Heart rate is sensitive personal data (LGPD art. 5 II, art. 11), and the
 * only lawful basis this product has for it is consent that is specific,
 * separate per purpose, recorded and revocable. So each purpose below is one
 * switch with its own sentence, and the version of these words travels with
 * every grant: `CONSENT_TEXT_VERSION` is what the server stores next to the
 * "yes", and it must change whenever a sentence here changes meaning.
 *
 * The same seven keys, and the same version literal, exist in the backend
 * and the Android app (the shared contract of 26/09). Do not rename one here
 * alone.
 *
 * This is the screen where the brand goes quiet: no jokes, no pink fields,
 * short sentences that say exactly what happens.
 */

export const CONSENT_TEXT_VERSION = '2026-09-26'

export type ConsentPurpose =
  | 'terms'
  | 'read_heart_rate'
  | 'keep_night'
  | 'crowd_stats'
  | 'artist_compare'
  | 'improve_detection'
  | 'marketing'

/** In the order the screen shows them: what the core needs first. */
export const CONSENT_PURPOSES: readonly ConsentPurpose[] = [
  'terms',
  'read_heart_rate',
  'keep_night',
  'crowd_stats',
  'artist_compare',
  'improve_detection',
  'marketing',
]

/**
 * The two the core loop needs, each with its own explicit tap. Everything
 * else is optional and always starts off.
 */
export const CORE_PURPOSES: readonly ConsentPurpose[] = ['terms', 'read_heart_rate']

export function isConsentPurpose(value: unknown): value is ConsentPurpose {
  return typeof value === 'string' && (CONSENT_PURPOSES as readonly string[]).includes(value)
}

export interface PurposeCopy {
  title: string
  description: string
  /** What turning it off costs, said plainly. Only where it costs something. */
  without?: string
}

export interface ConsentCopy {
  lang: 'pt-BR' | 'en'
  title: string
  intro: string
  /** The facts a person needs before any switch: one short line each. */
  facts: string[]
  purposes: Record<ConsentPurpose, PurposeCopy>
  requiredTag: string
  optionalTag: string
  /** Under the Terms once accepted: they are withdrawn by deleting the account. */
  lockedNote: string
  save: string
  saving: string
  saved: string
  version: string
  birthDate: { label: string; hint: string }
  termsLink: string
  privacyLink: string
}

export const CONSENT_PT: ConsentCopy = {
  lang: 'pt-BR',
  title: 'Seus batimentos, suas regras',
  intro:
    'Antes de tudo, o que a TumTum faz com seu batimento. Cada uso tem um sim separado, e você muda qualquer um quando quiser.',
  facts: [
    'Batimento é dado de saúde. A lei (LGPD) trata ele como dado pessoal sensível, e por isso a TumTum só usa com o seu sim, uso por uso.',
    'A leitura acontece só na janela dos eventos que você ativar: do começo do evento menos 30 minutos até o fim mais 30 minutos. Fora disso, nada é lido.',
    'Cada leitura da noite (a série bruta) é apagada 30 dias depois que seus momentos são encontrados. Os momentos e os cards ficam enquanto sua conta existir e "Guardar minhas noites" estiver ligado.',
    'A TumTum não é um dispositivo médico e não interpreta saúde. Ela mostra quando seu coração subiu, nunca o que isso quer dizer.',
    'Nada vai para clubes, artistas, produtoras, festivais ou anunciantes.',
    'Mudou de ideia? Muda aqui, no seu perfil, ou no app em Configurações → Privacidade. Vale dali pra frente.',
  ],
  purposes: {
    terms: {
      title: 'Termos de Uso e Política de Privacidade',
      description: 'Você leu e aceita os Termos de Uso e a Política de Privacidade.',
      without: 'Sem esse aceite não dá pra ter conta na TumTum.',
    },
    read_heart_rate: {
      title: 'Ler meus batimentos nos eventos',
      description:
        'A TumTum lê seus batimentos na janela dos eventos que você ativar e encontra seus momentos.',
      without: 'Sem isso, nenhuma noite é gravada.',
    },
    keep_night: {
      title: 'Guardar minhas noites',
      description:
        'Sua noite (as leituras e os momentos) fica guardada na sua coleção, nos servidores da TumTum, pra você rever e fazer cards.',
      without: 'Sem isso, a noite fica só no seu celular e não sobe.',
    },
    crowd_stats: {
      title: 'Entrar na estatística da galera',
      description:
        'Sua noite entra, sem seu nome, na conta coletiva do evento ("A galera"). Só aparece quando o evento passa de 100 noites, e sempre em faixas, nunca o número exato de pessoas.',
    },
    artist_compare: {
      title: 'Comparar com artista ou atleta',
      description:
        'Quando existir: comparar sua noite com a de um artista ou atleta que topou participar. Ainda não existe; sua escolha fica guardada.',
    },
    improve_detection: {
      title: 'Ajudar a melhorar o detector de momentos',
      description:
        'A TumTum usa sua noite, sem seu nome, pra encontrar momentos melhor nas próximas.',
    },
    marketing: {
      title: 'Receber novidades por e-mail',
      description:
        'A TumTum te manda e-mail sobre eventos e novidades. Sem isso, você só recebe e-mail da sua conta, como códigos e senha.',
    },
  },
  requiredTag: 'Necessário',
  optionalTag: 'Opcional',
  lockedNote: 'Aceito. Pra retirar esse aceite, apague a conta.',
  save: 'Salvar minhas escolhas',
  saving: 'Salvando…',
  saved: 'Pronto. Suas escolhas estão salvas.',
  version: `Versão do texto: ${CONSENT_TEXT_VERSION}`,
  birthDate: {
    label: 'Data de nascimento',
    hint: 'A TumTum é só para quem tem 18 anos ou mais.',
  },
  termsLink: 'Termos de Uso',
  privacyLink: 'Política de Privacidade',
}

export const CONSENT_EN: ConsentCopy = {
  lang: 'en',
  title: 'Your heartbeat, your rules',
  intro:
    'First, what TumTum does with your heartbeat. Each use has its own yes, and you can change any of them whenever you like.',
  facts: [
    'Heart rate is health data. Brazilian law (LGPD) treats it as sensitive personal data, so TumTum only uses it with your yes, use by use.',
    'Reading happens only in the window of the events you activate: from 30 minutes before the event starts to 30 minutes after it ends. Nothing is read outside it.',
    'Each reading of the night (the raw series) is deleted 30 days after your moments are found. Moments and cards stay while your account exists and "Keep my nights" is on.',
    'TumTum is not a medical device and does not interpret health. It shows when your heart rose, never what that means.',
    'Nothing goes to clubs, artists, promoters, festivals or advertisers.',
    'Changed your mind? Change it here, on your profile, or in the app under Configurações → Privacidade. It applies from then on.',
  ],
  purposes: {
    terms: {
      title: 'Terms of Use and Privacy Policy',
      description: 'You have read and accept the Terms of Use and the Privacy Policy.',
      without: 'Without this there is no TumTum account.',
    },
    read_heart_rate: {
      title: 'Read my heartbeat at events',
      description:
        'TumTum reads your heartbeat in the window of the events you activate and finds your moments.',
      without: 'Without this, no night is recorded.',
    },
    keep_night: {
      title: 'Keep my nights',
      description:
        'Your night (the readings and the moments) is kept in your collection, on TumTum’s servers, so you can relive it and make cards.',
      without: 'Without this, the night stays on your phone and is not uploaded.',
    },
    crowd_stats: {
      title: 'Count me in the crowd statistics',
      description:
        'Your night joins, without your name, the event’s collective count ("A galera"). It only appears once the event passes 100 nights, and always in bands, never the exact number of people.',
    },
    artist_compare: {
      title: 'Compare with an artist or athlete',
      description:
        'When it exists: compare your night with an artist or athlete who agreed to take part. It does not exist yet; your choice is kept.',
    },
    improve_detection: {
      title: 'Help improve the moment detector',
      description: 'TumTum uses your night, without your name, to find moments better next time.',
    },
    marketing: {
      title: 'Get news by email',
      description:
        'TumTum emails you about events and news. Without this, you only get account emails, such as codes and passwords.',
    },
  },
  requiredTag: 'Required',
  optionalTag: 'Optional',
  lockedNote: 'Accepted. To withdraw it, delete the account.',
  save: 'Save my choices',
  saving: 'Saving…',
  saved: 'Done. Your choices are saved.',
  version: `Text version: ${CONSENT_TEXT_VERSION}`,
  birthDate: {
    label: 'Date of birth',
    hint: 'TumTum is only for people aged 18 or over.',
  },
  termsLink: 'Terms of Use',
  privacyLink: 'Privacy Policy',
}
