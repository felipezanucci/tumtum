# Verificação de idade (age assurance): opções e prévia de custo

27/09/2026. Item 15 da lista pós-parecer v1.1 (`docs/decision-log.md`, entrada de 26/09 à noite). Pesquisa feita só com o que as buscas públicas retornam: os sites dos fornecedores estavam bloqueados pela rede do ambiente de trabalho, então **todo preço abaixo é "a confirmar" com o fornecedor**. Nada aqui é parecer jurídico.

## Por que isso subiu de prioridade

O **ECA Digital (Lei 15.211/2025)** está em vigor desde **17/03/2026** e, no art. 9º, §1º, **proíbe a autodeclaração** como mecanismo de verificação de idade: caixa "sou maior de 18", campo de data de nascimento sem verificação e pop-up de confirmação não valem mais. A lei alcança produtos e serviços digitais "direcionados ou de provável acesso" por crianças e adolescentes. A ANPD publicou orientações com seis requisitos mínimos (proporcionalidade, precisão e confiabilidade, proteção de dados, não discriminação, transparência, interoperabilidade), um cronograma em fases (lojas de apps e sistemas operacionais primeiro; **demais setores a partir de agosto de 2026**, com diretrizes definitivas) e **fiscalização a partir de janeiro de 2027**.

O que isso significa para a TumTum, e o que o advogado precisa fechar (item 81):
- **Se a TumTum está no escopo** ("provável acesso" por adolescentes em shows e futebol é uma leitura razoável), a data de nascimento no picker, sozinha, **não cumpre a lei** desde março. Cumpre o parecer v1.0 para o piloto (conferência visual de documento sem cópia), mas não para o público.
- **Se não está no escopo**, a data de nascimento mais a conferência visual seguem sendo o mínimo do piloto, e o provedor entra antes do lançamento público, como o parecer v1.1 já pedia (§8).
- Em qualquer caso, o parecer v1.1 pede o **menor sinal necessário**: o provedor devolve "18+ / não 18+", a TumTum não guarda documento, CPF completo nem data de nascimento além do que já tem.

## As três famílias de método

| Família | O que prova | O que a TumTum recebe | Fraqueza |
|---|---|---|---|
| **A. CPF + data de nascimento na Receita** | Que aquele CPF pertence a alguém maior de 18 | Data de nascimento (ou um booleano, se descartarmos) | Não prova que **quem digitou** é o dono do CPF; um adolescente digita o CPF de um adulto. Pela régua da ANPD ("precisão e confiabilidade"), provavelmente insuficiente sozinho |
| **B. Documento + selfie (KYC)** | Que a pessoa na câmera é a do documento, e a idade do documento | Resultado + nome + data de nascimento do documento (podemos pedir só o sinal) | Fricção alta para um app de show; documento passa pelo fornecedor; transferência internacional na maioria |
| **C. Estimativa facial de idade** | Que o rosto na câmera tem, com margem, mais de 18 | Só "18+ / não 18+" e a margem | Falha perto dos 18 (pede fallback para B); é a mais alinhada ao "menor sinal necessário" |

## Fornecedores e prévia de custo

| Fornecedor | Família | Base / onde processa | Preço público (a confirmar) | Contratação | Leitura |
|---|---|---|---|---|---|
| **cpfhub.io** (BR) | A | Receita Federal | Verificação de idade "a partir de R$ 0,10"; consulta de CPF: 50 grátis, plano Pro R$ 149/mês com 1.000 consultas, R$ 0,15 por excedente; CPF não encontrado não cobra | Online, sem contrato | Barato e brasileiro; devolve nome e data de nascimento, temos de descartar |
| **entrar.api.br** (BR) | A | Receita Federal | Consulta de CPF a partir de **R$ 0,02** (1.000 créditos por R$ 20 via PIX), 10 grátis por mês, créditos não expiram | Online, sem contrato | O mais barato; mesma fraqueza da família A |
| **FonteData** (BR) | A | Receita Federal | Não encontrado nas buscas | Comercial | Vende explicitamente "para o ECA Digital" |
| **Serpro Datavalid** (governo) | A + B (biometria facial contra a base da CNH; dados cadastrais) | Bases oficiais, Brasil | Tabela por faixa de consumo na Loja Serpro (não legível daqui); **grátis 30 dias ou até 3.000 consultas** | Contrato com o Serpro, exige **CNPJ** | A opção mais defensável perante a ANPD: base oficial, dado fica no país, biometria liga a pessoa ao CPF. Precisa do CNPJ que ainda não existe (item 71) |
| **Didit** (ES/US) | B | Documento (RG/CNH) + selfie + liveness; processa fora do Brasil | **500 verificações grátis por mês** por recurso; depois ~US$ 0,15 documento + US$ 0,10 liveness + US$ 0,05 face match ≈ **US$ 0,30** por verificação completa; sem contrato mínimo | Online | Custo zero no nosso volume; exige DPA e mecanismo de transferência internacional (Res. 19/2024); fricção de documento |
| **Yoti** (UK) | C (e também B) | Estimativa facial; processa fora do Brasil | Não publicado; estimativas de mercado **US$ 1,50 a 3,00** por verificação, negociável por volume | Comercial | O método que a v1.1 prefere (só o sinal 18+, sem documento); o preço estimado é o mais alto da lista |
| **Veridas** (ES) | C + B | Estimativa facial com liveness; tem página em PT-BR | Não publicado | Comercial | Alternativa à Yoti na família C |
| **Veriff** (EE) | B | Documento + biometria | **US$ 2 a 6** por sessão; planos a partir de US$ 49/mês | Online/comercial | Caro para o que precisamos |
| **Unico, idwall, CAF** (BR) | B | KYC brasileiro (CNH/RG + selfie) | Sob consulta, foco em empresa grande | Comercial | Fortes no Brasil, mas dimensionados para banco e fintech |
| **gov.br** | — | Credencial de idade emitida pelo governo | — | Ainda não existe | A ANPD sinaliza o gov.br como emissor natural de credencial de idade; quando existir, é a resposta definitiva. Vale acompanhar |

## Prévia de custo por cenário

Volumes hipotéticos; o custo é por pessoa verificada uma vez, não por mês.

| Cenário | A (cpfhub / entrar) | B (Didit) | A+B (Datavalid) | C (Yoti, estimativa) |
|---|---|---|---|---|
| Piloto, 5 a 20 pessoas | R$ 0 (faixa grátis) | US$ 0 (faixa grátis) | R$ 0 (30 dias / 3.000 grátis) | sob consulta |
| Lançamento, 1.000 pessoas no primeiro ano | R$ 20 a R$ 150 | US$ 0 a 150 (só se passar de 500/mês) | tabela Serpro; ordem de centenas de reais | US$ 1.500 a 3.000 |
| 10.000 pessoas | R$ 200 a R$ 1.500 | US$ 1.500 a 3.000 | tabela Serpro; ordem de milhares de reais | US$ 15.000 a 30.000 |

Custo de engenharia, à parte do fornecedor: uma tela no app e no site, um endpoint que chama o fornecedor e grava só `age_verified_at`, `provider`, `method` e um id de transação (nunca CPF, documento ou foto), a rotina que apaga qualquer temporário, e o DPA. Estimativa: **dois a três dias** para a família A ou B com um fornecedor de API simples; mais uma semana para o Datavalid (contrato, homologação, certificado).

## Recomendação

1. **Piloto (agora):** data de nascimento no picker + conferência visual do documento pelo operador, sem cópia, anotada no termo assinado. É o que o parecer v1.1 aceita para o piloto, e custa zero.
2. **Antes do lançamento público, em duas camadas:**
   - **Camada 1, barata e brasileira:** CPF + data de nascimento conferidos na Receita (cpfhub ou entrar.api.br). A TumTum descarta a data e o CPF e guarda só o sinal. Custo praticamente zero. Sozinha, provavelmente não satisfaz a régua de confiabilidade da ANPD.
   - **Camada 2, que liga a pessoa ao dado:** **Serpro Datavalid** com biometria facial contra a base da CNH, assim que o CNPJ existir. Base oficial, dado no país, sem transferência internacional, faixa grátis para começar. Se o CNPJ demorar, **Didit** cobre o intervalo a custo zero, com o ônus do DPA e da transferência internacional.
3. **Não agora:** Yoti e Veriff (preço), Unico/idwall/CAF (porte). Reavaliar Yoti ou Veridas se a fricção do documento derrubar a conversão no lançamento, porque a estimativa facial é a que menos dado pede.
4. **Acompanhar:** a credencial de idade do gov.br e as diretrizes definitivas da ANPD (agosto de 2026); qualquer uma das duas pode simplificar tudo isso.

## O que fica com Felipe

- Advogado: a TumTum está no escopo do ECA Digital? (junto com o item 81)
- CNPJ (item 71): pré-requisito do Datavalid.
- Pedir a tabela de preços na Loja Serpro e no cpfhub; abrir conta de teste no Didit.
- Decidir a camada 2 e assinar o DPA correspondente (`docs/dpa-checklist.md`).

## Fontes consultadas

- ECA Digital em vigor e proibição de autodeclaração: [Congresso em Foco](https://www.congressoemfoco.com.br/noticia/117235/eca-digital-entra-em-vigor-o-que-muda-com-a-nova-lei), [Machado Meyer](https://www.machadomeyer.com.br/pt/inteligencia-juridica/publicacoes-ij/direito-digital/estatuto-digital-da-crianca-e-do-adolescente-lei-n-15-211-2025-entra-em-vigor-em-17-de-marco-de-2026), [CPFHub sobre o ECA Digital](https://www.cpfhub.io/eca-digital)
- Orientações e cronograma da ANPD: [Olhar Digital](https://olhardigital.com.br/2026/03/20/internet-e-redes-sociais/eca-digital-anpd-divulga-cronograma-para-verificacao-de-idade-em-apps/), [Correio Braziliense](https://www.correiobraziliense.com.br/brasil/2026/03/7380246-eca-digital-anpd-publica-orientacoes-para-verificacao-de-idade-on-line.html), [Migalhas](https://www.migalhas.com.br/quentes/452365/anpd-divulga-orientacoes-sobre-verificacao-de-menores-na-internet)
- cpfhub.io: [preços](https://www.cpfhub.io/precos), [verificação de idade](https://www.cpfhub.io/verificacao-de-idade)
- entrar.api.br: [verificação de idade](https://entrar.api.br/verificacao-de-idade), [consulta de CPF](https://entrar.api.br/)
- FonteData: [verificação de idade para o ECA Digital](https://fontedata.com/lp/verificacao-idade-eca-digital)
- Serpro Datavalid: [Loja Serpro](https://www.store.serpro.gov.br/datavalid), [Datavalid v5](https://www.serpro.gov.br/menu/noticias/noticias-2026/datavalid-v5), [gov.br](https://www.gov.br/pt-br/servicos/obter-solucao-digital-para-validacao-de-identidade-datavalid)
- Didit: [preços](https://didit.me/pricing/), [documentação de preços](https://docs.didit.me/getting-started/pricing)
- Yoti: [estimativa facial de idade](https://www.yoti.com/business/facial-age-estimation/), [comparativo com preços estimados](https://didit.me/blog/didit-vs-yoti/)
- Veriff: [faixas de preço](https://costbench.com/software/kyc-aml/veriff/), [Vendr](https://www.vendr.com/marketplace/veriff)
- Veridas: [APIs em PT-BR](https://veridas.com/br/apis/)
- gov.br como emissor de credencial de idade: [Didit sobre o ECA Digital no Brasil](https://didit.me/pt-BR/blog/brazil-digital-eca-age-verification/)
