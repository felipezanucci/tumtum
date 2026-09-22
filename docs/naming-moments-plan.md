# Como um momento ganha nome sem ninguém tocar em nada

> Escrito em 2026-09-22, a pedido do Felipe, depois do teste da b145:
> *"a pessoa que está no estádio assistindo o jogo não vai lembrar de clicar
> gol… o nosso algoritmo tem que identificar sozinho os picos. E depois, seja
> através da ordem do setlist do show ou da análise do que aconteceu no jogo,
> a gente tem que identificar que momentos são esses."*
>
> Ele está certo, e o problema é menor do que parece — mas a parte que sobra
> é mais dura do que parece. Este documento separa as duas.

## 1. Duas coisas diferentes, e só uma está faltando

O teste da b145 (teste7, 22h35, 240 batidas) provou as duas ao mesmo tempo, o
que faz parecer que são uma coisa só. Não são.

| | O que é | Estado hoje |
|---|---|---|
| **Achar o momento** | Onde o coração disparou | ✅ **Já é automático.** `detect_peaks()` não olha a linha do tempo — o parâmetro existe e está marcado *"Unused here"*. Ele achou o pico das 22h35 sozinho |
| **Nomear o momento** | *O que* estava acontecendo ali | ❌ Vem da linha do tempo do evento, que hoje é digitada ou tocada |

O `GOL` que apareceu na tela não ajudou o detector a achar nada. O pico já
estava lá. A marca só **deu nome** a ele.

Então a frase certa não é *"o algoritmo tem que achar sozinho"* — ele já acha.
É: **de onde vem o nome, se ninguém tocar.**

## 2. Onde o fã está hoje (uma coisa a menos com que se preocupar)

Os botões `GOL` · `MÚSICA` · `MOMENTO` **já são invisíveis para o fã**. Eles só
aparecem com a chave *"Marcar momentos na captura"* ligada, atrás de
Configurações → OPERADOR, e ela vem **desligada**. Um fã que instala o app
hoje não vê botão nenhum durante a captura, e nunca viu.

O que está errado não é a experiência do fã — é que **a única fonte de nome
que funciona hoje depende de um operador presente e atento**. Isso serve a um
piloto de 3–5 pessoas e não serve a mais nada.

## 3. Jogo de futebol — resolvível, com uma âncora

A API-Football entrega os eventos com o **minuto da partida** (`elapsed` +
`extra`). Converter minuto em relógio de parede precisa de duas coisas, e
`parse_fixture_to_timeline()` erra as duas (open item 28):

1. **O intervalo.** Ela soma o minuto ao pontapé e ignora os ~15 minutos do
   intervalo, então todo evento do segundo tempo cai 15–20 min adiantado —
   quinze vezes fora da janela de ±60 s do correlator.
2. **O pontapé de verdade.** Ela usa o horário *agendado*. Jogo atrasa 1–5 min
   rotineiramente, e os acréscimos do primeiro tempo (1–5 min) empurram o
   segundo tempo mais ainda.

Somando: sem âncora, um gol do segundo tempo pode errar **até 15 minutos**.

**A correção.** O intervalo é aritmética. A âncora é que decide a precisão, e
tem duas saídas:

- **Dois toques do operador por jogo** — um no apito inicial, um no começo do
  segundo tempo. Não é o fã, não é "lembrar de marcar cada gol": são dois
  toques previsíveis, nos únicos dois instantes em que todo mundo no estádio
  sabe exatamente o que está acontecendo. Com eles, o erro cai para segundos e
  **todos os gols, cartões e substituições ganham nome sozinhos**, para todos
  os fãs daquele evento.
- **Sem toque nenhum**, dependendo do que o plano da API-Football expõe de
  horário real por período. Precisa ser verificado contra a API antes de
  prometer.

**Custo:** ~1 sessão para o intervalo + a âncora de dois toques + os testes.
Depois disso, um jogo inteiro se nomeia com dois toques em vez de um por gol.

## 4. Show — não tem solução pelo setlist, e isso é uma propriedade da fonte

**O Setlist.fm publica a ordem das músicas e nunca os horários.** Não é uma
limitação da nossa integração; é o que o serviço é. `parse_setlist_to_timeline()`
chuta 4 minutos por música e, por isso, sai da janela de ±60 s por volta da
terceira faixa — e o erro só cresce.

Nem a duração real resolve. Puxando a duração de cada música do Spotify (API
que já está na lista do projeto) e somando a partir do começo do show, ainda
sobram a conversa entre músicas, as intros esticadas, o solo que virou cinco
minutos e o grito da galera. Um set de vinte músicas roda com facilidade
20–30 minutos além da soma das versões de estúdio. **Na terceira música já
não dá para confiar, e na décima o nome estaria simplesmente errado** — o que
é pior do que não ter nome, porque o card mente.

Três caminhos reais:

### (a) Um aparelho ouvindo por evento — ~~a resposta de verdade~~ **não funciona ao vivo**

> **Retratação, 2026-09-22.** A versão anterior deste documento chamava este
> caminho de *"a resposta definitiva para shows"* e de *"a única opção que
> nomeia um show inteiro com precisão sem ninguém tocar em nada"*. **Está
> errado, e o erro é da tecnologia, não do preço.** A pesquisa que deveria ter
> vindo antes da recomendação está abaixo.

**A ideia era:** um aparelho da TumTum no local — o do operador, ou um
parado — escuta 10 s por minuto, reconhece a música (ACRCloud, AudD) e
publica a linha do tempo no servidor para todo mundo daquele evento. Nenhum
microfone de fã, nenhuma permissão nova, nenhuma mudança na declaração de
privacidade da loja. A arquitetura continua boa. **O reconhecimento é que
não acontece.**

**Por quê.** Uma impressão digital acústica identifica **uma gravação
específica** — a master de estúdio — e não a música. Ela é feita para
sobreviver a ruído, compressão e microfone ruim; **não** para sobreviver a
andamento diferente, tom diferente, arranjo diferente, a banda esticando a
intro e a galera cantando por cima. É uma propriedade declarada da técnica:
*acoustic fingerprinting is not robust against considerable musical changes,
which is why it is not applicable in live music use.* A mesma banda tocando
a mesma música ao vivo é, para o algoritmo, outra gravação — e não há nada
no banco para casar com ela.

O corolário é o teste: **quando o Shazam acerta num show, o que ele
detectou foi playback.** Backing track, base rodando, ou playlist do PA
entre uma música e outra. Isso não é uma anedota contra o método; é o que
o método mede.

**Onde ele continua valendo**, e vale de verdade:

- **DJ set** — o que toca *é* a gravação. Reconhecimento funciona inteiro.
- **Show com base pesada** — comum no pop brasileiro. Reconhece as faixas
  que rodam de base e erra as que a banda toca de verdade, sem avisar qual
  é qual. Um nome errado no card é pior do que nenhum, então isso só serve
  com uma confirmação humana em cima.
- **Playlist antes/depois do show** — reconhece, e não interessa a ninguém.

**O preço, para ficar registrado** (o Felipe perguntou, e a resposta é: nunca
foi o obstáculo):

| | Avulso | Volume |
|---|---|---|
| **AudD** | **US$ 0,005 por consulta** (300 grátis) | US$ 450 / 100 mil · US$ 1.800 / 500 mil (~US$ 0,002 a consulta). Monitoramento de stream contínuo: US$ 45 por stream/mês |
| **ACRCloud** | Pacotes anuais, ~¥320 por 10 mil consultas (**~US$ 0,0045 por consulta**) | ~¥300 por 10 mil no pacote de 1 milhão. Preço da página oficial exige login |

Escutando 10 s a cada minuto, um show de 3 h são ~180 consultas: **menos de
US$ 1 por show.** Mil shows por ano custariam algumas centenas de dólares.
**Barato e inútil** para o caso que importa — a banda tocando ao vivo é
exatamente o que a impressão digital não reconhece.

Identificar *que música é esta* a partir de uma execução ao vivo qualquer é
um problema de pesquisa em aberto (cover / version identification), não uma
integração de API. Não é trabalho de uma sessão, e não é trabalho nosso.

### (b) Oferecer um palpite em vez de afirmar — barato e honesto

O momento mostra `às 22h35` e, embaixo, *"tava tocando uma dessas?"* com as
2–3 músicas cuja janela estimada cobre aquele minuto, tiradas do setlist +
durações do Spotify. A pessoa toca na certa.

Isso é **reconhecimento no lugar de memória** — exatamente o princípio §5.6 da
pesquisa de 19/09, que é o mesmo que já justificou o *"Toca pra dizer o que
tava rolando"*. E respeita a regra da casa: o app **não afirma** o que não
sabe; ele oferece. Um palpite errado custa um toque; um nome errado no card
custa a confiança.

Custo: ~1 sessão. Funciona no dia seguinte ao show, sem aparelho no local, sem
API paga. Com (a) fora, **este é o caminho para show** — e ele melhora
sozinho: quando a linha do tempo de um evento for boa (futebol com âncora, um
DJ set reconhecido, um setlist com horários que alguém anotou), o palpite
vira certeza e a pergunta some.

### (c) Não nomear

O card 01 ("Só o momento") é o padrão universal do manual e **não precisa de
nome nenhum**: pico, hora, evento. É o que saiu hoje no teste7 antes do GOL
entrar. Vale lembrar que o produto funciona sem nome — o nome é o que o faz
valer mais.

## 5. Recomendação

Revista em 22/09, depois que (a) caiu.

| Ordem | O quê | Custo | Por quê agora |
|---|---|---|---|
| 1 | **Futebol: intervalo + âncora de dois toques** | ~1 sessão | A única fonte de linha do tempo **exata** que existe para nós. Transforma um jogo inteiro em nomes automáticos, para todos os fãs daquele evento. O piloto de futebol depende disto |
| 2 | **Palpite do setlist** (b) | ~1 sessão | Com (a) fora, **é o caminho para show** — não um paliativo enquanto a resposta boa não vem. Barato, honesto, serve show e jogo, e não depende de nada estar presente no evento |
| 3 | **Reconhecimento de áudio** (a) | 1–2 sessões + ~US$ 1 por evento | **Só para DJ set.** Fora disso, não reconhece. Não é prioridade e não é a resposta para show |

A inversão de 1 e 2 tem um motivo além de (a) ter caído: futebol é o único
lugar onde o nome pode ser **exato**, e `docs/pilot-event-options.md` já
escolhe o jogo como teste técnico do piloto.

**Construídos em 22/09 (#77), os dois.** O relógio do jogo vive em
`backend/app/services/football_service.py` (`MatchClock`, duas âncoras lidas
da linha do tempo, `APITO` e `2º TEMPO` na tela de captura de um jogo). O
palpite vive em `setlist_guess.py` e chega ao app como `candidate_labels`
em cada momento sem nome — chips "TAVA ROLANDO UMA DESSAS?". A regra que
liga os dois: **horário medido afirma, horário derivado oferece** — uma
entrada com `estimated: true` ou `anchored: false` nunca chega ao
correlator. E o admin web (`/admin/eventos/{id}`) é onde o jogo ou o
setlist são ligados ao evento. Falta um jogo de verdade para testar.

**Nada disso mexe no detector.** Ele já faz a parte que o Felipe pediu.

As marcas do operador **ficam**, como rede de segurança: são o que salva um
show cuja linha do tempo não veio, e custam uma chave desligada por padrão.

## 6. O que fica registrado

- O detector acha momentos sem marca nenhuma. Provado em campo (Realness,
  20 momentos) e de novo na b145.
- As marcas nomeiam; não acham.
- O fã nunca vê botão de marca, e nunca viu.
- Setlist.fm não tem horário e nunca vai ter. Qualquer plano que dependa de
  estimar o horário de uma música pelo setlist tem um teto de precisão de
  poucos minutos — e poucos minutos é um nome errado.
- **Reconhecimento de áudio identifica uma gravação, não uma música.** Ao
  vivo, a gravação não existe no banco. O caminho (a) desta mesma página foi
  recomendado antes de isso ser verificado e retirado no mesmo dia; a API
  custa menos de US$ 1 por show e isso nunca foi o ponto.
- A lição de processo, que é a cara: **o custo foi levantado antes da
  viabilidade.** A pergunta "quanto custa" só vale depois de "funciona", e
  aqui ela chegou primeiro porque a resposta parecia óbvia.
