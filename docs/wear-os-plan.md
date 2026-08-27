# Wear OS + Health Services — plano e custo real

**Criado:** 2026-08-27, em resposta à pergunta "quanto tempo para o Health
Services num app de Wear OS?".
**Substitui** a estimativa da fase 2 de `docs/path-2-roadmap.md`, escrita em
24/08 quando o app de celular ainda não existia.

---

## 1. A reformulação que muda a decisão

O Wear OS entrou na conversa como "o jeito de ver a batida ao vivo". Mas ao
vivo **não é o que a Fase 0 precisa**. O laço do produto é: viver o evento →
depois ver a noite → gerar o card → compartilhar. Ninguém olha o relógio no
meio do show.

O valor real de um app de Wear OS é outro, e é maior:

> **O `ExerciseClient` garante ~1 leitura por segundo em qualquer aparelho com
> Wear OS 3+.** Não depende de configuração, de o dono lembrar de nada, nem de
> qual app do fabricante está instalado.

Ou seja: o app de relógio **resolve exatamente o risco que pode matar o caminho
do Health Connect** — a densidade da amostragem. Essa é a razão para construí-lo,
não o "ao vivo".

### E existe uma mitigação que custa zero

O mesmo relógio que amostra a cada 10 minutos em uso normal amostra denso
**quando há uma sessão de exercício rodando**. Então antes de gastar semanas:

> Instrução no onboarding: *"quando o show começar, inicie um treino no seu
> relógio."*

Custa uma frase. Resolve a densidade para quem obedecer. O app de Wear OS é a
versão que funciona para quem **não** obedecer — e é isso que ele está
comprando por várias semanas de trabalho.

E o Super Panorama de junho/2026 diz que essa frase pede pouco: **monitorar
exercício é a funcionalidade mais importante do vestível para 30,2% dos donos
brasileiros** — o primeiro lugar da lista. A instrução não pede um hábito novo,
pede o hábito que já é o mais comum.

---

## 2. As três formas de capturar, lado a lado

| | Alcance | Densidade | Custo para nós | Custo para a pessoa |
|---|---|---|---|---|
| **Cinta + app** (hoje, provado) | qualquer um com cinta | 1 Hz garantido | **zero, feito** | precisa de uma cinta |
| **Health Connect** (próxima etapa) | Android com relógio | **depende do dono** | ~2 semanas | exportar/autorizar |
| **App de Wear OS** | **só Wear OS 3+** | 1 Hz garantido | **3–5 semanas** | instalar via ADB (piloto) |

**O alcance é o dado desconfortável.** Wear OS 3+ significa Galaxy Watch 4 ou
mais novo, Pixel Watch, e pouco mais. Galaxy Watch antigo é Tizen. Fitbit
próprio, Garmin, Amazfit: nenhum é Wear OS. Apple Watch, que é o relógio mais
comum, está fora.

É a fatia mais estreita das três, pelo maior custo de construção.

### E o mercado brasileiro piora essa conta

**30,1% dos brasileiros donos de smartphone usam relógio ou pulseira
inteligente** (Super Panorama, junho/2026; base 4.138, 16 anos ou mais) —
37,6% nas classes A e B. Três pulsos em dez é o teto de qualquer caminho que
dependa do aparelho da pessoa. O Wear OS enxerga uma fração pequena desses três.

O volume no Brasil é dominado pela faixa abaixo de US$ 150 — Xiaomi, Huawei,
Zepp/Amazfit, Positivo — e o vestível mais vendido na Amazon Brasil em 2025 foi
o **Samsung Galaxy Fit3**, que é uma *band*, não um relógio.

**Nenhuma dessas pulseiras roda Wear OS.** Xiaomi Smart Band, Amazfit, Galaxy
Fit: são sistemas fechados do fabricante. Um app de Wear OS não roda em
nenhuma delas, por mais bem feito que seja.

Ou seja: o investimento de 3–5 semanas alcançaria Galaxy Watch 4+ e Pixel
Watch — e nada do que a maior parte do Brasil tem no pulso.

**Health Connect, ao contrário, alcança todas elas**: Mi Fitness, Zepp e
Samsung Health escrevem frequência cardíaca no Health Connect. É o único
caminho que chega ao volume brasileiro.

---

## 3. Etapas

### Etapa 0 — Comprar um relógio · dias · R$ 800–2.000

Não existe emulador que sirva: sensor de batimento não se emula. Precisa ser um
**Galaxy Watch 4+ ou Pixel Watch**, usado serve. Sem ele nada começa.

### Etapa 1 — Módulo Wear no projeto · 1–2 sessões

Um segundo módulo Gradle no mesmo repositório, com AndroidX, Compose for Wear
(ou views simples) e `androidx.health:health-services-client`. Sai do zero
absoluto de dependências do APK de celular — e diferente da Etapa 1 do Health
Connect, aqui isso é inevitável, não uma escolha.

### Etapa 2 — Captura com ExerciseClient · 2–3 sessões

| # | Tarefa |
|---|---|
| 2.1 | `ExerciseClient`, `HEART_RATE_BPM`, permissões `BODY_SENSORS` e `ACTIVITY_RECOGNITION` |
| 2.2 | `BatchingMode.HEART_RATE_5_SECONDS` — sem isso o relógio agrupa leituras por minutos quando a tela apaga, que é o estado normal num show |
| 2.3 | Foreground service + Ongoing Activity, o mesmo aprendizado que o app de celular já pagou |
| 2.4 | `capabilities()` em tempo de execução: aparelhos diferem, e a tela precisa dizer a verdade sobre o que este aparelho faz |

### Etapa 3 — A noite tem que sobreviver · 1–2 sessões

O relógio é **mais agressivo** que o celular matando processos, tem menos
memória, e a bateria acaba. As leituras precisam ir para disco enquanto chegam
— não guardadas em memória como o serviço do celular faz hoje.

### Etapa 4 — Tirar os dados do relógio · 2–3 sessões · **a parte difícil**

Duas arquiteturas, e a escolha não é óbvia:

| | Como | Problema |
|---|---|---|
| **Via celular** | `DataClient` → app de celular → backend | a ponte precisa sobreviver seis horas; sincronizar arquivos grandes pelo Data Layer é notoriamente chato |
| **Direto** | relógio no WiFi/LTE → backend | **login num relógio**: digitar e-mail e senha numa tela de 45 mm |

Recomendação: **via celular**, reusando todo o caminho de upload que já está
provado. O relógio grava, o celular busca depois do show e sobe.

### Etapa 5 — Instalar no relógio das pessoas · **o obstáculo real**

Não existe "manda o link do APK" no Wear OS. Cada relógio precisa de: opções de
desenvolvedor destravadas, depuração sem fio ligada, e um **PC com ADB** na
mesma rede — uma sessão presencial por pessoa.

Para um piloto de 3–5 pessoas, dá. **Não escala** sem publicar na Play Store,
que traz o mesmo formulário de declaração de saúde do outro plano, mais a
revisão do Wear.

### Etapa 6 — Validação · 1 sessão + noite real

1. Protocolo de três blocos, relógio contra Polar H10 na mesma pessoa.
2. **Soak de bateria de 4–6 horas** com medição nas duas pontas.

> **A bateria é um gate de verdade, não uma formalidade.** Uma sessão de
> exercício com batimento contínuo é o modo que mais consome num relógio.
> Se um Galaxy Watch não atravessa seis horas de festival, o app de Wear OS
> não serve para festival — e isso só se descobre medindo.

---

## 4. Estimativa

| | |
|---|---|
| **Sessões de trabalho** | 7 a 11 |
| **Calendário** | **3 a 5 semanas**, depois de o relógio chegar |
| **Antes de 25/09** | **Não cabe** com folga. Cabe apertado e sem margem para o inesperado |

Comparado ao Health Connect (4–6 sessões, ~2 semanas): **cerca do dobro**, para
o público mais estreito.

O que não comprime: cada iteração precisa de um relógio num pulso. Não há SDK
do Android nesta máquina, não há emulador com sensor, e a bateria só se mede em
tempo real.

---

## 5. Recomendação

**Nesta ordem, e cada uma decide a seguinte:**

1. **A frase no onboarding** ("inicie um treino quando o show começar"). Custa
   nada, e pode resolver a densidade sem nenhum código.
2. **Health Connect**, começando pela Etapa 0 daquele plano — que também custa
   nada e mede a densidade real.
3. **Wear OS**, e só se a Etapa 0 do Health Connect mostrar que a densidade não
   vem de graça — porque é o único problema que este app resolve e os outros
   dois caminhos não.

Construir o app de Wear OS antes de medir seria pagar semanas por um seguro
contra um risco que ainda não foi quantificado.

---

## 6. Fontes

- [Health Services on Wear OS](https://developer.android.com/health-and-fitness/health-services)
- [Record an exercise with ExerciseClient](https://developer.android.com/health-and-fitness/health-services/active-data)
- [Compatibilidade entre aparelhos Wear OS](https://developer.android.com/health-and-fitness/health-services/compatibility)
- [Power your Wear OS fitness app — batching](https://android-developers.googleblog.com/2022/11/power-your-wear-os-fitness-app-with-health-services-latest-version.html)
- *Super Panorama, junho 2026* — Mobile Time / Opinion Box, gráficos 63 e 64 (páginas 40–41)
