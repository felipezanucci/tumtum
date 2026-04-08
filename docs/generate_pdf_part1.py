#!/usr/bin/env python3
"""TumTum Pricing Strategy PDF Generator - Part 1: Base + Chapter 1"""

from fpdf import FPDF
import pickle
import os

# Brand colors
RED = (192, 57, 43)       # #C0392B
DARK_BG = (8, 8, 12)      # #08080C
SURFACE = (17, 17, 24)    # #111118
WHITE = (240, 240, 245)   # #F0F0F5
MUTED = (107, 107, 128)   # #6B6B80
CYAN = (0, 210, 255)      # #00D2FF
DARK_RED = (120, 30, 20)


class TumTumPDF(FPDF):
    def __init__(self):
        super().__init__()
        self.set_auto_page_break(auto=True, margin=25)

    def header(self):
        if self.page_no() == 1:
            return
        self.set_font("Helvetica", "B", 8)
        self.set_text_color(*MUTED)
        self.cell(0, 10, "TumTum - Estrategia de Precificacao | Abril 2026", align="R")
        self.ln(5)

    def footer(self):
        self.set_y(-15)
        self.set_font("Helvetica", "", 8)
        self.set_text_color(*MUTED)
        self.cell(0, 10, f"Pagina {self.page_no()}", align="C")

    def cover_page(self):
        self.add_page()
        self.set_fill_color(*DARK_BG)
        self.rect(0, 0, 210, 297, "F")

        # Red accent bar
        self.set_fill_color(*RED)
        self.rect(0, 80, 210, 4, "F")

        # Title
        self.set_y(95)
        self.set_font("Helvetica", "B", 36)
        self.set_text_color(*WHITE)
        self.cell(0, 18, "TUMTUM", align="C", new_x="LMARGIN", new_y="NEXT")

        self.set_font("Helvetica", "", 20)
        self.set_text_color(*CYAN)
        self.cell(0, 12, "Estrategia de Precificacao", align="C", new_x="LMARGIN", new_y="NEXT")

        self.ln(8)
        self.set_font("Helvetica", "", 13)
        self.set_text_color(*MUTED)
        self.cell(0, 8, "Mercado Brasileiro | Esportes & Entretenimento", align="C", new_x="LMARGIN", new_y="NEXT")

        # Bottom info
        self.set_y(220)
        self.set_font("Helvetica", "", 11)
        self.set_text_color(*WHITE)
        lines = [
            "Versao 1.0 | Abril 2026",
            "Segmentos: Futebol + Shows/Festivais",
            "Modelo: Freemium + Hibrido",
            "",
            "Documento Confidencial",
        ]
        for line in lines:
            self.cell(0, 7, line, align="C", new_x="LMARGIN", new_y="NEXT")

        # Bottom red bar
        self.set_fill_color(*RED)
        self.rect(0, 275, 210, 2, "F")

    def toc_page(self):
        self.add_page()
        self.set_font("Helvetica", "B", 22)
        self.set_text_color(*RED)
        self.cell(0, 14, "Indice", new_x="LMARGIN", new_y="NEXT")
        self.ln(6)

        chapters = [
            ("1", "Resumo Executivo e Analise do Modelo de Precificacao"),
            ("2", "Analise de Valor para o Cliente e Disposicao a Pagar"),
            ("3", "Estrutura de Planos, Nomes e Precos"),
            ("4", "Matriz de Alocacao de Funcionalidades"),
            ("5", "Modelo de Revenue Share com Artistas e Atletas"),
            ("6", "Projecoes Financeiras e Unit Economics"),
        ]

        self.set_font("Helvetica", "", 12)
        self.set_text_color(50, 50, 50)
        for num, title in chapters:
            self.cell(0, 10, f"Capitulo {num} - {title}", new_x="LMARGIN", new_y="NEXT")
        self.ln(10)

    def chapter_title(self, num, title):
        self.add_page()
        self.set_fill_color(*RED)
        self.rect(10, 15, 190, 1.5, "F")
        self.ln(5)
        self.set_font("Helvetica", "B", 11)
        self.set_text_color(*RED)
        self.cell(0, 8, f"CAPITULO {num}", new_x="LMARGIN", new_y="NEXT")
        self.set_font("Helvetica", "B", 20)
        self.set_text_color(30, 30, 30)
        self.cell(0, 12, title, new_x="LMARGIN", new_y="NEXT")
        self.set_fill_color(*RED)
        self.rect(10, self.get_y() + 2, 40, 1, "F")
        self.ln(8)

    def section_title(self, text):
        self.ln(4)
        self.set_font("Helvetica", "B", 14)
        self.set_text_color(*RED)
        self.cell(0, 10, text, new_x="LMARGIN", new_y="NEXT")
        self.ln(2)

    def subsection_title(self, text):
        self.ln(2)
        self.set_font("Helvetica", "B", 11)
        self.set_text_color(50, 50, 50)
        self.cell(0, 8, text, new_x="LMARGIN", new_y="NEXT")
        self.ln(1)

    def body_text(self, text):
        self.set_font("Helvetica", "", 10)
        self.set_text_color(40, 40, 40)
        self.multi_cell(0, 5.5, text)
        self.ln(2)

    def bold_text(self, text):
        self.set_font("Helvetica", "B", 10)
        self.set_text_color(40, 40, 40)
        self.multi_cell(0, 5.5, text)
        self.ln(2)

    def bullet(self, text):
        self.set_font("Helvetica", "", 10)
        self.set_text_color(40, 40, 40)
        x = self.l_margin
        self.set_x(x)
        self.cell(6, 5.5, "-")
        self.multi_cell(self.w - self.r_margin - self.get_x(), 5.5, text)
        self.ln(1)

    def table(self, headers, rows, col_widths=None):
        if col_widths is None:
            w = (self.w - 20) / len(headers)
            col_widths = [w] * len(headers)

        # Header
        self.set_font("Helvetica", "B", 9)
        self.set_fill_color(*RED)
        self.set_text_color(255, 255, 255)
        for i, h in enumerate(headers):
            self.cell(col_widths[i], 8, h, border=1, fill=True, align="C")
        self.ln()

        # Rows
        self.set_font("Helvetica", "", 9)
        self.set_text_color(40, 40, 40)
        fill = False
        for row in rows:
            max_h = 8
            # Calculate needed height
            for i, cell_text in enumerate(row):
                lines = self.multi_cell(col_widths[i], 5, str(cell_text), split_only=True)
                needed = len(lines) * 5
                if needed > max_h:
                    max_h = needed

            if fill:
                self.set_fill_color(245, 245, 250)
            else:
                self.set_fill_color(255, 255, 255)

            for i, cell_text in enumerate(row):
                x = self.get_x()
                y = self.get_y()
                self.rect(x, y, col_widths[i], max_h, "DF" if True else "D")
                self.set_xy(x + 1, y + 1)
                self.multi_cell(col_widths[i] - 2, 5, str(cell_text))
                self.set_xy(x + col_widths[i], y)
            self.ln(max_h)
            fill = not fill
        self.ln(4)

    def highlight_box(self, text, color=RED):
        self.set_fill_color(color[0], color[1], color[2])
        self.set_draw_color(color[0], color[1], color[2])
        x = self.get_x()
        y = self.get_y()
        self.rect(x, y, 3, 20, "F")
        self.set_xy(x + 6, y + 2)
        self.set_font("Helvetica", "B", 10)
        self.set_text_color(color[0], color[1], color[2])
        self.multi_cell(self.w - 30, 5.5, text)
        self.ln(4)


def build_chapter1(pdf):
    """Chapter 1: Resumo Executivo e Analise do Modelo"""
    pdf.chapter_title("1", "Resumo Executivo e Analise do Modelo de Precificacao")

    pdf.section_title("1.1 Resumo Executivo")
    pdf.body_text(
        "O TumTum transforma emocoes de eventos ao vivo em conteudo compartilhavel, "
        "capturando dados de frequencia cardiaca durante shows e jogos de futebol. "
        "Este documento define a estrategia de precificacao ideal para o mercado brasileiro, "
        "equilibrando simplicidade, viralidade e crescimento de receita."
    )

    pdf.bold_text("Recomendacao principal: Freemium + Monetizacao Hibrida")

    pdf.bullet("Tier gratuito generoso que alimenta o loop viral (cards nas redes sociais)")
    pdf.bullet("Um unico plano premium (TumTum Pro) a R$ 14,90/mes ou R$ 119,90/ano")
    pdf.bullet("Microtransacoes para nao-assinantes (R$ 4,90 por card premium)")
    pdf.bullet("Revenue share com artistas/atletas de 75/25 (artista recebe 75%)")
    pdf.bullet("Insights de dados B2B como receita futura")
    pdf.ln(4)

    pdf.table(
        ["Criterio", "Recomendacao", "Racional"],
        [
            ["Simplicidade", "2 planos (Free + Pro)", "Sem fadiga de decisao. Consumidor BR abandona precificacao complexa."],
            ["Viralidade", "Cards gratis com marca d'agua", "Cada card compartilhado = marketing gratuito. Paywall mata viralidade."],
            ["Captura de receita", "Assinatura + microtx", "Captura tanto receita recorrente quanto compras por impulso."],
            ["Escalabilidade", "Uso cresce com eventos", "Mais eventos = mais cards = mais valor = maior propensao a upgrade."],
        ],
        [45, 50, 95],
    )

    pdf.section_title("1.2 Analise dos Modelos de Precificacao")
    pdf.body_text(
        "Avaliamos cinco modelos de precificacao contra o contexto especifico do TumTum: "
        "um app brasileiro de consumo usado periodicamente (orientado a eventos, nao diario), "
        "onde viralidade via compartilhamento social e o principal motor de crescimento."
    )

    # Model 1
    pdf.subsection_title("Modelo 1: Assinatura Pura (Mensal/Anual)")
    pdf.body_text(
        "Pros: Receita recorrente previsivel, simples de entender, padrao de mercado. "
        "Contras: Dificil justificar para uso periodico (1-4 eventos/mes), alto risco de churn entre temporadas, "
        "fadiga de assinaturas no Brasil (consumidor medio tem 2,4 assinaturas pagas)."
    )
    pdf.bold_text("Veredito: Parcialmente adotado - assinatura e um pilar de receita, mas nao o unico.")

    # Model 2
    pdf.subsection_title("Modelo 2: Por Evento / Por Card (Transacional Puro)")
    pdf.body_text(
        "Pros: Pague pelo que usa, parece justo, baixa barreira de compromisso. "
        "Contras: Receita imprevisivel, sem base recorrente, usuarios frequentes se sentem punidos."
    )
    pdf.bold_text("Veredito: Parcialmente adotado - microtransacoes complementam assinaturas para usuarios casuais.")

    # Model 3
    pdf.subsection_title("Modelo 3: Freemium com Feature Gating")
    pdf.body_text(
        "Pros: Topo de funil massivo, loop viral intacto no tier gratuito, modelo comprovado no Brasil "
        "(Cartola FC, Spotify). Contras: Deve escolher cuidadosamente o que restringir, risco de tier "
        "gratuito 'bom o suficiente' matar conversoes."
    )
    pdf.bold_text("Veredito: ADOTADO como modelo central. Tier gratuito util mas com gatilhos claros de upgrade.")

    # Model 4
    pdf.subsection_title("Modelo 4: Assinatura com Multiplos Planos (3+ tiers)")
    pdf.body_text(
        "Pros: Captura diferentes disposicoes a pagar. "
        "Contras: Adiciona complexidade, excessivo para app de consumo na Fase 0, "
        "consumidores brasileiros nao comparam 3 planos."
    )
    pdf.bold_text("Veredito: REJEITADO para Fase 0. Um unico plano premium e mais simples.")

    # Model 5
    pdf.subsection_title("Modelo 5: Tier Gratuito com Publicidade")
    pdf.body_text(
        "Pros: Monetiza usuarios nao-pagantes. "
        "Contras: CPMs brasileiros sao baixos (R$ 3-8 CPM), anuncios destroem a marca premium/emocional, "
        "intrusivos durante a experiencia de 'reviver seu momento'."
    )
    pdf.bold_text("Veredito: REJEITADO. A marca do TumTum e 'premium, emocional, noturna'. Anuncios quebram isso.")

    pdf.section_title("1.3 Modelo Recomendado: Freemium + Monetizacao Hibrida")
    pdf.body_text(
        "O modelo hibrido funciona para o TumTum porque:"
    )
    pdf.bullet(
        "Padrao de uso orientado a eventos: Diferente do Spotify (uso diario), TumTum e usado em torno de eventos. "
        "Assinatura pura parece pagar por nada entre eventos. Microtransacoes capturam valor de usuarios ocasionais."
    )
    pdf.bullet(
        "Viralidade depende de compartilhamento gratuito: O card e o mecanismo #1 de crescimento. "
        "Se voce coloca paywall na geracao de cards, o crescimento morre."
    )
    pdf.bullet(
        "Realidades do mercado brasileiro: Com salario minimo de R$ 1.518/mes, uma assinatura de R$ 14,90 = ~1% do salario. "
        "Este e o teto para um app de uso nao-diario. Microtransacoes de R$ 4,90 estao na zona de compra por impulso."
    )
    pdf.bullet(
        "Valor escala com uso: Usuarios que frequentam mais eventos geram mais cards, atingem limites gratuitos "
        "com mais frequencia e tem motivacao mais forte para assinar."
    )
    pdf.ln(4)

    pdf.section_title("1.4 Captura de Valor em Escala")
    pdf.table(
        ["Fase", "Usuarios", "Receita Primaria", "Receita Secundaria"],
        [
            ["Lancamento (0-50K)", "Early adopters", "Microtransacoes", "-"],
            ["Crescimento (50K-500K)", "Mainstream", "Assinaturas (5-8% conv.)", "Microtransacoes"],
            ["Escala (500K-2M)", "Massa", "Assinaturas + microtx", "B2B data, cards patrocinados"],
            ["Dominancia (2M+)", "Plataforma", "Toda receita consumer", "Parcerias artistas/atletas"],
        ],
        [40, 30, 55, 65],
    )

    return pdf


if __name__ == "__main__":
    # Build PDF with cover + TOC + Chapter 1
    pdf = TumTumPDF()
    pdf.cover_page()
    pdf.toc_page()
    pdf = build_chapter1(pdf)

    # Save intermediate state
    with open("/tmp/tumtum_pdf_state.pkl", "wb") as f:
        pickle.dump(pdf, f)

    print("Chapter 1 done. PDF state saved.")
