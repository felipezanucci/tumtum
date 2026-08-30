#!/usr/bin/env python3
"""Generate PDF from BUSINESS_REVIEW.md"""

from fpdf import FPDF
import re

class ReviewPDF(FPDF):
    DARK_BG = (8, 8, 12)
    SURFACE = (17, 17, 24)
    RED = (192, 57, 43)
    RED_LIGHT = (231, 76, 60)
    CYAN = (0, 210, 255)
    TEXT = (240, 240, 245)
    TEXT_MUTED = (107, 107, 128)
    BORDER_COLOR = (26, 26, 36)
    WHITE = (255, 255, 255)

    def __init__(self):
        super().__init__()
        self.set_auto_page_break(auto=True, margin=20)

    def header(self):
        if self.page_no() > 1:
            # Dark background on every content page
            self.set_fill_color(*self.DARK_BG)
            self.rect(0, 0, 210, 297, "F")
            self.set_fill_color(*self.RED)
            self.rect(0, 0, 3, 297, "F")
            # Header bar
            self.set_fill_color(*self.SURFACE)
            self.rect(0, 0, 210, 12, "F")
            self.set_font("Helvetica", "B", 7)
            self.set_text_color(*self.TEXT_MUTED)
            self.set_y(3)
            self.cell(0, 5, "TUMTUM  |  REVISAO CRITICA DE NEGOCIO  |  2026-04-08", align="C")
            self.ln(10)

    def footer(self):
        self.set_y(-15)
        self.set_font("Helvetica", "", 7)
        self.set_text_color(*self.TEXT_MUTED)
        self.cell(0, 10, f"Pagina {self.page_no()}/{{nb}}", align="C")

    def add_cover(self):
        self.add_page()
        self.set_fill_color(*self.DARK_BG)
        self.rect(0, 0, 210, 297, "F")

        # Red accent bar at top
        self.set_fill_color(*self.RED)
        self.rect(0, 0, 210, 4, "F")

        # Title
        self.set_y(80)
        self.set_font("Helvetica", "B", 36)
        self.set_text_color(*self.WHITE)
        self.cell(0, 15, "TUMTUM", align="C")
        self.ln(20)

        self.set_font("Helvetica", "", 14)
        self.set_text_color(*self.TEXT_MUTED)
        self.cell(0, 8, "Revisao Critica de Negocio", align="C")
        self.ln(30)

        # Verdict box
        self.set_x(30)
        self.set_fill_color(*self.SURFACE)
        self.set_draw_color(*self.RED)
        self.rect(30, self.get_y(), 150, 35, "FD")

        self.set_y(self.get_y() + 5)
        self.set_font("Helvetica", "B", 18)
        self.set_text_color(*self.RED_LIGHT)
        self.cell(0, 10, "VEREDITO: NAO PROSSEGUIR", align="C")
        self.ln(12)
        self.set_font("Helvetica", "", 11)
        self.set_text_color(*self.TEXT)
        self.cell(0, 8, "Score Geral: 2.5 / 10", align="C")

        # Date and meta
        self.set_y(200)
        self.set_font("Helvetica", "", 10)
        self.set_text_color(*self.TEXT_MUTED)
        self.cell(0, 7, "Documento gerado em 2026-04-08", align="C")
        self.ln(7)
        self.cell(0, 7, "Analise independente assumindo postura cetica sobre viabilidade do projeto", align="C")

        # Bottom red bar
        self.set_fill_color(*self.RED)
        self.rect(0, 293, 210, 4, "F")

    def new_content_page(self):
        self.add_page()
        self.set_fill_color(*self.DARK_BG)
        self.rect(0, 0, 210, 297, "F")
        self.set_fill_color(*self.RED)
        self.rect(0, 0, 3, 297, "F")

    def section_title(self, text):
        # Need room for title bar (12) + at least some content (~40)
        if self.get_y() > 240:
            self.add_page()
        self.ln(4)
        self.set_fill_color(*self.RED)
        self.rect(10, self.get_y(), 190, 12, "F")
        self.set_font("Helvetica", "B", 13)
        self.set_text_color(*self.WHITE)
        self.set_x(14)
        self.cell(0, 12, text.upper())
        self.ln(16)

    def subsection_title(self, text):
        if self.get_y() > 265:
            self.add_page()
        self.ln(2)
        self.set_font("Helvetica", "B", 10)
        self.set_text_color(*self.RED_LIGHT)
        self.set_x(12)
        self.cell(0, 7, text)
        self.ln(8)

    def body_text(self, text):
        if self.get_y() > 270:
            self.add_page()
        self.set_font("Helvetica", "", 9)
        self.set_text_color(*self.TEXT)
        self.set_x(12)
        self.multi_cell(186, 5, text)
        self.ln(2)

    def bold_text(self, text):
        if self.get_y() > 270:
            self.add_page()
        self.set_font("Helvetica", "B", 9)
        self.set_text_color(*self.TEXT)
        self.set_x(12)
        self.multi_cell(186, 5, text)
        self.ln(2)

    def bullet_point(self, text):
        if self.get_y() > 270:
            self.add_page()
        self.set_font("Helvetica", "", 9)
        self.set_text_color(*self.TEXT)
        self.set_x(16)
        self.cell(5, 5, "-")
        self.multi_cell(181, 5, text)
        self.ln(1)

    def add_score_table(self):
        headers = ["Dimensao", "Score", "Resumo"]
        rows = [
            ["Problema/Demanda", "2/10", "Solucao procurando problema"],
            ["Tamanho de Mercado", "2/10", "Nicho de nicho de nicho"],
            ["Viabilidade Tecnica", "3/10", "Premissas cientificas frageis"],
            ["Monetizacao", "2/10", "Unit economics nao fecham"],
            ["Competitividade", "3/10", "Zero fossos defensaveis"],
            ["Execucao", "3/10", "Fundador solo, stack complexo"],
        ]
        col_widths = [50, 20, 116]

        # Header
        self.set_fill_color(*self.RED)
        self.set_font("Helvetica", "B", 8)
        self.set_text_color(*self.WHITE)
        self.set_x(12)
        for i, h in enumerate(headers):
            self.cell(col_widths[i], 8, h, border=1, fill=True, align="C")
        self.ln()

        # Rows
        self.set_font("Helvetica", "", 8)
        for row in rows:
            self.set_fill_color(*self.SURFACE)
            self.set_text_color(*self.TEXT)
            self.set_draw_color(*self.BORDER_COLOR)
            self.set_x(12)
            for i, val in enumerate(row):
                align = "C" if i == 1 else "L"
                if i == 1:
                    self.set_text_color(*self.RED_LIGHT)
                else:
                    self.set_text_color(*self.TEXT)
                self.cell(col_widths[i], 7, f"  {val}", border=1, fill=True, align=align)
            self.ln()
        self.ln(4)

    def add_simple_table(self, headers, rows, col_widths=None):
        if self.get_y() > 240:
            self.add_page()
        if col_widths is None:
            w = 186 // len(headers)
            col_widths = [w] * len(headers)

        self.set_fill_color(*self.RED)
        self.set_font("Helvetica", "B", 7)
        self.set_text_color(*self.WHITE)
        self.set_x(12)
        for i, h in enumerate(headers):
            self.cell(col_widths[i], 7, f" {h}", border=1, fill=True)
        self.ln()

        self.set_font("Helvetica", "", 7)
        for row in rows:
            self.set_fill_color(*self.SURFACE)
            self.set_text_color(*self.TEXT)
            self.set_draw_color(*self.BORDER_COLOR)
            self.set_x(12)
            for i, val in enumerate(row):
                self.cell(col_widths[i], 6, f" {val}", border=1, fill=True)
            self.ln()
        self.ln(4)

    def add_quote_box(self, text):
        if self.get_y() > 255:
            self.add_page()
        y = self.get_y()
        self.set_fill_color(*self.SURFACE)
        self.set_x(12)
        self.set_font("Helvetica", "I", 9)
        self.set_text_color(*self.TEXT_MUTED)
        # Estimate height
        lines = len(text) // 80 + 1
        h = max(lines * 5 + 8, 14)
        self.rect(12, y, 186, h, "F")
        self.set_fill_color(*self.CYAN)
        self.rect(12, y, 2, h, "F")
        self.set_xy(18, y + 3)
        self.multi_cell(176, 5, text)
        self.set_y(y + h + 3)


def build_pdf():
    pdf = ReviewPDF()
    pdf.alias_nb_pages()

    # ========== COVER ==========
    pdf.add_cover()

    # ========== PAGE: SUMARIO EXECUTIVO ==========
    pdf.new_content_page()
    pdf.section_title("Sumario Executivo")

    pdf.bold_text("Veredito: NAO PROSSEGUIR na forma atual.")
    pdf.body_text(
        'O TumTum possui uma premissa emocionalmente sedutora - "reviva seus momentos mais '
        'intensos atraves do seu batimento cardiaco" - mas a analise detalhada revela fragilidades '
        "estruturais em todas as dimensoes criticas: mercado, tecnologia, monetizacao, "
        "competitividade e execucao. A recomendacao e pivotar significativamente ou abandonar o projeto."
    )

    pdf.bold_text("Score geral: 2.5/10")
    pdf.add_score_table()

    # ========== PAGE: MERCADO E DEMANDA ==========
    pdf.section_title("1. Analise de Mercado e Demanda")

    pdf.subsection_title("1.1 O problema nao existe")
    pdf.body_text(
        'Ninguem sai de um show e pensa "queria saber minha frequencia cardiaca durante Yellow". '
        "O desejo real pos-evento e: (a) provar presenca, (b) reviver emocao, (c) gerar engajamento "
        "social. As pessoas ja fazem isso com fotos, videos e stories - conteudo emocionalmente rico "
        "que um grafico de BPM nao consegue superar."
    )

    pdf.subsection_title("1.2 O funil de usuarios e brutalmente estreito")
    pdf.body_text("Empilhando filtros para Sao Paulo:")
    pdf.add_simple_table(
        ["Filtro", "Populacao"],
        [
            ["Grande SP", "22.000.000"],
            ["Frequentam shows/jogos (2x/ano)", "6.600.000"],
            ["Possuem smartwatch com HR", "660.000 (~10% faixa AB)"],
            ["Usam wearable durante evento", "330.000"],
            ["Dispostos a instalar + conectar saude", "66.000"],
            ["Usam de fato e compartilham", "~20.000"],
        ],
        [110, 76],
    )
    pdf.bold_text("TAM realista do MVP: ~20 mil usuarios em SP, ~60-80 mil no Brasil.")

    pdf.subsection_title("1.3 Penetracao de wearables no Brasil")
    pdf.body_text(
        "Brasil tem ~10-12 milhoes de smartwatches/smart bands em uso ativo (5% da populacao, vs. "
        "20-25% nos EUA). Mercado dominado por Xiaomi Mi Band e similares baratos com leitura de HR "
        "inconsistente e integracao fragil com Health Connect."
    )

    pdf.subsection_title("1.4 Frequencia de uso critica")
    pdf.body_text(
        "Brasileiro classe media: 3-5 shows e 10-20 jogos/ano. TumTum aberto ~15-25 vezes/ano "
        "(~2x/mes). Apps com essa frequencia sao desinstalados. Retencao D7 para uso mensal "
        "fica tipicamente abaixo de 10%."
    )

    pdf.subsection_title("1.5 Efeito novidade")
    pdf.body_text(
        "Primeira vez: fascinante. Segunda: interessante. Terceira: previsivel. Quarta: esqueceu "
        "de abrir. Nao ha loop de retencao natural - nao e jogo, rede social, nem ferramenta diaria."
    )

    # ========== ANALISE TECNICA ==========
    pdf.section_title("2. Analise Tecnica")

    pdf.subsection_title("2.1 Dados de wearables sao imprecisos em eventos")
    pdf.body_text(
        "PPG (fotopletismografia optica) no pulso perde precisao com movimento. Erro medio: "
        "7-10 BPM em repouso, 20-30 BPM durante atividade intensa. Em shows, usuarios pulam, "
        "batem palma, suam. O sensor perde contato constante."
    )
    pdf.body_text(
        "O algoritmo de peak detection usa z-score > 2.0 sobre dados ja ruidosos. Resultado: "
        "picos falsos (artefatos de movimento) ou picos reais filtrados junto com ruido. "
        "O smoothing de 5s nao resolve artefatos sistematicos que duram minutos."
    )

    pdf.subsection_title("2.2 Sincronizacao temporal ficcionalmente imprecisa")
    pdf.body_text(
        "O setlist_service.py inventa timestamps - soma 4 minutos por musica a partir do "
        "horario declarado de inicio. Problemas criticos:"
    )
    pdf.bullet_point("Shows atrasam 30-60 minutos no Brasil (norma, nao excecao)")
    pdf.bullet_point("Duracoes variam de 3 a 8+ min - erro acumulado de 15-30 min pela musica 15")
    pdf.bullet_point("Correlator usa janela de +-60s - com timestamps errados por minutos, nenhum peak e correto")
    pdf.ln(2)
    pdf.add_quote_box(
        'O card dira "Seu coracao disparou durante Creep" quando na verdade era "Karma Police". '
        "A feature principal do produto esta fundamentalmente quebrada."
    )

    pdf.subsection_title("2.3 PWA no iOS = produto morto para metade do mercado")
    pdf.body_text(
        "apple-health.ts confessa: 'In a PWA context, we rely on a companion iOS app or Apple "
        "Health export.' Usuarios de iPhone (60%+ do publico-alvo AB) precisam exportar XML "
        "manualmente - dealbreaker para MVP frictionless."
    )

    pdf.subsection_title("2.4 Correlacao =/= emocao (falacia central)")
    pdf.body_text(
        "HR sobe em shows por: posicao ortoestatica prolongada, alcool/cafeina, temperatura "
        "em multidoes, atividade fisica (pular, dancar), desidratacao. Nenhum desses e emocao."
    )
    pdf.add_quote_box(
        "Salimpoor et al. (2011, Nature Neuroscience): musica induz respostas autonomicas de "
        "~5-10 BPM - abaixo do threshold de z-score > 2.0. Picos detectados serao atividade fisica."
    )

    pdf.subsection_title("2.5 Over-engineering do stack")
    pdf.body_text(
        "Docker Compose sobe 5 servicos para: receber JSON de BPM, fazer conta estatistica, "
        "gerar PNG. TimescaleDB e para bilhoes de data points IoT. Um show de 3h = 10.800 pontos. "
        "1.000 usuarios = 10M pontos - Postgres vanilla resolve. Melhor: SQLite + FastAPI inline, "
        "Fly.io por $5/mes."
    )

    pdf.subsection_title("2.6 APIs externas frageis")
    pdf.bullet_point("Setlist.fm: cobertura pessima para sertanejo, funk, pagode")
    pdf.bullet_point("API-Football free: 100 req/dia - atende ~33 jogos. Brasileirao esgota no 1o sabado")
    pdf.bullet_point("Zero fallback: servicos retornam listas vazias silenciosamente")

    # ========== GTM E MONETIZACAO ==========
    pdf.section_title("3. Go-to-Market e Monetizacao")

    pdf.subsection_title("3.1 Aquisicao de usuarios")
    pdf.body_text(
        "CAC estimado: R$80-150 por usuario ativo. Marketing digital nao tem segmentacao para "
        "'tem Apple Watch E vai a shows'. Viralidade organica e a unica esperanca - mas o card "
        "compete com video do show e selfie com amigos, e perde."
    )

    pdf.subsection_title("3.2 Viralidade: Spotify Wrapped voce nao e")
    pdf.body_text(
        "O Wrapped funciona por: (a) base de 500M+ usuarios instalados, (b) constroi identidade "
        "pessoal ('meu top artista'), (c) momento cultural massivo. TumTum nao replica nenhum."
    )
    pdf.body_text(
        "Taxa de compartilhamento estimada: 10-15% na primeira experiencia, caindo para 3-5% nas subsequentes."
    )

    pdf.subsection_title("3.3 Modelos de receita - todos frageis")
    pdf.add_simple_table(
        ["Modelo", "Receita estimada", "Problema"],
        [
            ["Cards premium (R$4,90)", "~R$1.600/mes", "Nao paga servidor"],
            ["Assinatura mensal (R$14,90)", "Churn 30-40%", "Insustentavel"],
            ["Assinatura anual (R$79,90)", "LTV ~R$110-130", "Payback 8-12 meses"],
        ],
        [60, 56, 70],
    )

    pdf.subsection_title("3.4 Unit economics")
    pdf.body_text(
        "Uso 3-5x/ano: DAU/MAU abaixo de 5%. LTV freemium proximo de zero. "
        "Ratio LTV:CAC saudavel (3:1) exige CAC < R$40. Impossivel sem viralidade massiva."
    )

    pdf.subsection_title("3.5 A armadilha do hardware (Phase 1)")
    pdf.body_text(
        "Anatel: 6-12 meses. MOQ China: 5-10K unidades. Custo unitario: R$80-150. "
        "Investimento minimo: R$500K-1M. Se Phase 0 nao validar, e suicidio financeiro."
    )

    # ========== COMPETITIVA ==========
    pdf.section_title("4. Analise Competitiva")

    pdf.subsection_title("4.1 Por que ninguem fez isso?")
    pdf.body_text(
        "Apple, Garmin, Fitbit, Samsung coletam HR de centenas de milhoes de usuarios ha uma "
        "decada. Nenhuma construiu essa feature. Razoes: (a) demanda insuficiente em pesquisas, "
        "(b) correlacao HR+emocao imprecisa, (c) risco reputacional com dados de saude."
    )

    pdf.subsection_title("4.2 Risco de plataforma: dependencia existencial")
    pdf.body_text(
        'Apple pode em qualquer WWDC: lancar "Concert Mode" nativo, restringir acesso a HR '
        "para terceiros (ja fez com SpO2), ou rejeitar o app. Google e ainda mais imprevisivel."
    )

    pdf.subsection_title("4.3 Zero fossos defensaveis")
    pdf.add_simple_table(
        ["Tipo de Fosso", "TumTum Possui?"],
        [
            ["Tecnologia proprietaria", "Nao - z-score e estatistica basica"],
            ["Dados exclusivos", "Nao - dados pertencem as plataformas"],
            ["Efeitos de rede", "Nao - experiencia individual"],
            ["Marca", "Nao - pre-lancamento"],
            ["Custos de troca", "Nao - zero switching cost"],
            ["Regulatorio", "Nao - regulacao e barreira, nao vantagem"],
        ],
        [60, 126],
    )

    pdf.subsection_title("4.4 Feature, nao produto")
    pdf.body_text(
        "Tudo que o TumTum faz pode ser replicado como: feature do Apple Watch (3 meses), "
        "plugin do Spotify (2 meses), ou template Canva com HealthKit. Sem IP, sem dados, "
        "sem rede - nao ha nada a defender."
    )

    pdf.subsection_title("4.5 Startups similares que falharam")
    pdf.bullet_point("Feel (2016): Pulseira emocional. Levantou $1M, pivotou, morreu.")
    pdf.bullet_point("Moodmetric: Anel emocional. Sem tracao de consumidor.")
    pdf.bullet_point("Lightwave: Biometria de audiencias em shows. Pivotou para corporate.")
    pdf.bullet_point("Bionym/Nymi: Wearable biometrico. Pivotou para enterprise.")
    pdf.ln(2)
    pdf.bold_text("Padrao claro: dados biometricos emocionais fascinam em demo mas nao sustentam negocio B2C.")

    # ========== EXECUCAO + VEREDITO ==========
    pdf.section_title("5. Risco de Execucao")
    pdf.bullet_point("Fundador solo nao-tecnico aprendendo a programar enquanto constroi integracoes complexas")
    pdf.bullet_point("Stack de equipe de 4-5 seniors (Next.js + FastAPI + TimescaleDB + Celery + Redis + D3.js)")
    pdf.bullet_point("Estimativa de 14 semanas: irrealista por fator de 3-4x para dev experiente, 6-8x para iniciante")
    pdf.bullet_point("Risco alto de burnout antes do lancamento")

    pdf.ln(6)
    pdf.section_title("6. Veredito Final")

    # Big verdict box
    y = pdf.get_y()
    pdf.set_fill_color(*pdf.SURFACE)
    pdf.set_draw_color(*pdf.RED)
    pdf.rect(12, y, 186, 55, "FD")

    pdf.set_xy(16, y + 4)
    pdf.set_font("Helvetica", "B", 16)
    pdf.set_text_color(*pdf.RED_LIGHT)
    pdf.cell(0, 10, "NAO PROSSEGUIR NA FORMA ATUAL")
    pdf.ln(14)

    items = [
        "O problema nao existe - ninguem pede para ver HR de shows",
        "O mercado e minusculo - nicho de nicho (~20K usuarios em SP)",
        "A ciencia e fragil - HR =/= emocao, dados ruidosos",
        "A monetizacao nao fecha - uso 3-5x/ano nao sustenta negocio",
        "Zero fossos - qualquer big tech replica em 3 meses",
        "Startups similares falharam - padrao historico claro",
    ]
    pdf.set_font("Helvetica", "", 8)
    pdf.set_text_color(*pdf.TEXT)
    for i, item in enumerate(items, 1):
        pdf.set_x(18)
        pdf.cell(0, 5, f"{i}. {item}")
        pdf.ln(5)

    pdf.set_y(y + 62)

    pdf.subsection_title("Se insistir, pivote para:")
    pdf.bullet_point("B2B puro desde o dia 1: venda analytics de engajamento para produtoras/venues")
    pdf.bullet_point("SDK/Feature: licencie a tecnologia para Sympla, Ingresse, Eventim")
    pdf.bullet_point("Abandone hardware: Phase 1 com smart band e caminho para falencia")

    pdf.ln(4)
    pdf.subsection_title("Antes de qualquer codigo:")
    pdf.add_quote_box(
        "Faca 50 entrevistas com pessoas saindo de shows no Allianz Parque: "
        "'Voce pagaria R$5/mes para ver seu batimento cardiaco sincronizado com o jogo?' "
        "A resposta provavelmente sepulta ou salva o projeto - e custa muito menos que um MVP."
    )

    pdf.ln(8)
    pdf.set_font("Helvetica", "I", 8)
    pdf.set_text_color(*pdf.TEXT_MUTED)
    pdf.set_x(12)
    pdf.multi_cell(186, 4,
        "Este documento representa uma analise critica deliberadamente pessimista. "
        "Toda startup enfrenta ceticismo - mas os problemas identificados aqui sao "
        "estruturais, nao de execucao ou timing."
    )

    pdf.output("/home/user/tumtum/BUSINESS_REVIEW.pdf")
    print("PDF gerado: BUSINESS_REVIEW.pdf")


if __name__ == "__main__":
    build_pdf()
