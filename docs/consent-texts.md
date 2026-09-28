# Textos de consentimento

The full words of each consent text version, as the person reads them on the
consent screen. The server does not store them: every row of `consents`
carries its `text_version` and a `proof` — the SHA-256 hex of
`"{purpose}:{text_version}"` — and this document is where that version's
words are kept on the record (v1.1 legal opinion, §4.2 "consent ledger").
The same words live in the clients' string resources: on Android,
`android/app/src/main/res/values/strings.xml` (`consent_*`), mapped per
purpose in `ui/screens/consent/ConsentCopy.kt`.

**Any change to these sentences is a new version**: a new
`CONSENT_TEXT_VERSION` in `backend/app/services/consents.py`, a new
`ConsentText.VERSION` in the app, the same in the web, and a new section here
— never an edit of a section below. A row whose `proof` is the hash of
`"{purpose}:{version}"` points at exactly one section of this file.

The texts are in Brazilian Portuguese as shown; they are copied verbatim from
the strings (the title's Android `\n` line break written as a space).

## Versão 2026-09-26.1

Mesma tela, duas frases mudadas na noite de 26/09 depois do parecer v1.1
(`docs/decision-log.md`, entrada de 26/09 à noite). Nenhum consentimento
anterior foi invalidado: a retenção prometida caiu, não subiu, e a frase
sobre terceiros ficou mais precisa, não mais permissiva. Consentimentos
novos carregam esta versão; os antigos guardam a `2026-09-26`.

- `consent_intro_retention`: "…a série de batidas fica no servidor por
  **7 dias** depois do momento…" (era 30). Acompanha
  `RAW_READINGS_RETENTION_DAYS=7`.
- `consent_keep_stops`: "Nenhuma noite nova sobe pra TumTum, e as que já
  subiram são apagadas do servidor em até 24 horas. Elas continuam só no seu
  celular." (era "…Pra apagar uma que já subiu, abre a noite e toca em Apagar
  esta noite.", que contradizia `services/maintenance.py`: revogar apaga as
  noites guardadas depois de 24 h de carência).
- `consent_intro_nobody`: "Seu dado cardíaco individual não vai para
  clubes, artistas, produtoras, festivais ou anunciantes. Fornecedores
  contratados processam dados só em nome da TumTum." (era "Nada vai para
  clubes, artistas ou anunciantes.", absoluto demais: operadores contratados
  processam em nome da TumTum — parecer v1.1, §20.1).

Fora da tela de consentimento, na mesma noite, a tela de permissão do
Health Connect trocou `perm_dont_2` por "Seu dado cardíaco não vai para
anunciantes, clubes ou artistas" e `perm_footnote` passou a dizer que as
cópias de segurança expiram no prazo da política.

**28/09 — linhas explicativas, não texto de consentimento (versão mantida).**
Depois da rodada de testes no b217 (`docs/decision-log.md`, entrada de
28/09), as telas ganharam frases *em volta* das finalidades, sem mudar a
frase de nenhuma finalidade — por isso a versão continua `2026-09-26.1` e
nenhum consentimento foi fechado:

- Cabeçalho das duas obrigatórias, no app e no site: "Pra usar a TumTum ·
  as duas precisam estar ligadas/marcadas", com "Liga as duas pra
  continuar." enquanto uma estiver desligada. O botão não segue sem as
  duas (LGPD art. 9, §3: o serviço pode depender do consentimento que ele
  de fato precisa, dito com destaque).
- Sob `keep_night`, uma razão para ligar (continua desligada de fábrica):
  "Pra sua noite ficar na sua coleção, entrar no feed do evento e não
  sumir se você trocar de celular."
- Sob cada chave desligada, o que a revogação faz, recalculado a cada
  exibição e não só no toque: para `keep_night`, "Desligado. Suas noites
  guardadas saem do servidor em até 24 horas e continuam neste celular."
  (ou o prazo exato, `revoked_at` + 24 h).

## Versão 2026-09-26

### O que a tela diz antes das chaves

**Antes de ler sua batida.**

- Batimento é dado de saúde. Por isso cada uso tem a sua chave, e nenhuma fica ligada sem você ligar.
- A gente só lê sua batida na janela do evento: de 30 minutos antes do começo até 30 minutos depois do fim.
- Se você guardar a noite na TumTum, a série de batidas fica no servidor por 30 dias depois do momento. Os momentos e os cards ficam enquanto sua conta existir e “Guardar a noite” estiver ligado.
- A TumTum não é um dispositivo médico e não interpreta saúde.
- Nada vai para clubes, artistas ou anunciantes.
- Dá pra desligar qualquer chave quando quiser, em Configurações.

> **Note, 26/09.** This version says the raw series stays *30 dias*. The same
> day the server's default became 7 (`RAW_READINGS_RETENTION_DAYS`, v1.1
> opinion §11). Keeping less than the text promised breaks no promise, but the
> next version of these texts should say the number the server keeps.

### `terms` — Termos e Política de Privacidade

Você leu e aceita os Termos de uso e a Política de Privacidade. Sem isso, não tem conta.

Ao desligar: Sem os Termos, a conta não funciona. Pra sair de vez, apague a conta aqui embaixo.

- base legal: `contract_art7`
- proof: `13e66f1becbe571598de6c4595e6ac4a46526e43b2ebf257249957a034139705`

### `read_heart_rate` — Ler sua batida

Ler a batida que seu relógio ou sensor grava, só na janela do evento, e achar seus momentos. Sem isso, não tem noite.

Ao desligar: Nenhuma noite nova é gravada. As que já estão aqui continuam.

- base legal: `consent_art11`
- proof: `d5f40e2ff642faac427fb150910ad585db0afcfde57c43c0695ee63570e50d24`

### `keep_night` — Guardar a noite

Guardar a noite (a série de batidas e os momentos) na sua coleção, no servidor da TumTum. Sem isso, a noite fica só no seu celular.

Ao desligar: Nenhuma noite nova sobe pra TumTum. Pra apagar uma que já subiu, abre a noite e toca em Apagar esta noite.

- base legal: `consent_art11`
- proof: `e02a9dc1c1b2358024ecc28ba387fc3bac1d190bf3935f271be2a22a373b1198`

### `crowd_stats` — Entrar na galera

Sua noite, sem seu nome, entra na conta coletiva do evento (“A galera”). Só aparece em faixas, tipo “25+”, e só quando tem gente suficiente.

Ao desligar: Suas noites deixam de contar na galera dos eventos.

- base legal: `consent_art11`
- proof: `ef5b277ec583aee878a5e6af3db893f9309180b6db4e62f13d94f48cc138e9e8`

### `artist_compare` — Comparar com o artista

Quando existir: comparar sua batida com a do artista ou do atleta que topar mostrar a dele.

Ao desligar: Sem comparação com artista ou atleta.

- base legal: `consent_art11`
- proof: `ed687fc50477d7e9c29e0c8930c422ff9ccd5ef2e3bbb46f0a5ccf36bf3a379c`

### `improve_detection` — Melhorar o detector

Usar suas noites pra melhorar o jeito como a TumTum acha os momentos.

Ao desligar: Suas noites não são usadas pra melhorar o detector.

- base legal: `consent_art11`
- proof: `c6557237716ce688034584a2b3853b702fba2edbef7235a714ca8affac99b70c`

### `marketing` — Novidades por e-mail

Receber e-mails da TumTum sobre eventos e novidades.

Ao desligar: A gente para de mandar e-mail de novidade.

- base legal: `consent_art7`
- proof: `9df4d1022b462b8cbdc11307cc6ace9d6512451a388770e41bcc0e5873839bb7`
