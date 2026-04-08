# TumTum — Revisao Critica de Negocio

> Documento gerado em 2026-04-08. Analise independente assumindo postura cetica sobre viabilidade do projeto.

---

## Sumario Executivo

**Veredito: NAO PROSSEGUIR na forma atual.**

O TumTum possui uma premissa emocionalmente sedutora — "reviva seus momentos mais intensos atraves do seu batimento cardiaco" — mas a analise detalhada revela fragilidades estruturais em todas as dimensoes criticas: mercado, tecnologia, monetizacao, competitividade e execucao. A recomendacao e pivotar significativamente ou abandonar o projeto.

**Score geral: 2.5/10**

| Dimensao | Score | Resumo |
|----------|-------|--------|
| Problema/Demanda | 2/10 | Solucao procurando problema |
| Tamanho de Mercado | 2/10 | Nicho de nicho de nicho |
| Viabilidade Tecnica | 3/10 | Premissas cientificas frageis |
| Monetizacao | 2/10 | Unit economics nao fecham |
| Competitividade | 3/10 | Zero fossos defensaveis |
| Execucao | 3/10 | Fundador solo, stack complexo |

---

## 1. Analise de Mercado e Demanda

### 1.1 O problema nao existe

Ninguem sai de um show e pensa "queria saber minha frequencia cardiaca durante Yellow". O desejo real pos-evento e: (a) provar presenca, (b) reviver emocao, (c) gerar engajamento social. As pessoas ja fazem isso com fotos, videos e stories — conteudo emocionalmente rico que um grafico de BPM nao consegue superar.

### 1.2 O funil de usuarios e brutalmente estreito

Empilhando filtros para Sao Paulo:

| Filtro | Populacao |
|--------|-----------|
| Grande SP | 22.000.000 |
| Frequentam shows/jogos (2x/ano) | 6.600.000 |
| Possuem smartwatch com HR | 660.000 (penetracao ~10% na faixa AB) |
| Usam wearable durante o evento | 330.000 |
| Dispostos a instalar app + conectar dados de saude | 66.000 |
| Usam de fato e compartilham | ~20.000 |

**TAM realista do MVP: ~20 mil usuarios em SP, ~60-80 mil no Brasil inteiro.** Isso nao sustenta um negocio de venture capital.

### 1.3 Penetracao de wearables no Brasil

Brasil tem ~10-12 milhoes de smartwatches/smart bands em uso ativo (5% da populacao, vs. 20-25% nos EUA). O mercado e dominado por Xiaomi Mi Band e similares baratos com leitura de HR inconsistente e integracao fragil com Health Connect.

### 1.4 Frequencia de uso critica

Um brasileiro classe media vai a 3-5 shows e 10-20 jogos por ano (torcedor ativo). O TumTum seria aberto ~15-25 vezes por ano (~2x/mes). Apps com essa frequencia sao desinstalados. Retencao D7 para apps de uso mensal fica tipicamente abaixo de 10%.

### 1.5 Efeito novidade

Primeira vez: fascinante. Segunda vez: interessante. Terceira vez: previsivel. Quarta vez: esqueceu de abrir o app. Nao ha loop de retencao natural — nao e jogo, rede social, nem ferramenta diaria.

---

## 2. Analise Tecnica

### 2.1 Dados de wearables sao imprecises em eventos

PPG (fotopletismografia optica) no pulso perde precisao drasticamente com movimento. Erro medio: 7-10 BPM em repouso, 20-30 BPM durante atividade intensa. Em shows, usuarios pulam, batem palma, suam, seguram celular. O sensor perde contato constante.

O algoritmo de peak detection usa z-score > 2.0 sobre dados ja ruidosos. Resultado: picos falsos (artefatos de movimento interpretados como emocao) ou picos reais filtrados junto com ruido. O smoothing de 5 segundos nao resolve artefatos de movimento sistematicos que duram minutos.

O campo `motion_level` no modelo `HRData` existe mas nunca e populado. Sem dados de acelerometro, o algoritmo nao distingue "BPM subiu pela musica" de "BPM subiu porque pulou".

### 2.2 Sincronizacao temporal e ficcionalmente imprecisa

O `setlist_service.py` **inventa timestamps** — soma 4 minutos por musica a partir do horario declarado de inicio. Problemas:

- **Shows atrasam 30-60 minutos no Brasil** (norma, nao excecao)
- **Duracoes variam de 3 a 8+ minutos** — erro acumulado de 15-30 minutos pela musica 15
- **O correlator usa janela de +-60 segundos** — com timestamps errados por minutos, nenhum peak e corretamente atribuido

O card dira "Seu coracao disparou durante Creep" quando na verdade era "Karma Police". **A feature principal do produto esta fundamentalmente quebrada.**

### 2.3 PWA no iOS = produto morto para metade do mercado

`apple-health.ts` confessa: "In a PWA context, we rely on a companion iOS app or Apple Health export." Usuarios de iPhone (60%+ do publico-alvo AB em shows) precisam exportar XML manualmente — dealbreaker para um MVP frictionless.

### 2.4 Correlacao =/= emocao (falacia central)

HR sobe em shows por: posicao ortoestatica prolongada, alcool/cafeina, temperatura em multidoes, atividade fisica (pular, dancar), desidratacao. **Nenhum desses e "emocao".**

Estudo de Salimpoor et al. (2011, *Nature Neuroscience*): musica induz respostas autonomicas de ~5-10 BPM — abaixo do threshold de z-score > 2.0. Os picos detectados serao primariamente atividade fisica, nao emocionais.

### 2.5 Over-engineering do stack

Docker Compose sobe 5 servicos para o que e essencialmente: receber JSON de BPM, fazer conta estatistica, gerar PNG. TimescaleDB e projetado para bilhoes de data points de IoT. Um show de 3h gera 10.800 pontos. Com 1.000 usuarios = 10M pontos — Postgres vanilla resolve.

Stack mais adequado para MVP: SQLite + FastAPI + geracao inline. Roda em um Fly.io por $5/mes.

### 2.6 APIs externas frageis

- **Setlist.fm**: Cobertura pessima para sertanejo, funk, pagode — generos dominantes no Brasil
- **API-Football free**: 100 req/dia. Com 3 requests por jogo, atende ~33 jogos/dia. Brasileirao + estaduais esgotam no primeiro sabado
- **Zero fallback**: Servicos retornam listas vazias silenciosamente

---

## 3. Go-to-Market e Monetizacao

### 3.1 Aquisicao de usuarios

CAC estimado: R$80-150 por usuario ativo. Marketing digital nao tem segmentacao para "tem Apple Watch E vai a shows". Viralidade organica e a unica esperanca — mas o card do TumTum compete com video do show e selfie com amigos, e perde.

### 3.2 Viralidade: Spotify Wrapped voce nao e

O Wrapped funciona por: (a) base de 500M+ usuarios ja instalados, (b) constroi identidade pessoal ("meu top artista"), (c) momento cultural anual massivo. TumTum nao replica nenhum desses fatores.

Taxa de compartilhamento estimada: 10-15% na primeira experiencia, caindo para 3-5% nas subsequentes.

### 3.3 Modelos de receita — todos frageis

| Modelo | Receita estimada | Problema |
|--------|-----------------|----------|
| Cards premium (R$4,90) | ~R$1.600/mes com 8.400 usuarios | Nao paga servidor |
| Assinatura mensal (R$14,90) | Churn 30-40% em meses sem evento | Insustentavel |
| Assinatura anual (R$79,90) | LTV ~R$110-130, churn anual 60-70% | Payback de 8-12 meses |

### 3.4 Unit economics

Com uso de 3-5x/ano, DAU/MAU ficara abaixo de 5%. LTV de usuario freemium e proximo de zero. Para ratio LTV:CAC saudavel (3:1), CAC precisa ficar abaixo de R$40. **Impossivel sem viralidade organica massiva.**

### 3.5 A armadilha do hardware (Phase 1)

Certificacao Anatel: 6-12 meses. MOQ na China: 5-10 mil unidades. Custo unitario: R$80-150. Investimento minimo: R$500K-1M. Se Phase 0 nao validar (e provavelmente nao vai), esse investimento e suicidio financeiro.

---

## 4. Analise Competitiva

### 4.1 Por que ninguem fez isso?

Apple, Garmin, Fitbit, Samsung coletam HR de centenas de milhoes de usuarios ha uma decada. Nenhuma construiu essa feature. Razoes provaveis: (a) pesquisas internas mostraram demanda insuficiente, (b) correlacao HR+emocao e cientificamente imprecisa, (c) risco reputacional com dados de saude para features de entretenimento.

### 4.2 Risco de plataforma: dependencia existencial

Apple pode em qualquer WWDC: lancar "Concert Mode" nativo, restringir acesso a HR para terceiros (ja fez com SpO2), ou rejeitar o app. Google e historicamente ainda mais imprevisivel com APIs.

### 4.3 Zero fossos defensaveis

| Tipo de Fosso | TumTum Possui? |
|---------------|----------------|
| Tecnologia proprietaria | Nao — z-score e estatistica basica |
| Dados exclusivos | Nao — dados pertencem as plataformas |
| Efeitos de rede | Nao — experiencia individual |
| Marca | Nao — pre-lancamento |
| Custos de troca | Nao — zero switching cost |
| Regulatorio | Nao — regulacao e barreira, nao vantagem |

### 4.4 Feature, nao produto

Tudo que o TumTum faz pode ser replicado como: feature do Apple Watch (3 meses), plugin do Spotify (2 meses), ou template do Canva com integracao HealthKit. Sem IP, dados proprietarios ou efeito de rede, nao ha nada a defender.

### 4.5 Startups similares que falharam

- **Feel (2016)**: Pulseira de emocao. Levantou $1M, pivotou, morreu.
- **Moodmetric**: Anel emocional. Sem tracao de consumidor.
- **Lightwave**: Biometria de audiencias em shows. Pivotou para corporate — entretenimento nao pagava.
- **Bionym/Nymi**: Wearable biometrico. Pivotou para enterprise.

**Padrao claro: dados biometricos emocionais fascinam em demo mas nao sustentam negocio B2C.**

---

## 5. Risco de Execucao

- **Fundador solo nao-tecnico** aprendendo a programar enquanto constroi integracao com APIs de saude, processamento de series temporais, geracao de imagens e infra de backend
- **Stack de equipe de 4-5 engenheiros seniors** (Next.js + FastAPI + TimescaleDB + Celery + Redis + D3.js) para um dev solo
- **Estimativa de 14 semanas para MVP** e irrealista por fator de 3-4x para dev experiente, 6-8x para iniciante
- **Risco alto de burnout** antes do lancamento

---

## 6. Veredito Final

### NAO PROSSEGUIR na forma atual.

O TumTum e uma **demo impressionante em busca de um modelo de negocio**. A ideia e emocionalmente sedutora, mas seducao nao e demanda. Os problemas sao estruturais, nao de execucao:

1. **O problema nao existe** — ninguem pede para ver HR de shows
2. **O mercado e minusculo** — nicho de nicho de nicho (~20K usuarios em SP)
3. **A ciencia e fragil** — HR =/= emocao, dados de wearable sao ruidosos
4. **A monetizacao nao fecha** — uso 3-5x/ano nao sustenta negocio
5. **Zero fossos** — qualquer big tech replica em 3 meses
6. **Startups similares falharam** — padrao historico claro

### Se insistir, pivote para:

1. **B2B puro desde o dia 1**: Venda analytics de engajamento para produtoras/venues ("82% do publico teve pico de HR no encore"). O usuario usa gratis; quem paga e o venue.
2. **SDK/Feature, nao produto**: Licencie a tecnologia para Sympla, Ingresse, Eventim como feature de valor agregado.
3. **Abandone hardware completamente**: Phase 1 com smart band e caminho para falencia.

### Antes de qualquer codigo:

Faca **50 entrevistas** com pessoas saindo de shows no Allianz Parque: "Voce pagaria R$5/mes para ver seu batimento cardiaco sincronizado com o jogo?" A resposta provavelmente sepulta ou salva o projeto — e custa muito menos que um MVP.

---

*Este documento representa uma analise critica deliberadamente pessimista. Toda startup enfrenta ceticismo — mas os problemas identificados aqui sao estruturais, nao de execucao ou timing.*
