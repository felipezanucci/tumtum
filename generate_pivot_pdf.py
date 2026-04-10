#!/usr/bin/env python3
"""Generate the TumTum Pulse Pivot Plan PDF - white/light background."""

from fpdf import FPDF


class PivotPDF(FPDF):
    # Colors - light professional theme
    RED = (192, 57, 43)
    RED_LIGHT = (231, 76, 60)
    DARK = (30, 30, 40)
    GRAY_800 = (50, 50, 60)
    GRAY_600 = (100, 100, 115)
    GRAY_400 = (160, 160, 175)
    GRAY_200 = (230, 230, 235)
    GRAY_100 = (245, 245, 248)
    WHITE = (255, 255, 255)
    CYAN = (0, 160, 200)
    GREEN = (39, 174, 96)

    def __init__(self):
        super().__init__()
        self.set_auto_page_break(auto=True, margin=20)

    def header(self):
        if self.page_no() > 1:
            self.set_fill_color(*self.GRAY_200)
            self.rect(0, 0, 210, 10, "F")
            self.set_font("Helvetica", "B", 6)
            self.set_text_color(*self.GRAY_600)
            self.set_y(2.5)
            self.cell(0, 5, "TUMTUM PULSE  |  PLANO DE PIVOT B2B  |  2026", align="C")
            self.set_draw_color(*self.RED)
            self.set_line_width(0.5)
            self.line(0, 10, 210, 10)
            self.ln(8)

    def footer(self):
        self.set_y(-12)
        self.set_font("Helvetica", "", 7)
        self.set_text_color(*self.GRAY_400)
        self.cell(0, 10, f"{self.page_no()}/{{nb}}", align="C")

    # --- Cover ---
    def add_cover(self):
        self.add_page()
        # Top red strip
        self.set_fill_color(*self.RED)
        self.rect(0, 0, 210, 6, "F")

        # Title block
        self.set_y(70)
        self.set_font("Helvetica", "B", 38)
        self.set_text_color(*self.DARK)
        self.cell(0, 16, "TUMTUM PULSE", align="C")
        self.ln(20)

        self.set_font("Helvetica", "", 14)
        self.set_text_color(*self.GRAY_600)
        self.cell(0, 8, "Plano de Pivot B2B", align="C")
        self.ln(6)
        self.set_font("Helvetica", "", 11)
        self.cell(0, 8, "Audience Engagement Analytics para Eventos ao Vivo", align="C")
        self.ln(25)

        # Verdict box
        self.set_x(30)
        self.set_fill_color(*self.GRAY_100)
        self.set_draw_color(*self.RED)
        self.rect(30, self.get_y(), 150, 40, "FD")
        self.set_y(self.get_y() + 6)
        self.set_font("Helvetica", "B", 12)
        self.set_text_color(*self.RED)
        self.cell(0, 8, "DE: App B2C de HR em shows (Score 2.5/10)", align="C")
        self.ln(10)
        self.set_font("Helvetica", "B", 12)
        self.set_text_color(*self.GREEN)
        self.cell(0, 8, "PARA: SaaS B2B de Engagement Analytics", align="C")
        self.ln(10)
        self.set_font("Helvetica", "", 9)
        self.set_text_color(*self.GRAY_600)
        self.cell(0, 6, "Meta Ano 1: 20 clientes  |  R$2.4M ARR  |  Margem ~80%", align="C")

        # Date
        self.set_y(210)
        self.set_font("Helvetica", "", 9)
        self.set_text_color(*self.GRAY_400)
        self.cell(0, 6, "Abril 2026  |  Baseado em analise de 4 startups comparaveis", align="C")

        # Bottom red strip
        self.set_fill_color(*self.RED)
        self.rect(0, 291, 210, 6, "F")

    # --- Helpers ---
    def chapter_title(self, num, text):
        if self.get_y() > 235:
            self.add_page()
        self.ln(4)
        y = self.get_y()
        self.set_fill_color(*self.RED)
        self.rect(10, y, 190, 13, "F")
        self.set_font("Helvetica", "B", 12)
        self.set_text_color(*self.WHITE)
        self.set_xy(14, y)
        self.cell(0, 13, f"CAPITULO {num}: {text.upper()}")
        self.ln(17)

    def section_title(self, text):
        if self.get_y() > 260:
            self.add_page()
        self.ln(3)
        y = self.get_y()
        self.set_fill_color(*self.GRAY_100)
        self.rect(10, y, 190, 9, "F")
        self.set_draw_color(*self.RED)
        self.set_fill_color(*self.RED)
        self.rect(10, y, 2, 9, "F")
        self.set_font("Helvetica", "B", 9)
        self.set_text_color(*self.DARK)
        self.set_xy(16, y)
        self.cell(0, 9, text)
        self.ln(12)

    def body(self, text):
        if self.get_y() > 272:
            self.add_page()
        self.set_font("Helvetica", "", 8.5)
        self.set_text_color(*self.GRAY_800)
        self.set_x(12)
        self.multi_cell(186, 4.5, text)
        self.ln(2)

    def bold_body(self, text):
        if self.get_y() > 272:
            self.add_page()
        self.set_font("Helvetica", "B", 8.5)
        self.set_text_color(*self.DARK)
        self.set_x(12)
        self.multi_cell(186, 4.5, text)
        self.ln(2)

    def bullet(self, text):
        if self.get_y() > 272:
            self.add_page()
        self.set_font("Helvetica", "", 8.5)
        self.set_text_color(*self.GRAY_800)
        self.set_x(16)
        self.cell(5, 4.5, "-")
        self.multi_cell(177, 4.5, text)
        self.ln(1)

    def numbered(self, num, text):
        if self.get_y() > 272:
            self.add_page()
        self.set_font("Helvetica", "B", 8.5)
        self.set_text_color(*self.RED)
        self.set_x(14)
        self.cell(8, 4.5, f"{num}.")
        self.set_font("Helvetica", "", 8.5)
        self.set_text_color(*self.GRAY_800)
        self.multi_cell(176, 4.5, text)
        self.ln(1)

    def quote_box(self, text):
        if self.get_y() > 258:
            self.add_page()
        y = self.get_y()
        self.set_font("Helvetica", "I", 8.5)
        self.set_text_color(*self.GRAY_600)
        lines = len(text) // 85 + 2
        h = max(lines * 5 + 6, 12)
        self.set_fill_color(*self.GRAY_100)
        self.rect(12, y, 186, h, "F")
        self.set_fill_color(*self.CYAN)
        self.rect(12, y, 2, h, "F")
        self.set_xy(18, y + 3)
        self.multi_cell(176, 4.5, text)
        self.set_y(y + h + 3)

    def table(self, headers, rows, col_widths=None):
        if self.get_y() > 242:
            self.add_page()
        if col_widths is None:
            w = 186 // len(headers)
            col_widths = [w] * len(headers)
        # Header
        self.set_fill_color(*self.RED)
        self.set_font("Helvetica", "B", 7)
        self.set_text_color(*self.WHITE)
        self.set_x(12)
        for i, h in enumerate(headers):
            self.cell(col_widths[i], 7, f"  {h}", border=1, fill=True)
        self.ln()
        # Rows
        self.set_font("Helvetica", "", 7)
        alt = False
        for row in rows:
            if alt:
                self.set_fill_color(*self.GRAY_100)
            else:
                self.set_fill_color(*self.WHITE)
            self.set_text_color(*self.GRAY_800)
            self.set_draw_color(*self.GRAY_200)
            self.set_x(12)
            for i, val in enumerate(row):
                self.cell(col_widths[i], 6, f"  {val}", border=1, fill=True)
            self.ln()
            alt = not alt
        self.ln(3)


def build():
    pdf = PivotPDF()
    pdf.alias_nb_pages()

    # ===================== COVER =====================
    pdf.add_cover()

    # ===================== EXECUTIVE SUMMARY =====================
    pdf.add_page()
    y = pdf.get_y()
    pdf.set_fill_color(*pdf.GRAY_100)
    pdf.rect(10, y, 190, 52, "F")
    pdf.set_fill_color(*pdf.RED)
    pdf.rect(10, y, 3, 52, "F")
    pdf.set_xy(18, y + 4)
    pdf.set_font("Helvetica", "B", 11)
    pdf.set_text_color(*pdf.DARK)
    pdf.cell(0, 6, "RESUMO EXECUTIVO")
    pdf.set_xy(18, y + 12)
    pdf.set_font("Helvetica", "", 8.5)
    pdf.set_text_color(*pdf.GRAY_800)
    pdf.multi_cell(176, 4.5,
        "A TumTum B2C enfrenta os mesmos problemas que mataram Moodmetric, Feel, Lightwave e Nymi. "
        "Nenhuma fez biometria emocional funcionar como produto de consumidor. "
        "Proposta: pivotar para TumTum Pulse, SaaS B2B de audience engagement analytics. "
        "Consumidor usa app gratis (coleta). Produtora/patrocinador paga pelo relatorio. "
        "Mercado brasileiro de shows: R$94 bi/ano (2o maior do mundo). "
        "Meta Ano 1: 20 clientes, R$2.4M ARR, margem ~80%. Investimento: R$30-50K."
    )
    pdf.set_y(y + 58)

    # ===================== CHAPTER 1 =====================
    pdf.chapter_title(1, "Autopsia cruzada - Licoes das 4 startups")

    pdf.section_title("Resumo dos 4 Casos")
    pdf.table(
        ["Startup", "Funding", "B2C?", "Resultado", "Status"],
        [
            ["Moodmetric", "Minimo", "Sim, falhou", "Nao escalou", "App morto (2024)"],
            ["Feel/Sentio", "~$33M", "Sim, falhou", "Pivot pharma", "Ativa B2B"],
            ["Lightwave", "Nenhum VC", "Nao tentou", "Projetos pontuais", "Morta (~2017)"],
            ["Nymi", "~$32M", "Sim, falhou", "Pivot enterprise", "Adquirida (2022)"],
        ],
        [30, 26, 28, 42, 60],
    )

    pdf.section_title("Os 7 Padroes de Fracasso")
    pdf.numbered(1, "Solucao fascinante, problema inexistente: todas criaram tech que impressiona em demo mas ninguem pediu")
    pdf.numbered(2, "B2C com wearable biometrico nao escala: nenhuma atingiu tracao de consumidor sustentavel")
    pdf.numbered(3, "Efeito novidade mata retencao: curioso uma vez, esquecido na terceira")
    pdf.numbered(4, "Hardware e armadilha mortal: multiplica custo, tempo e risco sem criar defensibilidade")
    pdf.numbered(5, "Correlacao biometrica = emocao e cientificamente fragil: HR sobe por exercicio, calor, alcool")
    pdf.numbered(6, "O pivot B2B salvou as que sobreviveram: Feel (pharma), Nymi (manufacturing)")
    pdf.numbered(7, "O mercado que paga e diferente do que fascina: utilidade > curiosidade")

    pdf.section_title("10 Aprendizados para a TumTum")
    pdf.numbered(1, "Nao construa hardware - use wearables existentes e foque em software/dados")
    pdf.numbered(2, "Nao venda para consumidor final - venda para quem tem orcamento e problema real")
    pdf.numbered(3, "Nao prometa medir emocao - prometa medir engajamento (mais honesto e acionavel)")
    pdf.numbered(4, "Receita recorrente ou morte - SaaS, nao projetos pontuais como Lightwave")
    pdf.numbered(5, "O dado agregado vale mais que o individual - 82% teve pico > seu HR foi 142")
    pdf.numbered(6, "Encontre quem sofre a dor - produtoras, patrocinadores, venues")
    pdf.numbered(7, "Comece pelo nicho que paga mais - grandes produtoras e marcas patrocinadoras")
    pdf.numbered(8, "Use middleware (Terra API) - nao construa integracoes custom para cada wearable")
    pdf.numbered(9, "Valide com dinheiro antes de codigo - venda o relatorio antes de construir o SaaS")
    pdf.numbered(10, "O produto viral e o relatorio, nao o app - se o dado surpreende, a midia cobre gratis")

    # ===================== CHAPTER 2 =====================
    pdf.chapter_title(2, "O pivot - TumTum Pulse")

    pdf.section_title("O Problema que Resolve")
    pdf.bold_body("Para produtoras (T4F, Live Nation, 30e, Opus):")
    pdf.bullet("O setlist funcionou? Qual musica gerou mais reacao?")
    pdf.bullet("Devemos trazer esse artista de novo?")
    pdf.bullet("Hoje a resposta e: feeling do produtor + redes sociais (impreciso, subjetivo, atrasado)")
    pdf.ln(1)
    pdf.bold_body("Para patrocinadores (Heineken, Itau, Vivo, Budweiser):")
    pdf.bullet("Nossa ativacao de marca engajou? Quanto vs. palco principal?")
    pdf.bullet("O ROI justifica renovar contrato? Mercado global de patrocinio: $70 bilhoes")
    pdf.ln(1)
    pdf.bold_body("Para venues e artistas:")
    pdf.bullet("Qual setor do estadio e mais engajado? Que tipo de evento funciona melhor?")
    pdf.bullet("Artistas: qual musica do setlist gera mais reacao biometrica?")

    pdf.section_title("Como Funciona - Fluxo Completo")
    pdf.bold_body("ANTES: Produtora cadastra evento. Integracao com Sympla. QR code gerado.")
    pdf.bold_body("DURANTE: Participantes com wearable abrem app gratis. HR coletado em background via Terra SDK.")
    pdf.bold_body("APOS (T+2h a T+24h): Pipeline agrega dados anonimos, correlaciona com timeline, detecta picos.")
    pdf.bold_body("ENTREGA: Dashboard web + relatorio PDF executivo + dados exportaveis.")

    pdf.section_title("Engagement Score (0-100) - Metrica Proprietaria")
    pdf.body(
        "Baseado em 4 componentes: HR_delta (variacao vs. baseline, peso 0.30), "
        "Peak_density (picos coletivos/hora, peso 0.25), Sync_rate (% com HR elevado simultaneamente, peso 0.25), "
        "Sustained_elevation (tempo em ativacao, peso 0.20). "
        "Nao promete medir emocao. Mede ativacao fisiologica coletiva. "
        "Agregado (noise individual filtrado pela media), relativo ao baseline (compensa calor/alcool), "
        "comparavel entre eventos (score normalizado)."
    )

    pdf.section_title("Modelo de Negocio e Pricing")
    pdf.table(
        ["Tier", "Preco", "Inclui", "Cliente Alvo"],
        [
            ["Starter", "R$2.000/evento", "1 relatorio, ate 500 monit.", "Produtoras regionais"],
            ["Pro", "R$8.000/mes", "10 eventos, dashboard, API", "Produtoras nacionais"],
            ["Enterprise", "R$25.000/mes", "Ilimitado, white-label", "T4F, Live Nation"],
            ["Sponsor Report", "R$5.000/relat.", "ROI dedicado p/ patrocinador", "Marcas, agencias"],
        ],
        [28, 34, 68, 56],
    )

    pdf.section_title("Unit Economics")
    pdf.table(
        ["Metrica", "Valor"],
        [
            ["Receita media/cliente/mes", "R$10.000"],
            ["Custo infra (Terra + servers)", "R$2.000/mes"],
            ["Margem bruta", "~80%"],
            ["Target ano 1 (20 clientes)", "R$2.4M ARR"],
            ["CAC (venda consultiva)", "R$5.000-10.000"],
            ["LTV (contrato anual)", "R$120.000"],
            ["LTV:CAC", "12-24x"],
        ],
        [80, 106],
    )

    pdf.section_title("Go-to-Market (GTM)")

    pdf.bold_body("Fase 1: Prova de Conceito (Meses 1-3) - Custo: R$0")
    pdf.bullet("Felipe + 30-50 amigos com wearable em 1 show grande em SP")
    pdf.bullet("Gerar relatorio manualmente (Python + Jupyter)")
    pdf.bullet("Entregavel: case study de 5-10 paginas com graficos e insights")

    pdf.bold_body("Fase 2: Cold Outreach (Meses 3-6) - Meta: 3 pilotos pagos")
    pdf.bullet("Abordar produtoras menores (30e, Opus), venues (Allianz, Vibra), agencias de ativacao")
    pdf.bullet("Canal: LinkedIn direto + email frio com case study")
    pdf.bullet("Pitch: Veja o que descobrimos sobre o engajamento no show do [Artista]. Quer isso pros seus?")

    pdf.bold_body("Fase 3: Primeiros Clientes (Meses 6-12)")
    pdf.bullet("Converter pilotos em contratos mensais (Pro tier)")
    pdf.bullet("Produtora inclui QR code TumTum no ingresso digital (mais dados)")
    pdf.bullet("PR: relatorios viram materia na Billboard Brasil, Rolling Stone, Meio & Mensagem")

    pdf.bold_body("Fase 4: Escala (Meses 12-24)")
    pdf.bullet("Integracao formal com Sympla (API publica). SDK em apps de ingressos")
    pdf.bullet("Expansao para futebol: 20 times, 380 jogos/ano no Brasileirao")
    pdf.bullet("Tier Enterprise para T4F, Live Nation Brasil")

    pdf.section_title("Stack Tecnico Simplificado")
    pdf.table(
        ["Componente", "TumTum Original", "TumTum Pulse"],
        [
            ["Backend", "FastAPI + Celery + Redis", "FastAPI sozinho"],
            ["Database", "PostgreSQL + TimescaleDB", "PostgreSQL vanilla"],
            ["Wearable", "Integracao custom", "Terra API ($399/mes)"],
            ["Mobile", "PWA (iOS quebrado)", "React Native (nativo)"],
            ["Frontend", "PWA mobile-first", "Next.js desktop-first"],
            ["Docker", "5 servicos", "2 servicos"],
            ["Custo/mes", "$100-200", "$50-80 + $399 Terra"],
            ["Timeline MVP", "14 semanas (irreal)", "6-8 semanas (real)"],
        ],
        [32, 70, 84],
    )

    pdf.section_title("LGPD e Privacidade")
    pdf.body(
        "Vantagem estrutural: dados entregues ao B2B sao agregados e anonimos. "
        "Art. 12 da LGPD: dados anonimizados estao fora do escopo da lei. "
        "Nenhum cliente ve HR individual. App coleta com consentimento, "
        "mas entrega apenas medias, percentuais e scores. "
        "Sem PII (nome, email) na camada de coleta - apenas UUID anonimo."
    )

    pdf.section_title("Riscos e Mitigacoes")
    pdf.table(
        ["Risco", "Prob.", "Mitigacao"],
        [
            ["Poucos usuarios com wearable", "Alta", "Parceria c/ produtora p/ distribuir messaging"],
            ["Produtoras nao veem valor", "Media", "Case study gratis no 1o evento"],
            ["Terra API muda pricing", "Baixa", "Integracao direta como fallback"],
            ["Amostra pequena", "Alta", "Comunicar como amostra, nao censo"],
            ["Concorrente copia", "Media", "Ser 1o no BR + dados historicos = moat"],
            ["Ciclo de venda longo", "Alta", "Tier por evento (R$2K), sem contrato"],
        ],
        [52, 16, 118],
    )

    # ===================== CHAPTER 3 =====================
    pdf.chapter_title(3, "Cronograma e proximos passos")

    pdf.section_title("Roadmap de Execucao")
    pdf.table(
        ["Periodo", "Foco", "Entregavel"],
        [
            ["Mes 1-2", "App mobile MVP", "React Native + Terra SDK, coleta HR background"],
            ["Mes 2-3", "Backend + pipeline", "FastAPI + script agregacao + Engagement Score"],
            ["Mes 3-4", "Dashboard web MVP", "Next.js com visualizacao de relatorio"],
            ["Mes 4", "Primeiro teste real", "30-50 pessoas em show = Case Study #1"],
            ["Mes 5-6", "Cold outreach", "3 pilotos pagos (R$2K/evento)"],
            ["Mes 7-8", "Iterar produto", "Dashboard v2, alertas, comparacao historica"],
            ["Mes 9-12", "Escalar", "10-20 clientes, integracao Sympla, futebol"],
        ],
        [24, 50, 112],
    )

    pdf.section_title("Investimento Total Ano 1")
    pdf.table(
        ["Item", "Custo Estimado"],
        [
            ["Terra API (12 meses)", "R$25.000"],
            ["Infra (Railway, Vercel, dominio)", "R$5.000"],
            ["Apple Developer + Google Play", "R$1.500"],
            ["Custos operacionais (eventos, transporte)", "R$5.000"],
            ["Marketing (LinkedIn Ads, materiais)", "R$5.000"],
            ["Reserva / contingencia", "R$8.500"],
            ["TOTAL", "R$50.000"],
        ],
        [100, 86],
    )

    pdf.section_title("O Primeiro Passo: Antes de Qualquer Codigo")
    pdf.quote_box(
        "Va a 1 show no Allianz Parque com 5 amigos que tenham Apple Watch. "
        "Exporte os dados de HR do HealthKit manualmente (XML). "
        "Cruze com o setlist do show (Setlist.fm). "
        "Monte 1 grafico no Google Sheets mostrando HR medio vs. musicas. "
        "Se o grafico contar uma historia interessante, voce tem um negocio. "
        "Se nao contar, voce economizou 12 meses de trabalho. "
        "Custo: R$0. Tempo: 1 fim de semana."
    )

    pdf.ln(6)
    pdf.set_font("Helvetica", "I", 8)
    pdf.set_text_color(*pdf.GRAY_400)
    pdf.set_x(12)
    pdf.multi_cell(186, 4,
        "Este plano foi construido a partir da analise detalhada de 4 startups comparaveis "
        "(Moodmetric, Feel, Lightwave, Nymi) e dos padroes de fracasso identificados no "
        "mercado de biometria emocional B2C. O pivot B2B nao garante sucesso, mas elimina "
        "os erros estruturais que mataram todas as tentativas anteriores."
    )

    pdf.output("/home/user/tumtum/TUMTUM_PULSE_PIVOT_PLAN.pdf")
    print("PDF gerado: TUMTUM_PULSE_PIVOT_PLAN.pdf")


if __name__ == "__main__":
    build()
