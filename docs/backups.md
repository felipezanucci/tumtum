# Backups do banco

26/09/2026. A auditoria LGPD (`RELATORIO-AUDITORIA-LGPD.md`, itens C2 e D5)
não conseguiu ver nada sobre backups a partir do repositório: eles vivem no
Railway. Até esta data **ninguém conferiu** se existem, por quanto tempo
ficam, se são criptografados e onde estão. Isso importa porque um backup
guarda o que a pessoa já apagou: "apagar a conta" só é verdade depois que o
último backup com ela expira.

## O que conferir no Railway

Railway → projeto TumTum → serviço **Postgres** (e depois **Redis**):

| # | Pergunta | Onde olhar | Resposta | Conferido em |
|---|---|---|---|---|
| 1 | Existem snapshots/backups automáticos? Com que frequência? | Postgres → aba *Backups* (ou *Volumes* → backups) | [PENDENTE] | |
| 2 | Por quanto tempo cada backup fica? | Mesma aba; política do plano | [PENDENTE — prazo do Railway] | |
| 3 | São criptografados em repouso? E o volume do banco? | Documentação de segurança/conformidade do Railway | [PENDENTE] | |
| 4 | Em que região ficam o banco e os backups? | Serviço → *Settings* → *Region* | [PENDENTE] | |
| 5 | Quem pode restaurar ou baixar um backup? | Projeto → *Members* | [PENDENTE] | |
| 6 | O Redis tem persistência/backup? (guarda imagens de card por 7 dias) | Redis → *Settings*/*Volumes* | [PENDENTE] | |
| 7 | Alguém já baixou um dump para um computador? | Memória do Felipe | [PENDENTE] | |

## A decisão registrada

**Backups do banco expiram em [PENDENTE — prazo do Railway].** É esse o
número que a política de privacidade e a página de apagar conta passam a
dizer: *"o que você apaga sai na hora do app e do servidor, e das cópias de
segurança em até [prazo]"*. Até o número ser conferido, as telas **não** podem
dizer "não há backup" (o que `delete-account-copy.ts` dizia antes de 26/09).

Regras que valem já:

- Um backup **não é restaurado** para "recuperar" dado que alguém apagou. Se
  uma restauração for necessária (desastre), as exclusões registradas depois
  da data do backup são refeitas antes de o serviço voltar — o
  `deletion_log` diz quantas foram, e os pedidos em `data_subject_requests`
  dizem de quem.
- **Nenhum dump** de produção em computador, Drive ou repositório. Se um for
  preciso para investigar algo, fica cifrado, some no mesmo dia, e a
  existência dele entra no `docs/decision-log.md`.
- Se o prazo do Railway for maior que o necessário e não for configurável, o
  prazo entra na política como é, e a questão entra no `docs/ripd.md` como
  risco residual.
