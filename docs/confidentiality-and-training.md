# Confidencialidade, treinamento e acessos de quem entra

27/09/2026. É o item 22: hoje só o Felipe tem acesso a dado pessoal, e o
`docs/dpo.md` marca o critério *Treinamento* como `[PENDENTE]`. Este documento
existe para o dia em que houver **outra pessoa** — sócio, estagiário,
desenvolvedor contratado, operador do piloto com celular na mão — e decide
três coisas **antes** de ela receber o primeiro acesso:

- **A.** o termo que ela assina;
- **B.** os 45 minutos de treinamento que ela faz;
- **C.** como o acesso entra, muda e sai, sistema por sistema.

Item 84 do `docs/decision-log.md`. A lista de sistemas é a do
`docs/security-baseline.md`; se um sistema entrar lá, entra no §C daqui.

**A ordem não muda:** termo assinado → treinamento feito → acesso concedido.
Nenhum acesso "provisório enquanto o termo não chega".

---

## A. Termo de Confidencialidade e Proteção de Dados

`[PENDENTE — revisão jurídica]` Modelo, não contrato pronto. Um advogado
revisa antes da primeira assinatura, junto com o termo do piloto (item 77).
Se quem assina é uma **empresa** que trata dado em nome da TumTum (uma
agência, um desenvolvedor PJ com acesso ao banco), ela é **operadora**: além
deste termo, precisa de um contrato com as onze cláusulas mínimas do
`docs/dpa-checklist.md`.

> **TERMO DE CONFIDENCIALIDADE E PROTEÇÃO DE DADOS PESSOAIS**
>
> **Partes.**
> **TUMTUM** — [PENDENTE — Felipe: razão social], CNPJ [PENDENTE], com sede
> em [PENDENTE], controladora dos dados pessoais tratados no produto TumTum,
> neste ato representada por [nome], doravante **TUMTUM**; e
> **[Nome completo]**, CPF [___], [função: colaborador(a) / prestador(a) de
> serviço / sócio(a) / operador(a) do piloto], doravante **COLABORADOR(A)**.
>
> **1. Objeto.** Este termo regula o sigilo das informações e o tratamento
> dos dados pessoais a que o(a) COLABORADOR(A) tiver acesso em razão de
> [descrever a atividade: desenvolvimento, operação de evento, suporte],
> durante e depois da relação com a TUMTUM.
>
> **2. Informação confidencial.** É confidencial toda informação não pública
> a que o(a) COLABORADOR(A) tiver acesso: dados pessoais de usuários e
> participantes; credenciais, chaves e segredos de sistemas; documentos
> internos fora do repositório público; negociações com fornecedores,
> parceiros, artistas e clubes; resultados de testes e do piloto. O fato de o
> código-fonte ser público não torna públicos os dados que ele trata.
>
> **3. Dados sensíveis de saúde.** O(A) COLABORADOR(A) declara saber que o
> batimento cardíaco, e tudo o que dele deriva enquanto ligado a uma pessoa
> (curva, momento, pico, qualidade da medição), é **dado pessoal sensível
> referente à saúde** (Lei 13.709/2018, art. 5º, II, e art. 11), e que a sua
> exposição pode causar dano relevante ao titular.
>
> **4. Instruções documentadas.** O(A) COLABORADOR(A) trata dados pessoais
> **somente** para a atividade do item 1 e segundo as instruções escritas da
> TUMTUM — hoje `CLAUDE.md` (regras de produto), `docs/ropa.md`,
> `docs/incident-response.md` e este termo. Nenhum uso próprio, nenhuma
> finalidade nova sem instrução escrita.
>
> **5. Acesso mínimo.** O acesso é pessoal e intransferível, concedido na
> menor medida necessária e listado no Anexo I. O(A) COLABORADOR(A) usa
> autenticação em dois fatores em todos os sistemas do Anexo I, não
> compartilha senha, token ou sessão, e não tenta acessar o que não lhe foi
> concedido — inclusive dados de uma pessoa que ele(a) conheça.
>
> **6. Proibições.** Sem autorização prévia e escrita da TUMTUM, é proibido:
> (a) copiar, baixar ou exportar dados pessoais — por dump, planilha,
> print, foto de tela ou arquivo de operador — para qualquer dispositivo,
> nuvem ou conta que não seja da TUMTUM;
> (b) enviar dados pessoais por e-mail pessoal, mensageiro ou rede social;
> (c) **inserir dados pessoais reais — linhas do banco, exportações,
> registros de log, prints com nome, e-mail ou batimento — em ferramentas de
> inteligência artificial** (assistentes de chat, assistentes de código,
> tradutores e similares), mesmo para depurar um problema;
> (d) colocar dado pessoal ou segredo em repositório de código, issue, pull
> request ou documento compartilhado;
> (e) repassar dado pessoal a clube, artista, anunciante ou qualquer
> terceiro.
>
> **7. Incidentes.** O(A) COLABORADOR(A) comunica à TUMTUM, pelo canal do
> encarregado (oi@tumtum.cc, assunto "Privacidade") **e** diretamente a
> [nome do responsável], **em até 24 horas** de quando souber, qualquer
> incidente ou suspeita: acesso indevido, perda de dispositivo com dados,
> envio ao destinatário errado, credencial exposta, dado real colado onde
> não devia. Informa a data e a hora em que soube, não apaga evidências e não
> comunica titulares ou autoridades por conta própria.
>
> **8. Devolução e apagamento.** Ao fim da relação, ou a pedido da TUMTUM a
> qualquer tempo, o(a) COLABORADOR(A) devolve os equipamentos da TUMTUM,
> apaga todas as cópias de dados pessoais e de informação confidencial que
> estejam sob seu controle, e declara por escrito que o fez (Anexo II).
>
> **9. Vigência.** O dever de sigilo vale durante a relação e **por prazo
> indeterminado depois do seu término** quanto a dados pessoais, e por
> [PENDENTE — jurídico: 5 anos?] quanto à demais informação confidencial.
>
> **10. Descumprimento.** O descumprimento sujeita o(a) COLABORADOR(A) à
> responsabilidade civil pelos danos causados, sem prejuízo das medidas
> trabalhistas ou contratuais cabíveis e da responsabilidade prevista em lei.
> [PENDENTE — revisão jurídica: redação conforme o vínculo — CLT, estágio,
> PJ, sócio.]
>
> **11. Foro.** Fica eleito o foro da comarca de [PENDENTE — jurídico: São
> Paulo/SP], com renúncia a qualquer outro.
>
> [Cidade], ____/____/________
>
> TUMTUM: ______________________ COLABORADOR(A): ______________________
>
> Testemunhas: ______________________ ______________________
>
> **Anexo I — Acessos concedidos** (uma linha por sistema do §C; atualizado
> a cada mudança de função)
>
> | Sistema | Papel | Concedido em | Por quem | Revogado em |
> |---|---|---|---|---|
> | | | | | |
>
> **Anexo II — Declaração de devolução e apagamento**
>
> Declaro que em ____/____/________ devolvi os equipamentos da TUMTUM,
> apaguei todas as cópias de dados pessoais e de informação confidencial sob
> meu controle, e não mantenho nenhum acesso aos sistemas do Anexo I.
> Assinatura: ______________________

**Onde vive o termo assinado:** fora do repositório, na mesma pasta dos termos
do piloto `[PENDENTE — Felipe]`. Aqui só o registro de que existe (tabela do
§B.3 tem a coluna).

---

## B. Treinamento — 45 minutos

Feito por quem é o encarregado (hoje o Felipe), **antes** do primeiro acesso,
e repetido **uma vez por ano** e sempre que uma regra desta lista mudar. Em
pessoa ou chamada, com o repositório aberto: cada tópico aponta o documento
que a pessoa vai reler sozinha. Para o operador do piloto que só segura um
celular num evento, os tópicos 1, 3, 5, 7 e 8 bastam (≈ 20 min).

### B.1 Roteiro

| Min | Tópico | O que a pessoa precisa sair sabendo | Onde está | Pergunta de conferência |
|---|---|---|---|---|
| 0–3 | Abertura | Por que isso vem antes do acesso: a TumTum trata **batimento**, e a marca só existe se as pessoas confiarem nela com ele | Este documento | — |
| 3–9 | **1. O que é dado sensível aqui** | Batimento é dado de saúde (LGPD art. 11), e o que deriva dele também, enquanto ligado a alguém: curva, momento, pico, qualidade. Um momento diz **onde a pessoa estava e a que hora** (evento + horário). Também pessoal: e-mail, nome, data de nascimento, IP no `access_log`, foto do perfil | `docs/ropa.md` (cabeçalho e §1) | "Um card com 142 bpm às 22h12 no show X, sem nome: é dado sensível?" (Sim, se leva à pessoa — e o card leva) |
| 9–16 | **2. As sete finalidades** | `terms` e `read_heart_rate` (o que o produto precisa, cada um com seu toque); `keep_night`, `crowd_stats`, `artist_compare`, `improve_detection`, `marketing` — **desligados até a pessoa ligar**. Cada uma é uma linha em `consents`, revogável em Configurações → Privacidade. Nada sobe sem *Guardar minha noite na TumTum* com `keep_night` | `CLAUDE.md`, regras de produto; `docs/consent-texts.md` | "Posso usar as noites guardadas para testar um detector novo?" (Só as de quem deu `improve_detection`) |
| 16–21 | **3. O que nunca sai** | Dado individual não vai para clube, artista, anunciante nem parceiro — só para operadores sob contrato (Railway, Vercel, Resend, Sentry). Nenhum dump de produção em computador, Drive ou repositório. R-R e movimento nunca sobem | `docs/ropa.md` §2; `docs/backups.md` | "Um clube pede a lista de quem foi ao jogo com o batimento no gol." (Não. Nem agregado, sem o teste de *A galera*) |
| 21–26 | **4. Como se apaga** | A pessoa apaga uma noite ou a conta inteira pelo app ou pelo site; a exclusão leva tudo e deixa só uma linha sem identificador no `deletion_log`. A série bruta sai **7 dias** depois da análise; revogar `keep_night` apaga as noites **24 h** depois; imagem de card fica 7 dias no Redis; dados do piloto saem **30 dias** depois do evento. Backup guarda o que foi apagado até expirar — por isso não se restaura backup para "recuperar" | `docs/data-retention-policy.md`; `docs/pilot-data-retention.md`; `docs/backups.md` | "Alguém pede por e-mail para apagar a conta. Você apaga no banco?" (Não: encaminha ao encarregado no mesmo dia — §8) |
| 26–32 | **5. Incidentes** | Qualquer suspeita — celular do piloto perdido, ZIP de operador mandado errado, senha colada num chat — é avisada **na hora** ao Felipe, com a hora em que se soube: o prazo de 3 dias úteis à ANPD conta dela. O termo dá **24 h** no máximo; o certo é na hora. Não apagar evidência, não avisar titular por conta própria. Suspeita descartada também é registrada | `docs/incident-response.md` §2–§3; `docs/incident-register.md` | "Você mandou um ZIP de noite para o grupo errado e apagou em 1 minuto. Precisa avisar?" (Sim) |
| 32–37 | **6. Ferramentas de IA e dados reais** | Nunca colar em ChatGPT, Claude, Copilot, tradutor ou similar: linha do banco, exportação, log, print com nome/e-mail/bpm, ZIP de operador. Nem para depurar. As sessões do Claude Code deste repositório trabalham só com dado sintético (`@exemplo.com`), e numa investigação a consulta roda do lado do Felipe — volta só contagem | `docs/incident-response.md` §1; `docs/ropa.md` §2; job `privacy-scan` | "O servidor deu erro numa noite; posso colar o JSON da noite no assistente para ele achar o bug?" (Não. Descreve o formato, ou gera um parecido sintético) |
| 37–41 | **7. Telas de bloqueio e capturas** | Print de admin, de feed ou de noite de outra pessoa é **cópia de dado pessoal**: não se tira, não se manda. Celular com app de operador ou noites de participantes tem bloqueio por senha/biometria e nunca fica desbloqueado na mão de outro. Compartilhar tela em chamada: fechar o admin antes. A notificação do app é neutra de propósito (*"Sua noite abriu"*, sem bpm, sem evento) — não se "melhora" isso | `docs/ropa.md` §3 (no celular) | "Posso postar um print do feed do evento para mostrar que funcionou?" (Só sem nenhuma pessoa identificável, e com o Felipe de acordo) |
| 41–43 | **8. O canal do encarregado** | Titular fala com a TumTum por oi@tumtum.cc, assunto "Privacidade", pelo app (Configurações → Privacidade) ou pelo site (Perfil → Pedidos). Prazo de **15 dias**. Quem recebe um pedido em qualquer lugar — DM, evento, WhatsApp — **não responde com dado**: encaminha ao encarregado no mesmo dia, com a data de chegada | `docs/dpo.md` | "Um participante te pergunta no evento que dados a TumTum tem dele." (Aponta o canal e avisa o Felipe; não abre o admin) |
| 43–45 | Fechamento | As oito perguntas de conferência, de novo, em voz alta; assinatura no registro abaixo | §B.3 | — |

### B.2 Depois do treinamento

- A pessoa relê sozinha, em até uma semana: `CLAUDE.md` (Regras de produto),
  `docs/ropa.md` §2–§3, `docs/incident-response.md` §1–§4.
- Só então o acesso do §C é concedido, e só o do Anexo I do termo.

### B.3 Registro de presença

Uma linha por pessoa por sessão. Fica aqui (nome e função são o mínimo; nada
de CPF, e-mail ou telefone neste arquivo — ele vive num repositório público).

| Data | Nome | Função | Versão do roteiro | Tópicos feitos (1–8) | Termo assinado em | Aplicado por | Próxima reciclagem |
|---|---|---|---|---|---|---|---|
| | | | 27/09/2026 | | | | |

---

## C. Entrada, mudança e saída de acesso (joiner / mover / leaver)

### C.1 Entrada (joiner)

1. Definir a função e, dela, **o menor conjunto de acessos** — linha a linha
   na tabela do §C.4. Escrever no Anexo I do termo.
2. Termo assinado (§A). Se PJ que trata dado: contrato de operador também.
3. Treinamento (§B), linha no registro de presença.
4. A pessoa cria **a própria conta** em cada sistema e liga o 2FA; só então
   é convidada. Nunca receber a senha de uma conta existente.
5. Conceder o acesso; conferir no painel que o papel é o combinado.
6. Linha no decision log: quem entrou, com que função, em que data (nome e
   função, nada mais).

### C.2 Mudança de função (mover)

1. Novo conjunto de acessos pela tabela do §C.4 — o que a nova função **não**
   precisa sai no mesmo dia, antes de entrar o novo.
2. Anexo I atualizado. Se a nova função toca dado que a anterior não tocava,
   o treinamento é refeito nos tópicos que faltavam.

### C.3 Saída (leaver)

No **último dia**, não depois:

1. Revogar todos os acessos do Anexo I, pela tabela do §C.4.
2. **Rotacionar todo segredo que a pessoa podia ver** (`docs/security-baseline.md`
   §9) — quem via *Variables* do Railway viu o `SECRET_KEY`.
3. Recolher equipamentos da TumTum (celular de piloto, sensor); apagar dele
   noites e exports.
4. Anexo II do termo assinado (declaração de apagamento).
5. Linha no decision log: saída, data, e "acessos revogados e segredos
   rotacionados".

### C.4 Acessos por sistema

A lista é a do `docs/security-baseline.md`. "Menor papel" é o ponto de
partida — mais que isso é decisão escrita no Anexo I.

| Sistema | Menor papel que costuma resolver | Conceder (joiner) | Revogar (leaver) | Segredo a rotacionar na saída |
|---|---|---|---|---|
| **GitHub** | *Write* no repositório (nunca *Admin*); o ruleset de `main` vale para ela também | Repositório → *Settings* → *Collaborators* → *Add people* | Mesma tela → *Remove*; conferir forks privados e tokens que ela tenha criado para CI | Os quatro secrets da upload key, só se ela era *Admin* |
| **Railway** | Membro que vê logs e deploys; *Variables* só se for mexer no backend | Workspace/projeto → *Members* → convidar *(conferir)* | Mesma tela → remover | `SECRET_KEY`, `RESEND_API_KEY`, `API_FOOTBALL_KEY`, `SETLIST_FM_API_KEY`, senha do Postgres se ela conectou direto |
| **Vercel** | Membro do time com o papel mais baixo que faz deploy | *Team Settings* → *Members* → *Invite* | Mesma tela → remover | Tokens de deploy que ela criou |
| **Resend** | Nenhum, em geral: só o backend manda e-mail | *Settings* → *Team* | Mesma tela → remover | `RESEND_API_KEY` se ela a viu |
| **Sentry** | *Member* no projeto do backend | *Organization Settings* → *Members* → *Invite* | Mesma tela → remover | DSN, se ela o copiou para algum lugar |
| **Google Play Console** | Permissões só do que faz (ex.: ver versões, responder avaliações) — nunca *Administrador* | *Usuários e permissões* → *Convidar novos usuários* | Mesma tela → remover o usuário | Upload key, só se ela teve o `.jks` |
| **DNS (registrador / Cloudflare)** | Nenhum. Só o Felipe edita o DNS | — | Conferir que não há delegação | — |
| **oi@tumtum.cc** | Nenhum, enquanto o encarregado for o Felipe. Se ela for o encarregado ou o suplente: acesso ao grupo, nunca à senha da caixa | admin.google.com → *Grupos* → membro *(se for grupo)* | Remover do grupo; conferir encaminhamentos e filtros (`docs/security-baseline.md` §8) | — |
| **Admin da TumTum** (`ADMIN_EMAILS`) | Só se opera eventos. Lê pedidos de titular e o `data_access_log` registra cada leitura | Railway → backend → *Variables* → `ADMIN_EMAILS` → acrescentar o e-mail da conta dela → deploy | Tirar o e-mail da variável → deploy | — |
| **Portais Snap / TikTok / Meta** | Nenhum, em geral | Pelo portal de cada um, como membro do app | Remover do app no portal | *client secret* / *app secret*, se ela os viu |
| **Celular de operador do piloto** | O app com a chave *Cadastrar eventos pelo celular* ligada só se ela cadastra eventos | Entregar com bloqueio de tela, conta própria no app | Recolher; sair da conta; apagar noites e ZIPs de export | — |
| **Pasta de conformidade** (termos, DPAs, evidências) | Nenhum, salvo o encarregado ou o suplente | Compartilhar a pasta com a conta dela | Remover o compartilhamento | — |
