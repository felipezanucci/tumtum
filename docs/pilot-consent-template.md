# Termo de Consentimento Livre e Esclarecido — piloto TumTum

Modelo de 26/09/2026, versão do texto `2026-09-26` (a mesma constante
`CONSENT_TEXT_VERSION` que o app e o site gravam). Nasceu da auditoria LGPD
(`RELATORIO-AUDITORIA-LGPD.md`, CR-3, itens A8 e D6).

**Como usar.** Imprima uma cópia por participante, preencha os campos entre
colchetes antes do dia, leia junto com a pessoa, e só então ela marca as caixas
e assina. Uma cópia fica com a pessoa. **A cópia assinada nunca entra neste
repositório**: veja "Onde este termo fica guardado", no fim. Nenhum campo
`[PENDENTE]` pode estar em branco no papel que alguém assina.

O termo não substitui as telas do app: o participante também dá cada
consentimento no app (Configurações → Privacidade), que grava no servidor com
data, hora e versão do texto. O papel é a prova do piloto; o registro no
servidor é a prova do produto. Os dois precisam bater.

---

## TERMO DE CONSENTIMENTO — PILOTO TUMTUM

**Evento:** [nome do evento] — [local] — [data] — [hora de início e fim]

**Participante:** nome completo ____________________________________________

**E-mail da conta TumTum:** ____________________________________________

### 1. Quem cuida dos seus dados

- **Controlador:** [PENDENTE — Felipe: razão social e CNPJ], com endereço em
  [PENDENTE — endereço].
- **Encarregado pelo tratamento de dados (DPO):** Felipe Zanucci. Fale pelo
  e-mail **oi@tumtum.cc**, com o assunto **"Privacidade"**. Resposta em até
  15 dias.

### 2. O que é este piloto

Um teste pequeno da TumTum com [número] pessoas. A TumTum lê o seu batimento
durante o evento, encontra os momentos em que o seu coração disparou, e mostra
esses momentos ligados ao que estava acontecendo (a música, o gol). Você pode
gerar um card e, se quiser, compartilhar.

**A TumTum não é um dispositivo médico e não interpreta saúde.** Nada aqui é
diagnóstico, alerta ou avaliação do seu coração. Se você sentir qualquer coisa
diferente no evento, procure o atendimento do local — não o app.

### 3. O que é coletado

Batimento cardíaco é **dado pessoal sensível** (dado de saúde, art. 5º, II, e
art. 11 da LGPD). Por isso tudo aqui depende do seu consentimento.

| Dado | De onde vem | Onde fica |
|---|---|---|
| Batimento por minuto (bpm), com data e hora de cada leitura | Do seu relógio, pelo Health Connect, **ou** de uma cinta Bluetooth emprestada pela TumTum | No seu celular; no servidor da TumTum **só se você marcar o item C abaixo** e tocar em "Guardar minha noite" |
| O evento em que a noite foi gravada | Você ativa o evento no app | Com a noite |
| Intervalos R-R (o tempo entre uma batida e outra) e movimento do celular | Da cinta e do acelerômetro, durante a captura | **Só no seu celular**, criptografados, e apagados quando a noite é guardada ou apagada. Nunca vão para o servidor |
| Nome, e-mail, data de nascimento | Você, ao criar a conta | No servidor, enquanto a conta existir |

Pelo relógio, a leitura acontece **só na janela do evento** (do início menos
30 minutos ao fim mais 30 minutos). Pela cinta, só entre o toque em "Começar"
e o fim da noite. Ao configurar o relógio, o app lê os últimos 60 minutos
uma única vez, só para saber se ele grava com frequência suficiente; essa
leitura não sai do celular.

### 4. Para que — marque cada item separadamente

Cada item é uma escolha sua. **Os itens A e B são necessários para
participar.** Os itens C a G são opcionais, começam desmarcados, e o piloto
funciona sem eles. Você pode mudar qualquer um a qualquer hora.

| | Finalidade | Obrigatório? | Marque |
|---|---|---|---|
| **A** | Aceito os **Termos de Uso** (tumtum.cc/termos) e a **Política de Privacidade** (tumtum.cc/privacidade) | Sim, para ter conta | [ ] |
| **B** | Autorizo a TumTum a **ler meu batimento** na janela do evento e gerar os meus momentos | Sim, para gravar a noite | [ ] |
| **C** | Autorizo **guardar a minha noite** (a série de batimentos e os momentos) na minha coleção, no servidor da TumTum | Não | [ ] |
| **D** | Autorizo incluir a minha noite, **sem meu nome e junto com a de outras pessoas**, na estatística coletiva do evento ("A galera"). Ela só é mostrada com pelo menos 100 noites no evento e 10 pessoas em cada momento, em faixas ("10+"), nunca como número exato | Não | [ ] |
| **E** | Autorizo **comparar** a minha noite com a de um artista ou atleta, quando isso existir (hoje não existe) | Não | [ ] |
| **F** | Autorizo usar a minha noite para **melhorar o detector de momentos** da TumTum | Não | [ ] |
| **G** | Quero receber **comunicações da TumTum por e-mail** | Não | [ ] |

### 5. Por quanto tempo

- **Dados deste piloto:** apagados em **[PENDENTE — data de descarte = data do
  evento + 30 dias]**, conforme o plano de descarte do piloto
  (`docs/pilot-data-retention.md`). Isso vale para o servidor, para o celular
  emprestado (se houver) e para qualquer cópia feita pela equipe.
- **Se você continuar usando a TumTum depois do piloto** e tiver marcado o item
  C: a série bruta de batimentos é apagada 30 dias depois de os momentos serem
  encontrados; os momentos e os cards ficam enquanto a conta existir e o item C
  estiver marcado.
- **Imagem do card:** fica em cache por até 7 dias e sai na hora se você apagar
  o card.

### 6. Com quem

**Com ninguém que não seja a TumTum e os fornecedores que fazem o serviço
funcionar**, sob contrato: Railway (servidor e banco de dados), Vercel (site),
Resend (e-mails de código) e Sentry (registro de erros, sem o conteúdo das
suas noites). A lista completa está na Política de Privacidade.

**Nada vai para clube, artista, organizador do evento, patrocinador ou
anunciante.** A TumTum não vende dados e não usa dados para anúncio.

Só você decide publicar um card ou um post no feed do evento, e cada
publicação é um toque seu.

### 7. Seus direitos e como revogar

Você pode, a qualquer momento e sem dar motivo:

- **desmarcar qualquer item** em Configurações → Privacidade, no app. Desmarcar
  o item C para de guardar as próximas noites; para apagar as que já estão lá,
  use "Apagar esta noite";
- **apagar uma noite** (Configurações ou a própria noite → "Apagar esta
  noite");
- **baixar todos os seus dados** (tumtum.cc/perfil → "Baixar meus dados");
- **apagar a conta inteira** (Configurações → Apagar conta, com a sua senha);
- **pedir** acesso, correção, portabilidade, informação sobre com quem os dados
  foram compartilhados, ou qualquer outra coisa: pelo app, pelo site
  (tumtum.cc/perfil → Pedidos) ou por oi@tumtum.cc com o assunto
  "Privacidade". Resposta em até 15 dias.

Sair do piloto não tem nenhum custo e não muda nada na sua relação com a
equipe.

### 8. Idade

Declaro que tenho **18 anos ou mais**. Data de nascimento: ____/____/________

### 9. Declaração e assinatura

Li este termo (ou ele foi lido comigo), tive a chance de perguntar, e as minhas
escolhas estão marcadas acima.

Local e data: ______________________________, ____/____/________

Assinatura do participante: ____________________________________________

Pela TumTum (nome e assinatura): ____________________________________________

---

## Onde este termo fica guardado

- **Fora do repositório, sempre.** Este repositório é público.
- A cópia assinada (papel ou PDF escaneado) fica em [PENDENTE — Felipe: pasta
  com acesso restrito, por exemplo um Drive só dele], com o nome
  `termo-piloto-<evento>-<AAAA-MM-DD>-<iniciais>.pdf`.
- Guarde **enquanto os dados do piloto existirem e por mais 5 anos** depois do
  descarte: é a prova de que o consentimento existiu. O termo contém nome e
  data de nascimento, não contém batimento.
- Na planilha de controle do piloto (também fora do repositório), anote para
  cada participante: iniciais, data da assinatura, itens marcados, e se as
  escolhas no app batem com o papel (conferir em `GET /api/consents` com a
  conta da pessoa, ou pedindo que ela abra Configurações → Privacidade).

## O que ainda falta para este termo valer

- [ ] [PENDENTE — Felipe] razão social, CNPJ e endereço do controlador.
- [ ] [PENDENTE — Felipe] data do evento e, com ela, a data de descarte.
- [ ] [PENDENTE — Felipe] a pasta fora do repositório onde os termos ficam.
- [ ] [PENDENTE — Felipe] revisão jurídica deste texto antes da primeira
  assinatura.
