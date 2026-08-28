# Health Connect — plano da próxima etapa

**Criado:** 2026-08-27, depois que o app nativo fechou o caminho da captura por
cinta e a detecção foi validada contra o Polar batimento a batimento.
**Objetivo:** ler a batida dos relógios que as pessoas já têm, sem cinta.
**Marco duro:** piloto Tasha & Tracie, **2026-09-25** (~4 semanas).

---

## 1. A pergunta que precisa ser feita antes de qualquer código

Health Connect **não é uma coisa só**, e a diferença decide o projeto inteiro:

| | O que é | Health Connect resolve? |
|---|---|---|
| **Depois do evento** | Ler o que o relógio já gravou e guardou | **Sim.** É exatamente para isso que ele existe |
| **Ao vivo, durante o show** | Receber batidas em tempo real | **Não.** Isso é Health Services num app de Wear OS — outro projeto, no relógio, semanas |

Para a hipótese da Fase 0 — *as pessoas acham a entrega valiosa?* — o caminho
"depois do evento" basta: a pessoa usa o relógio dela no show, abre o TumTum
depois e vê a noite. Este plano é sobre esse caminho. Ao vivo fica para depois
de a Fase 0 responder.

---

## 2. O risco que pode matar tudo, e ele é medível hoje

**A densidade da amostragem.** O detector de picos assume dados densos: média
móvel de 5 s, linha de base de 300 s, pico mínimo de 5 s. Ele foi calibrado
contra ~1 leitura/segundo.

Mas o que os relógios realmente gravam depende de uma configuração do dono:

- **Samsung Health**: mede continuamente **ou a cada 10 minutos**, conforme a
  opção que a pessoa escolheu. O padrão não é contínuo.
- **Fitbit**: 1 a 5 s **em modo treino**; fora dele, muito mais esparso.
- **Qualquer relógio**: uma sessão de exercício iniciada à mão quase sempre
  grava denso; o uso normal, não.

A 1 leitura por 10 minutos, seis horas de festival viram **36 pontos**. O
detector não roda, e — pior — a curva desenhada seria uma mentira bonita.

> **Consequência de produto, não só de engenharia:** se a densidade não passar,
> Health Connect entrega *a curva da noite*, não *os momentos*. Isso muda a
> promessa do card, não o cronograma.

**O que o mercado diz sobre esse risco.** No Super Panorama de junho/2026,
**monitorar exercício é a funcionalidade mais importante do vestível para 30,2%
dos donos brasileiros** — o primeiro lugar, à frente de notificações (29,8%).
Ou seja: pedir "inicie um treino quando o show começar" está pedindo o
comportamento que essas pessoas mais praticam, e é a razão de a Etapa 0 medir
os dois casos — a noite comum e a noite com treino iniciado.

**E o teto do caminho inteiro é 30,1%** — a proporção de donos de smartphone no
Brasil que usam relógio ou pulseira (37,6% nas classes A e B). Menos o iPhone,
que fica de fora deste plano.

---

## 3. Etapas

### Etapa 0 — A medição que decide tudo · **zero código** · uma noite

O `/import` **já existe e já lê export do Health Connect**, e já pontua
qualidade contra os requisitos do detector. Então isto não precisa de nenhuma
linha nova.

São **duas** perguntas, e a primeira é mais básica do que parecia:

| | Pergunta | Por que ela existe |
|---|---|---|
| **A** | O app do fabricante escreve batimento no Health Connect **de qualquer jeito**? | Escrever é opção do fabricante, e a lista do que ele escreve não é a lista do que ele mede |
| **B** | Em que cadência? | O detector foi calibrado contra ~1 leitura/segundo |

**O que a pesquisa de 27/08 já indica, e precisa ser confirmado no aparelho:**

- **Samsung Health** (Galaxy Fit3, Galaxy Watch) — o próprio relógio oferece
  *medir continuamente*, *a cada 10 minutos* ou *só manual*. E há relatos de
  que **nem tudo atravessa para o Health Connect: só o batimento de exercício
  chega de forma confiável.** Se isso se confirmar, a frase do onboarding deixa
  de ser mitigação de densidade e vira **requisito de funcionamento**.
- **Zepp / Amazfit** — escreve no Health Connect (Perfil → vinculação de contas
  de terceiros), e é **mão única**: só escreve, não lê. Há relato isolado de
  batimento não chegando corretamente.
- **Mi Fitness** (Xiaomi / Redmi Smart Band) — escreve passos, sono, batimento
  e treinos, com escolha por métrica na hora de autorizar.
- **Huawei Health** — **não escreve no Health Connect, e não é bug: é
  geopolítica.** Desde as sanções de 2019 a Huawei vive no seu próprio stack
  (HMS), e não existe ponte nativa. O único caminho é um app de terceiros
  (Health Sync) — atrito demais para pedir a um fã. Huawei Band é top 3 de
  vendas no Brasil, então isso é uma fatia real do teto de 30,1% que o
  caminho não alcança.

Nenhuma dessas linhas é medição nossa. São o motivo de a Etapa 0 existir.

**Armadilha que corrompe o resultado: o clone.** Marketplaces brasileiros
vendem genéricos com nome montado para busca — um anúncio real trazia
*"Amazfit Mi Band 10"*, duas marcas concorrentes num nome só, com specs de um
terceiro modelo. Esses aparelhos **não rodam Zepp nem Mi Fitness**; rodam apps
genéricos (DaFit, FitCloudPro, Wearfit) que não escrevem no Health Connect.

Medir um clone produz *"não escreveu nenhuma batida"* e a conclusão errada de
que **a marca** não funciona — um falso negativo que descartaria um fabricante
bom. Antes de aceitar um aparelho no teste, confirme que o app é
**Samsung Health, Mi Fitness ou Zepp**. Se for outro nome, o aparelho não
responde a pergunta e não entra. Vale igual para o piloto: alguém aparecer com
uma pulseira de marca desconhecida não é dado, é ruído.

**O protocolo:**

0. **As duas pulseiras podem ir no mesmo celular, na mesma noite** — e é o
   experimento melhor. Pareadas ao mesmo aparelho, ambas escrevem no mesmo
   Health Connect, e desde 28/08 o app mede **uma fonte por vez**, nomeando
   qual app escreveu cada leitura. Um pulso cada, a cinta Polar no peito:
   mesma noite, mesmo corpo, mesmo coração. Qualquer diferença entre os
   fabricantes é **do fabricante**, não da noite — e o Polar é a verdade
   contra a qual os dois são medidos.
1. Duas ou três pessoas com **marcas diferentes** — idealmente uma Samsung, uma
   Xiaomi/Amazfit — usam o relógio uma noite. (Ou uma pessoa só, com as duas
   pulseiras, conforme o item acima.)
2. **Cada uma faz as duas noites:** uma trecho de uso normal e um trecho com
   **treino iniciado à mão**, no mesmo aparelho. É o único jeito de separar o
   que o relógio não mede do que o fabricante não repassa.
3. Exportam do Health Connect e passam pelo `/import`.
4. Lê-se a cadência real e o score de qualidade, **por marca e por trecho**.

**Gate — todos precisam valer:**
- Cadência sustentada de **pelo menos 1 leitura a cada 5 s** durante a janela
- Score de qualidade Aprovado
- Detecção acha picos plantados (um esforço combinado no meio da noite)

**Se falhar só no trecho normal e passar no trecho de treino:** o caminho vive,
e a instrução de iniciar treino passa a ser parte do produto, não um conselho.

**Se falhar nos dois:** parar e redesenhar a promessa antes de construir a
tela. Esta etapa custa uma noite e evita semanas.

---

### Etapa 1 — Migração AndroidX · **feita, 2026-08-27** · verde no CI

O APK hoje **não tem nenhuma dependência de runtime**, por decisão registrada
no `build.gradle`. O `androidx.health.connect:connect-client` quebra isso: traz
AndroidX e coroutines, e o fluxo de permissão exige `ComponentActivity` — as
sete telas do app usam `android.app.Activity` puro.

| O que muda | Custo |
|---|---|
| Sete Activities viram ComponentActivity | mecânico, mas toca tudo |
| Gradle: AndroidX, coroutines, tema AppCompat se necessário | configuração |
| APK: ~1,1 MB → provavelmente 3–5 MB | aceitável |
| R8/minify pode precisar de regras | risco de build |

**Nada disso é visível para o usuário.** É custo de infraestrutura pago para
que a Etapa 2 exista.

**Como foi de verdade (27/08):** menor no código e maior na toolchain do que o
previsto. As sete telas (oito, com o splash) usavam `Activity` só como
superclasse, então a migração foi oito declarações — mas o `connect-client`
1.1.0 publicado exige `compileSdk 36` e AGP 8.9.1+, o que arrastou AGP 8.11.1,
Gradle 8.13 e Kotlin 2.0.21. Três voltas de CI: o metadado do artefato
contradizia a documentação (36, não 35), e o `ComponentActivity` em Kotlin
rejeita a assinatura antiga de `onRequestPermissionsResult`
(`Array<out String>` → `Array<String>`). APK agora é a versão 0.2.

---

### Etapa 2 — Ler o Health Connect · **feita com a Etapa 3 junto, 2026-08-27** · aguarda teste em aparelho

| # | Tarefa |
|---|---|
| 2.1 | Checagem de disponibilidade em três estados: indisponível / precisa atualizar / pronto — **cada um com sua própria frase honesta** (Android 14+ tem embutido; 13 e abaixo precisam do app da Play Store) |
| 2.2 | Permissão `android.permission.health.READ_HEART_RATE`, activity de justificativa e as entradas de manifesto (inclusive o `activity-alias` do Android 14+) |
| 2.3 | `readRecords(HeartRateRecord)` na janela do evento → contrato BPM |
| 2.4 | Cadência **medida, nunca assumida**, alimentando o score de qualidade |

**Limite a registrar:** o Health Connect só entrega dados de até **30 dias
antes** da permissão ter sido concedida, salvo permissão extra de histórico.
Irrelevante para um show de ontem; importante se alguém quiser importar uma
noite antiga.

---

### Etapa 3 — A tela · **feita, 2026-08-27** ("Trazer do relógio" no menu)

"Trazer do meu relógio" no menu: escolher a noite (ou o evento), ver **o que
foi encontrado antes de subir** — quantas leituras, que cadência, que qualidade
— subir, analisar, abrir a noite. O mesmo desenho de estados vazios honestos
das telas de hoje.

---

### Etapa 4 — Validação · 1 sessão + uma noite real

1. Protocolo de três blocos, relógio da pessoa contra o Polar H10 na mesma
   pessoa ao mesmo tempo.
2. Uma noite real de várias horas, com pico plantado e horário anotado.

**Gate:** mesmos critérios da validação de 27/08 — número certo de picos, no
minuto certo.

---

### Etapa 5 — Play Store · **só quando/se** · semanas, não dias

Publicar na Play Store com permissões de saúde exige **formulário de declaração
do desenvolvedor**, política de privacidade hospedada, seção de segurança de
dados e revisão do Google.

**O piloto não precisa disso.** APK instalado à mão lê o Health Connect sem
qualquer aprovação. Esta etapa existe para quando o TumTum for para a loja, e
deve ser começada com semanas de antecedência.

---

## 4. Estimativa

| | |
|---|---|
| **Sessões de trabalho** | 4 a 6, sem contar a Etapa 0 (que não tem código) |
| **Calendário** | ~2 semanas, com as voltas de teste no aparelho |
| **Folga até 25/09** | ~4 semanas — cabe, **se a Etapa 0 passar** |

O que não é comprimível: as voltas de teste. Não existe SDK do Android nesta
máquina e nenhum relógio aqui — cada "funciona mesmo?" passa por uma pessoa com
o aparelho na mão.

---

## 5. O que este plano **não** cobre

- **iPhone.** Health Connect é só Android. Quem tem Apple Watch continua no
  import por arquivo (que já funciona) até existir um app iOS — semanas, e a
  conta de desenvolvedor Apple, conforme `docs/path-2-roadmap.md` fase 3.
- **Ao vivo.** Ver a batida subir durante o show é Wear OS, não Health Connect.
- **Criar evento no app.** Continua no site.

---

## 6. Dívida a limpar junto

`frontend/lib/health/google-health-connect.ts` **não é Health Connect** — é a
API REST do Google Fit, que é outra coisa. E ela está morta duas vezes:

- **Inscrição de novos desenvolvedores fechada desde 1º de maio de 2024** — não
  dá nem para ativar.
- **Fim de vida no fim de 2026.**

O arquivo deve ser apagado junto com a Etapa 1. Enquanto existir com esse nome,
ele sugere que metade do Health Connect já está feita, e não está.
`backend/app/services/health_sync.py` merece a mesma leitura.

---

## 7. Fontes

- [Health Connect — Get started](https://developer.android.com/health-and-fitness/health-connect/get-started)
- [Publish your health app on Google Play](https://developer.android.com/health-and-fitness/health-connect/publish)
- [Health apps declaration form](https://support.google.com/googleplay/android-developer/answer/14738291)
- [Google Fit migration FAQ](https://developer.android.com/health-and-fitness/health-connect/migration/fit/faq)
- [Samsung — Health Connect FAQ](https://developer.samsung.com/health/health-connect-faq.html)
- *Super Panorama, junho 2026* — Mobile Time / Opinion Box, gráficos 63 e 64 (páginas 40–41)
- [Samsung — como o Samsung Health monitora batimento](https://ushl.samsung.com/latin_en/support/apps-services/how-samsung-health-monitors-your-heart-rate)
- [Fórum de desenvolvedores Samsung — Heart Rate Sync with Google Health Connect](https://forum.developer.samsung.com/t/heart-rate-sync-with-google-health-connect/24632)
- [Amazfit ganha sincronização com Health Connect](https://www.notebookcheck.net/Amazfit-smartwatches-get-new-Health-Connect-data-sync-feature.951489.0.html)
- [Xiaomi Mi Band + Health Connect](https://www.reaction-club.com/guides/xiaomi-health-connect)
