# Plano de resposta a incidente de segurança com dado pessoal

26/09/2026. Nasceu da auditoria LGPD (`RELATORIO-AUDITORIA-LGPD.md`, item
K2, achado AL-8). Base: art. 48 da LGPD e o Regulamento de Comunicação de
Incidente de Segurança da ANPD (Resolução CD/ANPD nº 15/2024).

**A regra em uma frase:** ao saber de um incidente que pode expor batimento,
e-mail, senha ou conta de alguém, **contém primeiro, avalia depois, e comunica
à ANPD e às pessoas em até 3 dias úteis** — batimento é dado sensível, então a
resposta padrão é comunicar.

## 1. Papéis

| Papel | Quem | O que faz |
|---|---|---|
| **Responsável pelo incidente** | Felipe Zanucci (controlador e encarregado) | Decide contenção, classificação e comunicação; assina a comunicação |
| **Suplente** | [PENDENTE — suplente] | Assume se Felipe não responder em 2 horas |
| Apoio técnico | Quem estiver trabalhando no código (hoje, sessões do Claude Code a pedido do Felipe) | Executa a contenção, levanta o alcance nos logs. **Não decide comunicar** |
| Jurídico | [PENDENTE — Felipe: advogado de referência] | Revisa a comunicação antes do envio, se houver tempo dentro do prazo |

Contatos de fornecedor para ter à mão: suporte do Railway, do Vercel, do
Resend e do Sentry (pelo painel de cada um).

## 2. Detecção — de onde vem o aviso

- **Sentry**: pico de erros de autenticação (401/403) ou de exceções em
  `/api/health`, `/api/experience`, `/api/cards`, `/api/users/me/export`.
- **Logs**: `access_log` (toda requisição, com IP) e `data_access_log` (quem
  leu dado de saúde de quem). Sinais: um IP ou uma conta lendo noites de
  muitas pessoas; `actor_user_id ≠ subject_user_id` fora de admin; muitas
  exportações seguidas.
- **Reuso de refresh token**: o servidor já revoga a família inteira
  (`revoke_reason = 'reuse'`). Muitos casos juntos são sinal de roubo de
  token.
- **Denúncia**: e-mail para oi@tumtum.cc, mensagem em rede social, pedido de
  titular, pesquisador de segurança, alerta de secret scanning do GitHub,
  job `privacy-scan` vermelho num commit já publicado.
- **Perda física**: celular emprestado do piloto perdido ou roubado; ZIP de
  operador mandado para o destino errado.

Quem notar qualquer um desses **avisa o Felipe na hora** e anota a data e
hora em que soube: é dela que o prazo de 3 dias úteis conta.

## 3. Classificação

| Nível | Exemplo | Comunicar? |
|---|---|---|
| **0 — Suspeita** | Pico de 401 sem acesso bem-sucedido; alerta falso | Não. Registrar |
| **1 — Sem risco relevante** | E-mail de uma pessoa visto por outra por engano, sem batimento, contido na hora | Avaliar; registrar sempre |
| **2 — Risco relevante** | Qualquer exposição de **batimento**, noite, card não publicado, senha/hash, token de sessão, data de nascimento; `SECRET_KEY` vazada; acesso ao banco ou a um backup; ZIP de operador perdido; muitas pessoas afetadas | **Sim**: ANPD e titulares, 3 dias úteis |

Na dúvida entre 1 e 2, é 2. A Resolução lista dado sensível e dado de
autenticação entre os critérios de risco relevante; na TumTum, quase todo
incidente com conta toca um dos dois.

## 4. Contenção — o que fazer primeiro

Na ordem, parando quando o vazamento parou.

**a. Revogar a `SECRET_KEY`** (todo access token assinado com a chave antiga
deixa de valer no próximo deploy):

1. Gerar uma nova: `python -c "import secrets; print(secrets.token_urlsafe(48))"`.
2. Railway → serviço do backend → *Variables* → `SECRET_KEY` → colar →
   o Railway faz o redeploy sozinho.
3. Nunca escrever a chave em arquivo, chat ou commit.

**b. Revogar todos os refresh tokens** (eles não dependem da `SECRET_KEY`;
são aleatórios e guardados como hash). Todo mundo terá de entrar de novo:

```sql
UPDATE refresh_tokens SET revoked_at = now(), revoke_reason = 'incident'
WHERE revoked_at IS NULL;
```

(`railway run` a partir de `backend/`, ou o console de dados do Railway.)
Para uma conta só: acrescentar `AND user_id = '<id>'`.

**c. Derrubar o endpoint:**

- **Tudo fora do ar:** Railway → serviço do backend → *Settings* → remover o
  domínio público, ou *Deployments* → *Remove* no deploy ativo. O app e o site
  passam a dizer que não conseguem falar com o servidor; nada se perde no
  celular.
- **Um endpoint só:** um commit que remove a rota (ou devolve 503) em `main`;
  o Railway publica em minutos. O PR entra com a descrição "incidente",
  e Felipe faz o merge.

**d. Trocar os outros segredos se houver chance de exposição:** senha do
Postgres (Railway → Postgres → *Variables* / regenerar credenciais, e
atualizar `DATABASE_URL` no backend), `RESEND_API_KEY` (painel do Resend),
`SENTRY_DSN`, `API_FOOTBALL_KEY`, e os segredos do GitHub Actions
(`TUMTUM_UPLOAD_KEYSTORE_*`).

**e. Preservar evidência antes de limpar:** exportar as linhas relevantes do
`access_log` e do `data_access_log` do período (fora do repositório) antes do
ciclo de manutenção apagá-las.

## 5. Avaliação

Responder, por escrito, no registro do incidente (seção 7):

1. O que aconteceu, quando começou, quando foi descoberto, quando foi contido.
2. **Quais dados**: batimento (série, momentos, cards), e-mail, nome, data de
   nascimento, hash de senha, tokens, IP.
3. **Quantas pessoas e quem**: a partir do `data_access_log`
   (`subject_user_id` distintos lidos pelo ator suspeito) e do `access_log`.
4. Se havia criptografia ou pseudonimização que torne o dado ininteligível
   (hoje: senhas e tokens em hash; banco do celular em SQLCipher; banco do
   servidor [PENDENTE — criptografia em repouso do Railway]).
5. Consequências prováveis para as pessoas (exposição de presença num evento,
   de dado de saúde, uso de conta).

## 6. Comunicação — até 3 dias úteis

Contados da ciência do incidente. Se alguma informação ainda não existir, a
comunicação sai no prazo mesmo assim, dizendo o que falta, e é completada
depois (a Resolução permite comunicação complementar).

**À ANPD:** pelo formulário de comunicação de incidente de segurança no site
da ANPD (gov.br/anpd), assinado pelo encarregado. Conteúdo mínimo (art. 48,
§1º): natureza dos dados, titulares envolvidos, medidas técnicas e de
segurança, riscos, motivo de eventual demora, medidas adotadas para reverter
ou mitigar.

**Aos titulares:** e-mail individual (Resend) a cada pessoa afetada, em
linguagem simples, e aviso no site e no app se forem muitas. Tom: o da marca
em privacidade — quieto e cuidadoso, sem piada e sem minimizar.

### Modelo — e-mail aos titulares

> **Assunto:** TumTum — um problema de segurança que envolve seus dados
>
> Oi, [nome].
>
> Em [data], descobrimos que [o que aconteceu, numa frase: "uma pessoa sem
> autorização conseguiu ler dados de algumas contas da TumTum"]. Isso
> aconteceu entre [início] e [fim], e já foi interrompido.
>
> **O que foi exposto da sua conta:** [lista: por exemplo, "as noites
> guardadas em [evento], com os batimentos e os momentos", "seu e-mail e seu
> nome"]. **O que não foi:** [por exemplo, "sua senha, que guardamos só de
> forma embaralhada (hash)"].
>
> Batimento cardíaco é um dado sensível, e sabemos que isso é sério. O que já
> fizemos: [medidas: "encerramos o acesso", "desconectamos todas as sessões",
> "corrigimos a falha"].
>
> **O que recomendamos que você faça:** [por exemplo, "entrar de novo no app
> — todas as sessões foram encerradas", "trocar a senha se você usa a mesma
> em outro lugar"].
>
> Você pode apagar qualquer noite ou a conta inteira em Configurações, baixar
> todos os seus dados em tumtum.cc/perfil, ou falar diretamente com o
> encarregado pelos seus dados, Felipe Zanucci, em oi@tumtum.cc (assunto
> "Privacidade"). Comunicamos também a Autoridade Nacional de Proteção de
> Dados (ANPD).
>
> Pedimos desculpas.
>
> Felipe Zanucci — TumTum
> [PENDENTE — razão social e CNPJ]

### Modelo — resumo para o formulário da ANPD

> **Controlador:** [PENDENTE — razão social e CNPJ]. **Encarregado:** Felipe
> Zanucci, oi@tumtum.cc.
> **Natureza do incidente:** [confidencialidade / integridade /
> disponibilidade] — [descrição].
> **Datas:** ocorrência [ ], ciência [ ], contenção [ ].
> **Dados afetados:** [categorias], incluindo dado pessoal sensível (saúde:
> frequência cardíaca por instante, associada a evento).
> **Titulares:** [número] pessoas, maiores de 18 anos (a plataforma recusa
> cadastro abaixo de 18).
> **Medidas de segurança existentes:** HTTPS; senhas e tokens em hash;
> consentimento por finalidade; registro de acesso a dado de saúde; retenção
> de 30 dias para a série bruta; [demais].
> **Riscos:** [exposição de dado de saúde e de presença em evento; uso
> indevido de conta].
> **Medidas adotadas:** [revogação de chaves e sessões; correção; comunicação
> aos titulares em dd/mm].
> **Motivo de eventual atraso:** [ ].

## 7. Registro do incidente

**Todo incidente é registrado, comunicado ou não**, e o registro é guardado
por **no mínimo 5 anos** (Resolução CD/ANPD nº 15/2024). Onde: [PENDENTE —
Felipe: pasta fora do repositório, a mesma dos termos do piloto]. Campos:

| Campo | |
|---|---|
| Número | INC-AAAA-NN |
| Ciência (data e hora) e quem avisou | |
| Descrição | |
| Dados e titulares afetados (quantidade, categorias — sem lista de nomes aqui) | |
| Nível (0/1/2) e por quê | |
| Contenção: o quê, quando, quem | |
| Comunicou à ANPD? Quando, protocolo | |
| Comunicou aos titulares? Quando, como | |
| Causa raiz | |
| Correções e prazo | |

No `docs/decision-log.md` entra uma entrada datada, **sem nenhum dado
pessoal**: o que falhou, o que custou, o que mudou.

## 8. Pós-incidente

Em até duas semanas: causa raiz escrita; correção com teste que prova o
conserto; revisão do `docs/ripd.md` (o risco virou fato — a probabilidade
muda) e do `docs/ropa.md` se um operador esteve envolvido; checagem de que
todos os segredos trocados estão só no Railway/GitHub e em nenhum arquivo;
e, se a falha era de uma classe, procurar os irmãos dela no código.

## 9. Ensaio

Uma vez antes do piloto: executar a seção 4 (a, b) num ambiente de teste,
cronometrar, e corrigir este plano onde ele estiver errado.
[PENDENTE — Felipe: data do ensaio].
