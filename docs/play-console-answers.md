# Respostas do Play Console — versionadas

26/09/2026. Até esta data as respostas estavam espalhadas em
`docs/handoff-2026-09-18.md` §5, `docs/handoff-2026-09-24.md` §2 e no item 59
do `docs/decision-log.md`. Aqui ficam juntas, numa tabela, com a data de cada
versão. **Toda mudança no Console entra aqui no mesmo dia**, com a data, e a
linha antiga fica riscada em vez de apagada.

App: **`cc.tumtum.app`**, nome **TumTum**. Conta de desenvolvedor pessoal,
verificada (o e-mail fica fora do repositório). Faixa: **teste interno**.
Nada foi enviado para revisão do Google até 26/09: *Enviar app para revisão*
fica desativado enquanto o app é rascunho de teste interno, e toda mudança
espera em *Visão geral da publicação*.

## Respostas

| Formulário | Resposta | Desde | Por quê | Revisar |
|---|---|---|---|---|
| **Dados de login** (para o revisor) | Conta criada em tumtum.cc/signup, instruções em inglês com menos de 500 caracteres. Login: `[conta de revisão — fora do repositório]` | 18/09 | O celular não criava uma segunda conta até o #62; o site cadastra no mesmo servidor | **Sim (26/09):** desde 24/09 o cadastro pede um código de 6 dígitos no e-mail, e desde 26/09 pede data de nascimento e aceite dos Termos, e a conta precisa dos consentimentos para gravar. Conferir que a conta de revisão já passou por esse portão e que as instruções dizem quais telas o revisor vai ver |
| **Classificação do conteúdo** | Categoria *Todos os outros tipos*. Conteúdo gerado pelo usuário **compartilhado**; não é a fonte principal. Bloquear **sim**, denunciar **sim**, moderação de chat **não** (não há chat), interação só com amigos convidados **não**, compras digitais **não** | 18/09; corrigida 24/09 | "Social" dispararia o questionário mais pesado; declarar conteúdo compartilhado é verdade. Bloquear/denunciar existem desde 22/09. Compras digitais estava *sim* por engano em 18/09 | — |
| **Público-alvo** | **18+**, com a caixa de restringir menores marcada | 18/09 | Menos superfície de política para um app que lê batimento | Agora é também verdade no produto: o servidor recusa cadastro abaixo de 18 (26/09) |
| **Segurança dos dados** | Nome, e-mail, IDs de usuário (obrigatórios; funcionalidade e gestão de conta; **não efêmeros**). **Informações de saúde** (opcional; funcionalidade). *Atividade no app → Interações* (reações, denúncias, bloqueios: opcional, não compartilhado, não efêmero; funcionalidade e segurança/prevenção a fraude). Criptografado em trânsito. Exclusão **no app**, URL `/apagar-conta`. Exclusão parcial: **não**. Fotos e vídeos não declarados (nunca chegam ao servidor) | 18/09; corrigida 24/09 | Conferido no código: nenhum ID de aparelho enviado, nenhum SDK de analytics, nenhum compartilhamento automático, avatar nunca sai do celular. IDs de usuário estavam *efêmeros* por engano em 18/09 | **Sim (26/09):** (1) **exclusão parcial passa a ser sim** — "Apagar esta noite" existe; (2) **data de nascimento** é coletada: declarar em *Informações pessoais → Outras informações* (obrigatória, funcionalidade/gestão de conta); (3) **endereço IP** é guardado 180 dias no `access_log`: conferir se o formulário pede declará-lo e em qual tipo; (4) o upload da noite é **opcional** e por ato — a resposta "opcional" para saúde fica mais verdadeira; (5) "baixar meus dados" existe — não é pergunta do formulário, mas vale para a política |
| **Apps de saúde** | **Atividade e condicionamento físicos** | 18/09 | A única categoria que cobre ler batimento. Dizer "sem recursos de saúde" pedindo Health Connect é a contradição que reprova na revisão. Categorias de medicina implicam diagnóstico | **Revisar — ver abaixo** |
| **Anúncios / ID de publicidade** | **Não** | 18/09 | Dependências são AndroidX e coroutines; sem anúncios do Play Services, sem Firebase; `AD_ID` fora do manifesto | — |
| **Financeiro / Governo / Notícias** | **Não** | 18/09 | Compras futuras no app são cobradas pelo Google Play e não fazem dele um app financeiro | — |
| **Categoria da loja** | **Entretenimento**, tags Entretenimento/Esportes/Eventos | 18/09 | Saúde e fitness contradiria o NEVER do manual; Social traz deveres de bloquear/denunciar | — |
| **Tablet / Chromebook / XR** | **Vazio** | 18/09 | O app é um celular no bolso num show. Arte de celular ampliada deturparia um layout que o Compose não produz em tablet | — |
| **Política de privacidade (URL)** | `https://tumtum.cc/privacidade` | 18/09 | A página existe desde 18/09 | Conferir que o texto publicado é o de 26/09 (base legal, dado sensível, controlador, encarregado, operadores, prazos, 18+) |

## Revisar: "Apps de saúde → Atividade e condicionamento físicos"

**O conflito.** O manual de marca (v0.4) lista como NEVER *"Making TumTum
look like healthtech, fitness or wellness"*, e o `CLAUDE.md` evita
vocabulário de fitness em tudo o que a pessoa lê. A resposta ao Google diz o
contrário: que o app é de *atividade e condicionamento físico*. A auditoria
(item I3, J1) apontou a contradição. Ela não aparece para o fã hoje — a
categoria da loja é Entretenimento —, mas é uma declaração formal ao Google
sobre a natureza do app, e pode:

- pesar numa leitura de que a TumTum trata dado para fins de saúde, o que
  muda a expectativa sobre a finalidade (art. 6º, I, e art. 11 da LGPD) e a
  pergunta sobre dispositivo médico;
- contradizer a política de privacidade, que diz que a TumTum não é serviço
  de saúde, se alguém comparar as duas.

**Por que foi respondido assim** continua valendo: pedir a permissão de
leitura de batimento do Health Connect e declarar "sem recursos de saúde" é a
contradição que reprova na revisão.

**A pergunta para a revisão jurídica** [PENDENTE — Felipe]:

> O app lê frequência cardíaca do Health Connect exclusivamente para um fim
> de entretenimento (ligar o batimento de um show ou jogo aos momentos do
> evento), sem interpretação de saúde. No formulário "Apps de saúde" do
> Google Play, qual declaração é a mais exata e a menos arriscada: (a)
> "Atividade e condicionamento físicos", (b) outra categoria da lista, ou (c)
> outra forma de descrever o uso do dado de saúde? A escolha tem efeito sobre
> a caracterização do tratamento perante a LGPD ou a ANVISA (software como
> dispositivo médico)? E, se mantida (a), basta que a política de privacidade
> e o texto da permissão digam claramente que não é um app de saúde?

Até a resposta: **não mudar** a declaração (mudar sem saber é pior), e não
repetir "condicionamento físico" em nenhum texto que a pessoa lê.
