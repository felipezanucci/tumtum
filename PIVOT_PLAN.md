# TumTum Pulse — Plano de Pivot B2B

## Resumo Executivo

A TumTum, na forma atual (B2C de HR em shows), enfrenta os mesmos problemas estruturais que mataram 4 startups antes dela: Moodmetric, Feel, Lightwave e Bionym/Nymi. Nenhuma conseguiu fazer biometria emocional funcionar como produto de consumidor.

A proposta: pivotar para **TumTum Pulse**, uma plataforma B2B SaaS de audience engagement analytics. O consumidor usa o app gratis (canal de coleta). Quem paga e a produtora, venue ou patrocinador que recebe os relatorios.

O mercado brasileiro de shows ao vivo fatura R$94 bilhoes/ano (2o maior do mundo). Produtoras e patrocinadores operam as cegas sobre engajamento real. TumTum Pulse preenche essa lacuna com dados biometricos agregados e anonimos.

**Meta Ano 1**: 20 clientes B2B, R$2.4M ARR, margem bruta ~80%.

---

## Capitulo 1: Autopsia Cruzada — Licoes das 4 Startups

### Moodmetric (2013, Finlandia)
- Anel de EDA por $200. Funding minimo. App descontinuado em Fev/2024.
- Problema: preco alto, form factor nicho, nao escalou B2C, tentou B2B stress corporativo sem sucesso.

### Feel / Sentio Solutions (2015, EUA)
- Pulseira com 5 sensores. Levantou $33M. Pivotou de B2C para B2B pharma.
- Problema B2C: consumidores nao mantinham uso. Salva pelo pivot para digital therapeutics (monitorar pacientes em trials clinicos).

### Lightwave (2012, EUA)
- Pulseira descartavel para eventos. Clientes: Pepsi, Fox, Google, Jaguar.
- Nunca levantou VC. Morreu silenciosamente ~2017.
- Problema: modelo de consultoria (projetos pontuais), nao SaaS recorrente.

### Bionym/Nymi (2011, Canada)
- Pulseira ECG para autenticacao. Levantou $32M. MasterCard e Salesforce investiram.
- 10K pre-orders, mas nunca entregou produto consumer. Pivotou para enterprise pharma manufacturing. Adquirida pela Innominds em 2022.
- Problema B2C: atrasos de hardware, ecossistema inexistente, Face ID matou a proposta.

### Os 7 Padroes de Fracasso Compartilhados

1. Solucao fascinante, problema inexistente no consumidor
2. B2C com wearable biometrico nao escala
3. Efeito novidade mata retencao
4. Hardware e armadilha mortal
5. "Correlacao biometrica = emocao" e cientificamente fragil
6. O pivot B2B salvou as que sobreviveram
7. O mercado que paga e diferente do mercado que fascina

### 10 Aprendizados Aplicados ao TumTum

1. Nao construa hardware — use o que ja existe
2. Nao venda para consumidor final — venda para quem tem orcamento
3. Nao prometa "medir emocao" — prometa "medir engajamento"
4. Receita recorrente ou morte (SaaS, nao projetos pontuais)
5. O dado agregado vale mais que o individual
6. Encontre quem sofre a dor (produtoras, patrocinadores)
7. Comece pelo nicho que paga mais
8. Use middleware (Terra API), nao construa integracoes
9. Valide com dinheiro antes de codigo
10. O produto viral e o relatorio, nao o app

---

## Capitulo 2: O Pivot — TumTum Pulse

### O Problema que Resolve

Produtoras e patrocinadores de eventos ao vivo no Brasil (R$94 bi/ano) operam as cegas sobre engajamento real:
- Produtoras nao sabem se o setlist funcionou
- Patrocinadores nao sabem se a ativacao de marca engajou
- Venues nao sabem qual tipo de evento gera mais reacao
- Artistas nao tem dados objetivos sobre a performance ao vivo

### Como Funciona

1. Produtora cadastra evento no TumTum Pulse
2. Participantes com wearable usam o app TumTum (gratis)
3. HR coletado em background durante o evento via Terra SDK
4. Pipeline agrega dados anonimos pos-evento
5. Relatorio com Engagement Score, timeline de picos, analise por momento
6. Dashboard web para o cliente B2B

### Engagement Score (0-100)

Metrica proprietaria baseada em:
- HR_delta (variacao vs. baseline): peso 0.30
- Peak_density (picos coletivos/hora): peso 0.25
- Sync_rate (% com HR elevado simultaneamente): peso 0.25
- Sustained_elevation (tempo em ativacao): peso 0.20

Nao promete medir emocao. Mede ativacao fisiologica coletiva.

### Pricing

| Tier | Preco | Cliente Alvo |
|------|-------|-------------|
| Starter | R$2.000/evento | Produtoras regionais |
| Pro | R$8.000/mes (10 eventos) | Produtoras nacionais |
| Enterprise | R$25.000/mes (ilimitado) | T4F, Live Nation |
| Sponsor Report | R$5.000/relatorio | Marcas patrocinadoras |

### Unit Economics

- Receita media/cliente: R$10.000/mes
- Margem bruta: ~80%
- Target ano 1: 20 clientes = R$2.4M ARR
- LTV:CAC estimado: 12-24x

### GTM (Go-to-Market)

**Fase 1 (Meses 1-3)**: Prova de conceito. Felipe + 30-50 amigos em 1 show. Custo: R$0.

**Fase 2 (Meses 3-6)**: Cold outreach com case study. Alvo: produtoras menores (30e, Opus), venues (Allianz, Vibra), agencias de ativacao. Meta: 3 pilotos pagos.

**Fase 3 (Meses 6-12)**: Converter pilotos em contratos. Parcerias com clientes para distribuir app. PR via relatorios interessantes (Billboard Brasil, Rolling Stone).

**Fase 4 (Meses 12-24)**: Integracao Sympla. Expansao para futebol. Tier Enterprise.

### Stack Tecnico Simplificado

- Mobile: React Native + Terra SDK (HealthKit + Health Connect)
- Backend: FastAPI (Python) — 1 servico
- Database: PostgreSQL vanilla (sem TimescaleDB)
- Processing: Script Python (cron job, sem Celery/Redis)
- Dashboard: Next.js + Recharts
- Hosting: Vercel (free) + Railway ($20-50/mes)
- Custo infra: ~$50-80/mes + $399/mes Terra API

### LGPD

Dados entregues ao B2B sao agregados e anonimos (art. 12 LGPD — fora do escopo). Nenhum cliente ve HR individual.

### Cronograma

| Mes | Foco |
|-----|------|
| 1-2 | App mobile MVP (React Native + Terra SDK) |
| 2-3 | Backend + pipeline de processamento |
| 3-4 | Dashboard web MVP |
| 4 | Primeiro teste real (30-50 pessoas em show) |
| 5-6 | Cold outreach + 3 pilotos pagos |
| 7-8 | Iterar com feedback dos pilotos |
| 9-12 | Escalar para 10-20 clientes |

Investimento Ano 1: ~R$30-50K total.
