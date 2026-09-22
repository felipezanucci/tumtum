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

### (a) Um aparelho ouvindo por evento — a resposta de verdade

Reconhecimento de áudio (ACRCloud e similares): um aparelho escuta 10 s por
minuto e identifica a música. **O ponto que muda tudo: não precisa ser o
celular do fã.** Um aparelho da TumTum no local — o do operador, ou um
parado — reconhece e **publica a linha do tempo no servidor, para todo mundo
daquele evento**. Nenhum microfone de fã é usado, nenhuma permissão nova é
pedida a ninguém, e a declaração de privacidade da loja não muda.

Custo: API paga (da ordem de centavos por show), ~1–2 sessões de integração, e
um aparelho presente. **É a única opção que nomeia um show inteiro com
precisão sem ninguém tocar em nada.**

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
API paga. E **não conflita com (a)** — quando a linha do tempo for boa, o
palpite simplesmente vira certeza e a pergunta some.

### (c) Não nomear

O card 01 ("Só o momento") é o padrão universal do manual e **não precisa de
nome nenhum**: pico, hora, evento. É o que saiu hoje no teste7 antes do GOL
entrar. Vale lembrar que o produto funciona sem nome — o nome é o que o faz
valer mais.

## 5. Recomendação

| Ordem | O quê | Custo | Por quê agora |
|---|---|---|---|
| 1 | **Palpite do setlist** (b) | ~1 sessão | Barato, honesto, serve show e jogo, e é a única que não depende de nada estar presente no evento |
| 2 | **Futebol: intervalo + âncora de dois toques** | ~1 sessão | Transforma um jogo inteiro em nomes automáticos. O piloto de futebol depende disto |
| 3 | **Reconhecimento de áudio por evento** (a) | 1–2 sessões + API paga | A resposta definitiva para shows. Decisão de produto e de custo, não de código |

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
