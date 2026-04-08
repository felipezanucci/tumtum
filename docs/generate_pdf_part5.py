#!/usr/bin/env python3
"""TumTum Pricing Strategy PDF Generator - Part 5: Chapter 5"""

import pickle
import sys
sys.path.insert(0, "/home/user/tumtum/docs")
from generate_pdf_part1 import TumTumPDF, RED, CYAN

with open("/tmp/tumtum_pdf_state.pkl", "rb") as f:
    pdf = pickle.load(f)


def build_chapter5(pdf):
    """Chapter 5: Modelo de Revenue Share com Artistas e Atletas"""
    pdf.chapter_title("5", "Modelo de Revenue Share com Artistas e Atletas")

    pdf.section_title("5.1 Importancia Estrategica da Participacao de Artistas/Atletas")
    pdf.body_text(
        "O recurso de comparacao de FC com artista/atleta e o diferencial mais poderoso do TumTum. "
        "Nenhuma outra plataforma oferece 'Veja como seu coracao bateu em sincronia com seu artista favorito durante o show.' "
        "Este recurso:"
    )
    pdf.bullet("Impulsiona assinaturas Pro - e o gatilho de conversao #1")
    pdf.bullet("Cria conteudo viral - '82% sincronizado com Neymar' e inerentemente compartilhavel")
    pdf.bullet("Gera midia espontanea - quando um artista compartilha seus dados TumTum, milhoes de seguidores veem a marca")
    pdf.bullet("Constroi um moat - parcerias exclusivas com artistas sao dificeis de replicar")
    pdf.ln(2)

    pdf.highlight_box(
        "Este recurso SO funciona se artistas e atletas realmente participarem. "
        "O modelo de revenue share deve tornar a participacao uma decisao obvia."
    )

    pdf.section_title("5.2 Recomendacao de Revenue Share: 75/25 (Artista Recebe 75%)")

    pdf.table(
        ["Parte", "Participacao", "Racional"],
        [
            ["Artista/Atleta", "75%", "Share alto incentiva adocao precoce. O artista traz seu publico (milhoes de seguidores). A participacao deles E o produto."],
            ["TumTum", "25%", "TumTum fornece plataforma, tecnologia, geracao de cards e distribuicao. 25% e sustentavel em escala porque volume compensa menor margem."],
        ],
        [35, 25, 130],
    )

    pdf.subsection_title("Sobre Qual Receita o Split se Aplica?")
    pdf.table(
        ["Fonte de Receita", "Split?", "Exemplo"],
        [
            ["Microtx: desbloqueio de comparacao (R$ 7,90)", "SIM", "Fa paga R$ 7,90 para comparar com Anitta. Anitta recebe R$ 5,93. TumTum recebe R$ 1,97."],
            ["Assinatura Pro (parcela atribuida ao recurso)", "SIM, proporcional", "30% da assinatura vai ao pool de artistas quando o recurso e utilizado."],
            ["Cards de comparacao patrocinados (marca paga)", "SIM", "Budweiser patrocina cards de comparacao da Anitta. Receita dividida 75/25."],
            ["Insights de dados B2B (agregados/anonimizados)", "NAO", "Dados vendidos para promotores sao 100% receita TumTum."],
            ["Funcionalidades Pro padrao (cards sem artista)", "NAO", "Produto proprio do TumTum. Sem split necessario."],
        ],
        [55, 18, 117],
    )

    pdf.subsection_title("Exemplo com Numeros Reais")
    pdf.body_text("Cenario: Anitta se apresenta no Allianz Parque, 40.000 presentes")
    pdf.ln(1)
    pdf.table(
        ["Metrica", "Valor", "Receita para Anitta"],
        [
            ["Usuarios TumTum no evento (12,5% penetracao)", "5.000", "-"],
            ["Assinantes Pro (10% dos usuarios TumTum)", "500", "500 x R$ 4,47 x 75% = R$ 1.676"],
            ["Usuarios gratis que compram comparacao (15%)", "300", "300 x R$ 7,90 x 75% = R$ 1.777"],
            ["Usuarios gratis que nao compram", "2.500", "R$ 0 (mas veem o recurso, criando FOMO)"],
            ["TOTAL para Anitta (1 show)", "-", "~R$ 3.453"],
            ["TOTAL para TumTum (1 show, so comparacao)", "-", "~R$ 1.551"],
        ],
        [60, 25, 105],
    )

    pdf.section_title("5.3 Por Que 75/25 (Nao 50/50 ou 80/20)")

    pdf.subsection_title("Benchmarks de Mercado")
    pdf.table(
        ["Plataforma", "Share do Criador", "Share da Plataforma", "Contexto"],
        [
            ["Cameo", "75%", "25%", "Celebridade define preco. Mensagens de video 1:1."],
            ["OnlyFans", "80%", "20%", "Criador e o motor. Plataforma e infraestrutura."],
            ["YouTube", "55%", "45%", "Audiencia massiva. Plataforma fornece distribuicao + ads."],
            ["Twitch", "50-70%", "30-50%", "Streaming ao vivo. Plataforma fornece infra em tempo real."],
            ["Socios (Fan Tokens)", "~50%", "~50%", "Tokens cripto. Alta complexidade da plataforma."],
            ["Patreon", "88-95%", "5-12%", "Membership. Plataforma e so infra de pagamento."],
            ["TumTum", "75%", "25%", "Artista fornece audiencia + dados biometricos. TumTum fornece tech."],
        ],
        [35, 30, 30, 95],
    )

    pdf.subsection_title("Por Que Nao 50/50?")
    pdf.body_text(
        "50/50 e agressivo demais para uma plataforma nova. TumTum nao tem alavancagem de audiencia ainda. "
        "Artistas estao fazendo um favor ao TumTum participando cedo. O share de 75% diz: "
        "'Respeitamos que SUA base de fas e o que faz isso funcionar.' "
        "Em escala (1M+ usuarios), TumTum pode renegociar para 60/40, mas comecar generoso constroi lealdade."
    )

    pdf.subsection_title("Por Que Nao 80/20 ou 90/10?")
    pdf.body_text(
        "TumTum precisa de margem para sobreviver. Geracao de cards, infraestrutura, taxas de pagamento (3-5%) "
        "e suporte custam dinheiro. A 80/20, TumTum ganharia R$ 1,58 por microtx de R$ 7,90. "
        "Apos taxas de processamento (~R$ 0,40), sobram R$ 1,18. Nao e sustentavel. "
        "75/25 rende R$ 1,97 por microtx, menos ~R$ 0,40 de processamento = R$ 1,57 liquido. Apertado mas viavel em volume."
    )

    pdf.section_title("5.4 Estrutura de Pagamento por Tipo de Participante")

    pdf.subsection_title("Jogadores de Futebol")
    pdf.table(
        ["Tier", "Exemplos", "Deal Recomendado", "Estimativa Mensal"],
        [
            ["Superstar (fama internacional)", "Neymar, Vinicius Jr., Endrick", "Adiantamento fixo R$ 20-50K/mes + 75% rev share", "R$ 30-80K total"],
            ["Estrela do Brasileirao", "Artilheiros, favoritos da torcida", "75% rev share apenas (sem adiantamento)", "R$ 5-20K"],
            ["Talento em ascensao", "Jovens, titulares Serie A", "75% rev share apenas", "R$ 1-5K"],
            ["Emergente / Serie B", "Jogadores em desenvolvimento", "75% rev share + pacote de exposicao gratis", "R$ 200-2K"],
        ],
        [35, 40, 60, 55],
    )

    pdf.body_text(
        "Importante: Comecar com estrelas do Brasileirao e talentos em ascensao. "
        "Sao acessiveis, motivados por exposicao, e seu engajamento gera receita significativa. "
        "Superstars vem depois quando o modelo estiver comprovado."
    )

    pdf.subsection_title("Clubes de Futebol (Parcerias Institucionais)")
    pdf.table(
        ["Tier", "Exemplos", "Deal Recomendado"],
        [
            ["Grandes clubes (Big 12)", "Flamengo, Corinthians, Palmeiras, Sao Paulo", "Co-marketing: TumTum ganha integracao oficial. Clube recebe R$ 2-5K/mes minimo + 10% da receita de fas identificados como torcedores."],
            ["Clubes medios", "Fortaleza, Bahia, Athletico-PR", "Apenas rev share: Clube recebe 10% da receita de comparacao de seus jogadores + co-branding."],
            ["Clubes menores / Serie B", "Parte inferior Serie A, Serie B", "Parceria gratuita: Clube promove TumTum aos fas, TumTum fornece dashboard de engajamento."],
        ],
        [35, 50, 105],
    )

    pdf.body_text(
        "Nota: Os 10% do clube vem DA parcela de 25% do TumTum, nao dos 75% do jogador. "
        "Split efetivo: Jogador 75% / Clube 10% / TumTum 15%. Apertado mas vale pelo acesso institucional."
    )

    pdf.subsection_title("Musicos / Artistas")
    pdf.table(
        ["Tier", "Exemplos", "Deal Recomendado", "Estimativa Mensal"],
        [
            ["Megastar", "Anitta, Ludmilla, Ivete Sangalo", "Adiantamento R$ 10-30K/mes + 75% rev share", "R$ 20-60K total"],
            ["Artista principal", "Estrelas sertanejas, MPB/funk popular", "75% rev share apenas", "R$ 5-15K"],
            ["Artista em ascensao", "Regionais com 500K-2M seguidores", "75% rev share apenas", "R$ 1-5K"],
            ["Emergente / indie", "Artistas locais, bandas independentes", "75% rev share + promocao nos canais TumTum", "R$ 100-1K"],
        ],
        [30, 45, 65, 50],
    )

    pdf.body_text(
        "Insight importante: Artistas que se apresentam frequentemente (sertanejos fazendo 15-20 shows/mes) "
        "geram mais receita que artistas que fazem mega-shows ocasionais. O modelo de revenue share recompensa "
        "frequencia, o que naturalmente atrai os artistas mais valiosos para o TumTum."
    )

    pdf.section_title("5.5 Evolucao do Revenue Share por Fase")
    pdf.table(
        ["Fase", "Usuarios TumTum", "Share Artista", "Share TumTum", "Racional"],
        [
            ["Fase 0 - Lancamento", "0-50K", "75%", "25%", "Deve atrair artistas. TumTum nao tem alavancagem."],
            ["Fase 1 - Crescimento", "50K-500K", "75%", "25%", "Manter taxa. Artistas ainda sao o atrativo."],
            ["Fase 2 - Escala", "500K-2M", "70%", "30%", "TumTum fornece audiencia significativa. Novos contratos a 70/30."],
            ["Fase 3 - Dominancia", "2M+", "65%", "35%", "TumTum E a plataforma. Novos contratos a 65/35."],
        ],
        [30, 28, 22, 22, 88],
    )

    pdf.highlight_box(
        "Regra de ouro: NUNCA alterar retroativamente os termos de parceiros existentes. "
        "Manter parceiros iniciais a 75/25 para sempre e um investimento em lealdade."
    )

    pdf.section_title("5.6 Estrutura de Custos do Recurso de Comparacao")
    pdf.table(
        ["Componente de Custo", "Custo/Evento", "Notas"],
        [
            ["Setup e sync do device do artista", "~R$ 0", "Usam wearable existente. TumTum oferece suporte de setup."],
            ["Ingestao e processamento de dados", "R$ 0,02-0,05/usuario", "Compute do servidor para cruzar curvas artista + fa."],
            ["Geracao do card de comparacao", "R$ 0,05-0,10/card", "Geracao via Pillow/ImageMagick em Celery workers."],
            ["CDN storage e serving", "R$ 0,01-0,03/card", "Cloudflare R2. Negligivel em escala."],
            ["Processamento de pagamento", "3-5% da transacao", "~R$ 0,24-0,40 por microtx de R$ 7,90."],
            ["Custo total por card de comparacao", "~R$ 0,35-0,60", "-"],
            ["Receita por comparacao (microtx)", "R$ 7,90", "-"],
            ["Parcela de 25% do TumTum", "R$ 1,97", "-"],
            ["TumTum liquido apos custos", "~R$ 1,37-1,62", "~17-20% margem liquida no recurso de comparacao"],
        ],
        [55, 35, 100],
    )

    return pdf


pdf = build_chapter5(pdf)

with open("/tmp/tumtum_pdf_state.pkl", "wb") as f:
    pickle.dump(pdf, f)

print("Chapter 5 done. PDF state saved.")
