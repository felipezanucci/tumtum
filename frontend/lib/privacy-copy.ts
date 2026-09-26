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
 * the waitlist line "a gente só usa seu e-mail pra te avisar dos próximos
 * eventos", the consent screen (`consent-copy.ts`, text version 2026-09-26)
 * and the LGPD remediation contract of 26/09 — its retention windows, its
 * crowd thresholds and its list of operators are quoted here as promises.
 *
 * The controller's legal name and CNPJ do not exist yet; the bracketed
 * placeholder is deliberate and visible until Felipe fills it in.
 */

export interface PrivacyCopy {
  lang: 'pt-BR' | 'en'
  title: string
  intro: string
  /** One line set apart under the intro — the privacy page's "not medical". */
  disclaimer?: string
  updated: string
  sections: { heading: string; paragraphs: string[]; items?: string[] }[]
  /** `subject` pre-fills the e-mail, so a request reaches the right inbox. */
  contact: { lead: string; email: string; subject?: string }
  backHome: string
  meta: { title: string; description: string }
}

export const PRIVACY_PT: PrivacyCopy = {
  lang: 'pt-BR',
  title: 'Privacidade',
  intro:
    'Seus batimentos são seus. Esta página diz, sem rodeio, o que a TumTum guarda, pra quê, por quanto tempo, com quem, e como você decide sobre tudo isso.',
  disclaimer: 'A TumTum não é um dispositivo médico e não interpreta saúde.',
  updated: 'Atualizada em 26 de setembro de 2026. Texto de consentimento: versão 2026-09-26.',
  sections: [
    {
      heading: 'Quem cuida dos seus dados',
      paragraphs: [
        'Controlador: TumTum (Felipe Zanucci, São Paulo, SP) — [CONTROLADOR — preencher razão social e CNPJ].',
        'Encarregado pelo tratamento de dados pessoais: fale pelo e-mail oi@tumtum.cc, com o assunto "Privacidade". É o canal para qualquer pedido desta página.',
      ],
    },
    {
      heading: 'Batimento é dado de saúde',
      paragraphs: [
        'Para a Lei Geral de Proteção de Dados (LGPD), batimento cardíaco é dado pessoal sensível, referente à saúde. Por isso a TumTum só trata seu batimento com o seu consentimento específico e destacado (art. 11, I, da LGPD): um sim separado para cada uso, que você dá, vê e retira quando quiser.',
        'Seus dados de conta (e-mail, nome, data de nascimento) são tratados para cumprir o contrato que você aceita ao criar a conta. Os registros de acesso, porque a lei manda guardar (Marco Civil da Internet, art. 15).',
        'A TumTum não é um dispositivo médico e não interpreta saúde. Ela mostra quando seu coração subiu, nunca o que isso quer dizer: nada aqui é diagnóstico, alerta ou avaliação.',
      ],
    },
    {
      heading: 'O que a TumTum guarda',
      paragraphs: ['Só o que a experiência precisa:'],
      items: [
        'Sua conta: e-mail, nome e data de nascimento (pra confirmar que você tem 18 anos ou mais).',
        'Sua senha, guardada só como hash: de um jeito que ninguém consegue ler, nem a gente.',
        'Sua foto de perfil: fica só no seu celular, no app. Não sobe.',
        'Enquanto a conta é criada: e-mail, nome, data de nascimento e senha (em hash) esperam o código de 6 números que mandamos pro seu e-mail. Se o código não voltar, isso é apagado em até 24 horas.',
        'Suas leituras de batimento: as que o app lê do Health Connect (do seu relógio), as de um sensor Bluetooth (uma cinta cardíaca) e as de um arquivo que você importa (Polar, Apple Saúde, Samsung Health, CSV ou JSON). Cada leitura é uma hora e um número.',
        'Intervalos R-R (o tempo entre duas batidas) e o movimento do celular: ficam só no aparelho, pra saber se o sensor está mesmo no corpo, e são apagados quando a noite é salva. Não sobem.',
        'Seus momentos: os trechos da noite em que seu coração subiu, que a TumTum encontra.',
        'Seus cards, e se cada um está público.',
        'No feed de um evento: seus posts, suas reações, as denúncias e os bloqueios que você faz.',
        'Quando você compartilha um card, em qual rede foi.',
        'Seus consentimentos: cada sim e cada não, com a data e a versão do texto que você leu.',
        'Seus pedidos ao encarregado, e as respostas.',
        'Registros de acesso: endereço IP, data e hora, e a rota pedida ao servidor.',
        'Na lista de espera do site: só o e-mail.',
      ],
    },
    {
      heading: 'Pra que a TumTum usa, e com qual sim',
      paragraphs: [
        'Cada uso tem o seu consentimento, pedido na tela de consentimento, no cadastro ou no app. Os opcionais começam desligados, e nada do que é essencial depende deles.',
        'Os registros de acesso não dependem de consentimento: servem pra manter o serviço de pé, investigar abuso e cumprir a lei.',
      ],
      items: [
        'Termos e Política (necessário pra ter conta): você aceita marcando a caixa no cadastro.',
        'Ler seus batimentos (necessário pra gravar uma noite): a TumTum lê só na janela dos eventos que você ativar, do começo menos 30 minutos ao fim mais 30 minutos, e encontra seus momentos.',
        'Guardar suas noites (opcional): a noite sobe pros servidores da TumTum e fica na sua coleção. Sem esse sim, a noite fica só no seu celular.',
        'Estatística da galera (opcional): sua noite entra, sem seu nome, na conta coletiva do evento.',
        'Comparar com artista ou atleta (opcional): ainda não existe. Sua escolha fica guardada pra quando existir.',
        'Melhorar o detector de momentos (opcional): sua noite, sem seu nome, ajuda a TumTum a encontrar momentos melhor.',
        'Novidades por e-mail (opcional). Sem esse sim, você só recebe e-mail da sua conta: código, senha, respostas aos seus pedidos.',
      ],
    },
    {
      heading: 'Como mudar ou retirar um consentimento',
      paragraphs: [
        'No site, em Perfil → Privacidade ou em tumtum.cc/consentimento. No app, em Configurações → Privacidade. Vale dali pra frente.',
        'Retirar um sim não apaga sozinho o que já foi guardado com ele. Pra apagar, use "Apagar esta noite" em cada noite, ou apague a conta.',
        'No Android, dá também pra retirar a permissão do Health Connect nas configurações dele. A TumTum para de ler na hora.',
        'Retirar o aceite dos Termos é o mesmo que encerrar a conta.',
      ],
    },
    {
      heading: 'Seu relógio, sua cinta e seus arquivos',
      paragraphs: [
        'No Android, o app da TumTum pede uma única permissão do Health Connect: ler o batimento cardíaco que seu relógio já gravou. Nada de passos, sono, peso ou qualquer outro dado.',
        'Ele lê só a janela do evento que você ativou, e só depois que você autoriza. Pra descobrir qual relógio você usa, na configuração, o app olha uma vez os últimos 60 minutos, no próprio celular, e não envia nada.',
        'Com uma cinta cardíaca, a captura acontece no seu celular, por Bluetooth. A noite só sobe pra TumTum quando você toca em "Guardar minha noite", com "Guardar minhas noites" ligado.',
        'No site, ao importar um arquivo, você escolhe o evento primeiro, e só as leituras da janela dele (30 minutos antes do começo a 30 minutos depois do fim) saem do seu navegador. O resto do arquivo fica no seu computador.',
      ],
    },
    {
      heading: 'A galera: a estatística coletiva',
      paragraphs: [
        'Com o seu sim, sua noite conta, sem seu nome, na estatística do evento: quantas pessoas sentiram cada momento.',
        'Ela só aparece quando o evento passa de 100 noites medidas, e sai em faixas ("10+", "25+", "50+", "100+", "250+"), nunca com o número exato. Um momento sentido por menos de 10 pessoas não aparece.',
      ],
    },
    {
      heading: 'O que a TumTum não faz',
      items: [
        'Não vende seus dados. Nunca.',
        'Nenhum dado pessoal vai para clube, artista, produtora, festival ou anunciante. Só estatística anonimizada pode sair.',
        'Não mostra anúncios.',
        'Não lê nada do seu relógio fora da janela do evento que você ativou.',
        'Não publica nada sem você apertar o botão.',
        'Não toma decisão sobre você. O detector encontra momentos numa curva, e só.',
      ],
      paragraphs: [],
    },
    {
      heading: 'Quando você compartilha um card',
      paragraphs: [
        'Um card só fica público quando você compartilha. A página dele mostra o que a imagem mostra: seu nome, o número, o momento, a hora e o evento. Não mostra sua curva inteira nem sua conta, e pede pra buscadores não indexarem.',
        'Depois que você posta o card no Instagram, no WhatsApp ou em qualquer rede, a TumTum não tem controle sobre ele lá: quem viu pode salvar, e apagar lá é com você, pelas regras daquela rede.',
        'Pra desfazer do lado da TumTum: "Despublicar" tira a página e a imagem do ar na hora. Apagar o card também.',
      ],
    },
    {
      heading: 'O feed de um evento',
      paragraphs: [
        'Um momento só aparece no feed quando você aperta "mostrar", e só para quem também tem uma noite medida naquele evento — numa turnê, para quem foi em qualquer data, se você escolher. Aparecem seu nome, o número, a hora e o nome do momento; nunca sua curva nem seu e-mail. Dá para tirar quando quiser.',
        'O que alguém posta é responsabilidade de quem postou. Se algo te incomodar, denuncie o post ou bloqueie a pessoa: vocês deixam de se ver, e uma pessoa da TumTum lê cada denúncia.',
      ],
    },
    {
      heading: 'Quem mais trata seus dados',
      paragraphs: [
        'A TumTum usa estes serviços (operadores), que tratam dados em nome dela e só para isso:',
      ],
      items: [
        'Railway: servidores e banco de dados. Região: a confirmar.',
        'Vercel: hospeda o site. Região: a confirmar.',
        'Resend: envia os e-mails (código de cadastro, troca de senha e de e-mail). Região: a confirmar.',
        'Sentry: recebe erros técnicos quando algo quebra, sem o corpo das requisições — ou seja, sem seus batimentos. Região: a confirmar.',
      ],
    },
    {
      heading: 'Dados fora do Brasil',
      paragraphs: [
        'Seus dados podem ser hospedados fora do Brasil pelos operadores citados acima. Quando isso acontece, a transferência é feita com garantias contratuais de proteção compatíveis com a LGPD.',
        'A equipe da TumTum acessa dados individuais só para resolver um problema que você reportou, verificar um erro técnico ou moderar uma denúncia. Cada acesso a dados de batimento fica registrado.',
      ],
    },
    {
      heading: 'Por quanto tempo',
      items: [
        'Leituras de batimento (a série bruta da noite): apagadas 30 dias depois que os momentos da noite são encontrados.',
        'Momentos, resumo da noite e cards: enquanto sua conta existir e "Guardar minhas noites" estiver ligado.',
        'Imagem do card guardada pra carregar rápido: até 7 dias. Um card despublicado ou apagado deixa de ser mostrado na hora.',
        'Registros de acesso: 180 dias.',
        'Cadastro não confirmado: 24 horas.',
        'Lista de espera: até você pedir pra sair.',
        'Consentimentos e pedidos ao encarregado: enquanto a conta existir, pra provar o que você escolheu e o que pediu.',
      ],
      paragraphs: [
        'Cada tipo de dado tem seu prazo. Apagou a conta, apagou tudo: fica só o registro de que uma conta foi apagada, com a data e nada que identifique você.',
      ],
    },
    {
      heading: 'Seus direitos, e como exercer',
      paragraphs: [
        'Todo pedido pode ser feito no seu Perfil (Pedidos ao encarregado), no app ou por e-mail para oi@tumtum.cc, assunto "Privacidade". A resposta vem em até 15 dias. Você também pode reclamar à Autoridade Nacional de Proteção de Dados (ANPD).',
      ],
      items: [
        'Acesso e confirmação: Perfil → Privacidade → Baixar meus dados mostra tudo o que a TumTum tem sobre você.',
        'Portabilidade: o mesmo lugar entrega um arquivo JSON completo e um CSV com suas leituras, pra levar pra onde quiser.',
        'Correção: nome e e-mail no Perfil. Qualquer outro dado, por pedido ao encarregado.',
        'Exclusão: "Apagar esta noite" em cada noite (em Suas sessões, ou no app), "Despublicar" ou apagar cada card, e "Apagar minha conta" no Perfil ou no app, com sua senha.',
        'Revogação: desligue qualquer consentimento em Perfil → Privacidade, em /consentimento ou no app.',
        'Informação sobre compartilhamento: esta página lista todos os operadores. Pra saber mais, peça ao encarregado.',
        'Revisão: o detector de momentos é automático, mas não decide nada sobre você. Se quiser que uma pessoa olhe, peça ao encarregado.',
      ],
    },
    {
      heading: 'Só para maiores de 18',
      paragraphs: [
        'A TumTum é só para quem tem 18 anos ou mais. O cadastro pede sua data de nascimento, e o servidor recusa quem tem menos.',
        'Se a gente descobrir que uma conta é de alguém com menos de 18 anos, a conta e tudo que está nela são apagados. Se você souber de um caso, escreva para oi@tumtum.cc.',
      ],
    },
    {
      heading: 'Quando esta página mudar',
      paragraphs: [
        'Se algo aqui mudar de um jeito que importa, a gente avisa no app ou por e-mail antes de valer. Se um uso mudar, a TumTum pede o seu sim de novo.',
      ],
    },
  ],
  contact: {
    lead: 'Dúvida, pedido ou desconforto com qualquer coisa daqui: fala com o encarregado.',
    email: 'oi@tumtum.cc',
    subject: 'Privacidade',
  },
  backHome: '← Voltar para a TumTum',
  meta: {
    title: 'Privacidade — TumTum',
    description:
      'O que a TumTum guarda dos seus batimentos, pra quê, por quanto tempo, com quem, e como você decide. Curto, porque é sério.',
  },
}

export const PRIVACY_EN: PrivacyCopy = {
  lang: 'en',
  title: 'Privacy',
  intro:
    'Your heartbeat is yours. This page says plainly what TumTum keeps, what for, for how long, with whom, and how you decide about all of it.',
  disclaimer: 'TumTum is not a medical device and does not interpret health.',
  updated: 'Updated 26 September 2026. Consent text: version 2026-09-26.',
  sections: [
    {
      heading: 'Who looks after your data',
      paragraphs: [
        'Controller: TumTum (Felipe Zanucci, São Paulo, Brazil) — [CONTROLADOR — preencher razão social e CNPJ].',
        'Data protection officer (encarregado): write to oi@tumtum.cc with the subject "Privacidade". It is the channel for every request on this page.',
      ],
    },
    {
      heading: 'Heart rate is health data',
      paragraphs: [
        'Under Brazil’s General Data Protection Law (LGPD), heart rate is sensitive personal data, concerning health. So TumTum only processes your heartbeat with your specific and highlighted consent (LGPD art. 11, I): a separate yes for each use, which you give, see and withdraw whenever you like.',
        'Your account data (email, name, date of birth) is processed to perform the contract you accept when you create the account. Access logs, because the law requires keeping them (Marco Civil da Internet, art. 15).',
        'TumTum is not a medical device and does not interpret health. It shows when your heart rose, never what that means: nothing here is a diagnosis, an alert or an assessment.',
      ],
    },
    {
      heading: 'What TumTum keeps',
      paragraphs: ['Only what the experience needs:'],
      items: [
        'Your account: email, name and date of birth (to confirm you are 18 or older).',
        'Your password, stored only as a hash: in a way nobody can read, us included.',
        'Your profile photo: stays on your phone, in the app. It is not uploaded.',
        'While the account is being created: the email, name, date of birth and password (hashed) wait for the 6-digit code we send to your email. If the code never comes back, they are deleted within 24 hours.',
        'Your heart-rate readings: those the app reads from Health Connect (from your watch), from a Bluetooth sensor (a chest strap), and from a file you import (Polar, Apple Health, Samsung Health, CSV or JSON). Each reading is a time and a number.',
        'R-R intervals (the time between two beats) and the phone’s motion: kept only on the device, to tell whether the sensor is really on the body, and deleted when the night is saved. They are not uploaded.',
        'Your moments: the stretches of the night when your heart rose, which TumTum finds.',
        'Your cards, and whether each one is public.',
        'In an event feed: your posts, your reactions, and the reports and blocks you make.',
        'When you share a card, which network it went to.',
        'Your consents: every yes and every no, with the date and the version of the text you read.',
        'Your requests to the data protection officer, and the answers.',
        'Access logs: IP address, date and time, and the path requested from the server.',
        'On the site waitlist: only the email.',
      ],
    },
    {
      heading: 'What TumTum uses it for, and with which yes',
      paragraphs: [
        'Each use has its own consent, asked on the consent screen, at sign-up or in the app. The optional ones start off, and nothing essential depends on them.',
        'Access logs do not depend on consent: they keep the service running, help investigate abuse and meet the law.',
      ],
      items: [
        'Terms and Policy (required for an account): you accept by ticking the box at sign-up.',
        'Reading your heartbeat (required to record a night): TumTum reads only in the window of the events you activate, from 30 minutes before the start to 30 minutes after the end, and finds your moments.',
        'Keeping your nights (optional): the night is uploaded to TumTum’s servers and kept in your collection. Without this yes, the night stays on your phone.',
        'Crowd statistics (optional): your night joins, without your name, the event’s collective count.',
        'Comparing with an artist or athlete (optional): does not exist yet. Your choice is kept for when it does.',
        'Improving the moment detector (optional): your night, without your name, helps TumTum find moments better.',
        'News by email (optional). Without this yes, you only get account emails: codes, passwords, answers to your requests.',
      ],
    },
    {
      heading: 'How to change or withdraw a consent',
      paragraphs: [
        'On the site, under Profile → Privacidade or at tumtum.cc/consentimento. In the app, under Configurações → Privacidade. It applies from then on.',
        'Withdrawing a yes does not by itself delete what was already kept under it. To delete, use "Apagar esta noite" on each night, or delete the account.',
        'On Android, you can also withdraw the Health Connect permission in its settings. TumTum stops reading at once.',
        'Withdrawing acceptance of the Terms is the same as closing the account.',
      ],
    },
    {
      heading: 'Your watch, your strap and your files',
      paragraphs: [
        'On Android, the TumTum app asks for a single Health Connect permission: to read the heart rate your watch already recorded. No steps, sleep, weight or any other data.',
        'It reads only the window of the event you activated, and only after you allow it. To find out which watch you use, during setup, the app looks once at the last 60 minutes, on the phone itself, and sends nothing.',
        'With a chest strap, the capture happens on your phone, over Bluetooth. The night only reaches TumTum when you tap "Guardar minha noite", with "Keep my nights" on.',
        'On the site, when you import a file, you choose the event first, and only the readings in its window (30 minutes before the start to 30 minutes after the end) leave your browser. The rest of the file stays on your computer.',
      ],
    },
    {
      heading: 'A galera: the crowd statistics',
      paragraphs: [
        'With your yes, your night counts, without your name, in the event’s statistics: how many people felt each moment.',
        'They only appear once the event passes 100 measured nights, and are published in bands ("10+", "25+", "50+", "100+", "250+"), never the exact number. A moment felt by fewer than 10 people does not appear.',
      ],
    },
    {
      heading: 'What TumTum does not do',
      items: [
        'Does not sell your data. Ever.',
        'No personal data goes to any club, artist, promoter, festival or advertiser. Only anonymised statistics may leave.',
        'Does not show ads.',
        'Does not read anything from your watch outside the window of the event you activated.',
        'Does not publish anything unless you press the button.',
        'Does not make decisions about you. The detector finds moments in a curve, and that is all.',
      ],
      paragraphs: [],
    },
    {
      heading: 'When you share a card',
      paragraphs: [
        'A card becomes public only when you share it. Its page shows what the image shows: your name, the number, the moment, the time and the event. Not your whole curve, not your account, and it asks search engines not to index it.',
        'Once you post the card on Instagram, WhatsApp or any network, TumTum has no control over it there: whoever saw it can save it, and deleting it there is up to you, under that network’s rules.',
        'To undo it on TumTum’s side: "Despublicar" (unpublish) takes the page and the image down at once. Deleting the card does too.',
      ],
    },
    {
      heading: 'An event feed',
      paragraphs: [
        'A moment appears in the feed only when you press "show", and only to people who also have a measured night at that event — on a tour, to anyone at any date, if you choose. It shows your name, the number, the time and the moment’s name; never your curve or your email. You can take it down whenever you like.',
        'What someone posts is the responsibility of whoever posted it. If something bothers you, report the post or block the person: you stop seeing each other, and a person at TumTum reads every report.',
      ],
    },
    {
      heading: 'Who else processes your data',
      paragraphs: [
        'TumTum uses these services (processors), which handle data on its behalf and only for this:',
      ],
      items: [
        'Railway: servers and database. Region: to be confirmed.',
        'Vercel: hosts the site. Region: to be confirmed.',
        'Resend: sends the emails (sign-up code, password and email changes). Region: to be confirmed.',
        'Sentry: receives technical errors when something breaks, without request bodies — that is, without your heartbeats. Region: to be confirmed.',
      ],
    },
    {
      heading: 'Data outside Brazil',
      paragraphs: [
        'Your data may be hosted outside Brazil by the processors above. When that happens, the transfer is made with contractual safeguards compatible with the LGPD.',
        'The TumTum team looks at individual data only to solve a problem you reported, check a technical error or moderate a report. Every access to heart-rate data is logged.',
      ],
    },
    {
      heading: 'For how long',
      items: [
        'Heart-rate readings (the night’s raw series): deleted 30 days after the night’s moments are found.',
        'Moments, the night’s summary and cards: while your account exists and "Keep my nights" is on.',
        'Card image kept to load quickly: up to 7 days. An unpublished or deleted card stops being shown at once.',
        'Access logs: 180 days.',
        'Unconfirmed sign-up: 24 hours.',
        'Waitlist: until you ask to leave.',
        'Consents and requests to the data protection officer: while the account exists, to prove what you chose and what you asked.',
      ],
      paragraphs: [
        'Each kind of data has its own window. Delete the account and all of it goes: only a record that an account was deleted remains, with the date and nothing that identifies you.',
      ],
    },
    {
      heading: 'Your rights, and how to use them',
      paragraphs: [
        'Every request can be made on your Profile (Pedidos ao encarregado), in the app, or by email to oi@tumtum.cc, subject "Privacidade". The answer comes within 15 days. You can also complain to Brazil’s data protection authority (ANPD).',
      ],
      items: [
        'Access and confirmation: Profile → Privacidade → Baixar meus dados shows everything TumTum holds about you.',
        'Portability: the same place gives you a complete JSON file and a CSV of your readings, to take wherever you like.',
        'Correction: name and email on your Profile. Any other data, by request to the data protection officer.',
        'Deletion: "Apagar esta noite" on each night (in Suas sessões, or in the app), unpublish or delete each card, and "Apagar minha conta" on your Profile or in the app, with your password.',
        'Withdrawal: turn off any consent under Profile → Privacidade, at /consentimento or in the app.',
        'Information about sharing: this page lists every processor. To know more, ask the data protection officer.',
        'Review: the moment detector is automatic, but it decides nothing about you. If you want a person to look, ask the data protection officer.',
      ],
    },
    {
      heading: '18 and over only',
      paragraphs: [
        'TumTum is only for people aged 18 or over. Sign-up asks for your date of birth, and the server refuses anyone younger.',
        'If we find that an account belongs to someone under 18, the account and everything in it are deleted. If you know of a case, write to oi@tumtum.cc.',
      ],
    },
    {
      heading: 'When this page changes',
      paragraphs: [
        'If something here changes in a way that matters, we will tell you in the app or by email before it takes effect. If a use changes, TumTum asks for your yes again.',
      ],
    },
  ],
  contact: {
    lead: 'A question, a request, or anything here that does not sit right: talk to the data protection officer.',
    email: 'oi@tumtum.cc',
    subject: 'Privacidade',
  },
  backHome: '← Back to TumTum',
  meta: {
    title: 'Privacy — TumTum',
    description:
      'What TumTum keeps of your heartbeat, what for, for how long, with whom, and how you decide. Short, because it matters.',
  },
}
