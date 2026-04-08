#!/usr/bin/env python3
"""TumTum Pricing Strategy PDF Generator - Part 6: Chapter 6 + Final Output"""

import pickle
import sys
sys.path.insert(0, "/home/user/tumtum/docs")
from generate_pdf_part1 import TumTumPDF, RED, CYAN

with open("/tmp/tumtum_pdf_state.pkl", "rb") as f:
    pdf = pickle.load(f)


def build_chapter6(pdf):
    """Chapter 6: Projecoes Financeiras e Unit Economics"""
    pdf.chapter_title("6", "Projecoes Financeiras e Unit Economics")

    pdf.section_title("6.1 Unit Economics - Resumo")

    pdf.subsection_title("Receita Por Usuario Por Mes (RPUPM) por Segmento")
    pdf.table(
        ["Tipo de Usuario", "% dos Usuarios", "Receita Mensal", "Calculo"],
        [
            ["Free (sem compra)", "75-80%", "R$ 0", "Valor viral apenas (cards = marketing gratis)"],
            ["Free + microtx (comprador ocasional)", "12-15%", "R$ 4,90-7,90", "~1 card premium ou comparacao/mes"],
            ["Pro Mensal", "3-5%", "R$ 14,90", "Assinatura completa"],
            ["Pro Anual", "3-5%", "R$ 9,99 (efetivo)", "R$ 119,90/ano cobrado anualmente"],
        ],
        [45, 25, 30, 90],
    )

    pdf.subsection_title("ARPU Combinado (Receita Media Por Usuario)")
    pdf.body_text("Cenario: 100.000 Usuarios Ativos Mensais (MAU)")
    pdf.table(
        ["Metrica", "Valor"],
        [
            ["78.000 usuarios gratis (78%)", "R$ 0"],
            ["12.000 compradores ocasionais (12%)", "R$ 5,90 med x 12.000 = R$ 70.800"],
            ["4.000 Pro Mensal (4%)", "R$ 14,90 x 4.000 = R$ 59.600"],
            ["6.000 Pro Anual (6%)", "R$ 9,99 x 6.000 = R$ 59.940"],
            ["RECEITA MENSAL TOTAL", "R$ 190.340"],
            ["ARPU combinado (todos os usuarios)", "R$ 1,90/mes"],
            ["ARPU combinado (apenas pagantes)", "R$ 8,65/mes"],
            ["Taxa de conversao paga", "22% (10% assinatura + 12% microtx)"],
        ],
        [60, 130],
    )

    pdf.subsection_title("Valor do Tempo de Vida do Cliente (LTV)")
    pdf.table(
        ["Tipo", "Receita Mensal", "Vida Media", "LTV", "LTV Apos Custos (80% margem)"],
        [
            ["Free", "R$ 0", "8 meses", "R$ 0", "R$ 0 (mas gera viralidade)"],
            ["Microtx", "R$ 5,90", "6 meses", "R$ 35,40", "R$ 28,32"],
            ["Pro Mensal", "R$ 14,90", "5 meses", "R$ 74,50", "R$ 59,60"],
            ["Pro Anual", "R$ 9,99", "14 meses", "R$ 139,86", "R$ 111,89"],
        ],
        [25, 25, 22, 25, 93],
    )

    pdf.highlight_box(
        "Assinantes anuais tem 2x o LTV dos mensais apesar de pagar 33% menos por mes. "
        "Cobranca anual reduz dramaticamente o churn (sem decisao mensal de 'devo cancelar?')."
    )

    # Revenue projections
    pdf.section_title("6.2 Projecoes de Receita - Primeiros 24 Meses")

    pdf.subsection_title("Premissas")
    pdf.table(
        ["Variavel", "Valor", "Base"],
        [
            ["Data de lancamento", "Q3 2026", "Apos validacao do MVP Fase 0"],
            ["Usuarios iniciais (Mes 1)", "5.000", "Lancamento beta com 2-3 parcerias de eventos"],
            ["Taxa de crescimento mensal", "25-40% (inicio), 15-20% (maduro)", "Coeficiente viral de cards compartilhados"],
            ["Conversao para Pro", "8% do MAU (na maturidade)", "Benchmark: Strava ~10%, apps de saude media ~5%"],
            ["Conversao microtx", "12% dos usuarios gratis (por mes)", "Benchmark: gaming 5-15%, FutebolCard ~10%"],
            ["Churn mensal (Pro Mensal)", "12%", "Media de apps de saude/fitness"],
            ["Churn anual (Pro Anual)", "25% ao ano (~2,3%/mes)", "Melhor que media por temporadas de eventos"],
            ["Ratio Anual:Mensal", "60:40", "Push agressivo de anual com 33% de desconto"],
        ],
        [45, 45, 100],
    )

    pdf.subsection_title("Projecao Mensal - Ano 1")
    pdf.table(
        ["Mes", "MAU", "Assinantes", "Compradores Microtx", "Receita Assin.", "Receita Microtx", "Total"],
        [
            ["1", "5.000", "150", "350", "R$ 1.836", "R$ 2.065", "R$ 3.901"],
            ["3", "10.000", "500", "750", "R$ 6.126", "R$ 4.425", "R$ 10.551"],
            ["6", "32.000", "2.400", "2.700", "R$ 29.405", "R$ 15.930", "R$ 45.335"],
            ["9", "80.000", "6.400", "7.200", "R$ 78.413", "R$ 42.480", "R$ 120.893"],
            ["12", "150.000", "12.000", "13.500", "R$ 147.024", "R$ 79.650", "R$ 226.674"],
        ],
        [12, 18, 22, 28, 30, 30, 50],
    )

    pdf.bold_text("Receita Total Ano 1: ~R$ 950.000 (~R$ 1M)")

    pdf.subsection_title("Projecao Ano 2 (com comparacao de artistas)")
    pdf.table(
        ["Metrica", "Mes 13", "Mes 18", "Mes 24"],
        [
            ["MAU", "180.000", "350.000", "600.000"],
            ["Assinantes Pro", "14.400", "31.500", "54.000"],
            ["Receita microtx/mes", "R$ 95.580", "R$ 206.500", "R$ 354.000"],
            ["Receita assinaturas/mes", "R$ 176.429", "R$ 385.939", "R$ 661.608"],
            ["Receita comparacao artista/mes", "R$ 15.000", "R$ 85.000", "R$ 220.000"],
            ["RECEITA TOTAL/MES", "R$ 287.009", "R$ 677.439", "R$ 1.235.608"],
        ],
        [50, 45, 45, 50],
    )

    pdf.bold_text("Receita Total Ano 2: ~R$ 8,5M")
    pdf.bold_text("Receita Acumulada 24 meses: ~R$ 9,5M")

    # Cost structure
    pdf.section_title("6.3 Estrutura de Custos e Margens")

    pdf.subsection_title("Custos Variaveis (Por Usuario Por Mes)")
    pdf.table(
        ["Custo", "Usuario Free", "Assinante Pro", "Comprador Microtx"],
        [
            ["Infraestrutura (servidores, DB, CDN)", "R$ 0,08", "R$ 0,15", "R$ 0,10"],
            ["Geracao de cards (compute)", "R$ 0,05", "R$ 0,25", "R$ 0,10"],
            ["Processamento de pagamento (3-5%)", "R$ 0", "R$ 0,60", "R$ 0,25"],
            ["Custos de API de wearables", "R$ 0,02", "R$ 0,02", "R$ 0,02"],
            ["APIs externas (Setlist.fm, API-Football)", "R$ 0,01", "R$ 0,01", "R$ 0,01"],
            ["CUSTO VARIAVEL TOTAL", "R$ 0,16", "R$ 1,03", "R$ 0,48"],
            ["RECEITA", "R$ 0", "R$ 12,25 med", "R$ 5,90 med"],
            ["MARGEM BRUTA", "N/A", "~92%", "~92%"],
        ],
        [55, 35, 35, 65],
    )

    pdf.subsection_title("Custos Fixos (Mensais, em Escala)")
    pdf.table(
        ["Custo", "Mes 6", "Mes 12", "Mes 24"],
        [
            ["Equipe de engenharia (3-5 devs)", "R$ 60.000", "R$ 80.000", "R$ 150.000"],
            ["Infraestrutura cloud", "R$ 5.000", "R$ 15.000", "R$ 45.000"],
            ["Marketing e aquisicao", "R$ 10.000", "R$ 30.000", "R$ 80.000"],
            ["Parcerias artistas/atletas (adiantamentos)", "R$ 0", "R$ 15.000", "R$ 50.000"],
            ["Pagamentos de revenue share", "R$ 0", "R$ 5.000", "R$ 165.000"],
            ["Operacoes e suporte", "R$ 5.000", "R$ 10.000", "R$ 25.000"],
            ["CUSTOS FIXOS TOTAIS", "R$ 80.000", "R$ 155.000", "R$ 515.000"],
        ],
        [55, 35, 35, 65],
    )

    pdf.subsection_title("Caminho para Lucratividade")
    pdf.table(
        ["Marco", "Quando", "MAU", "Receita/Mes", "Custos/Mes", "Resultado"],
        [
            ["Break-even (margem de contribuicao)", "Mes 4-5", "15-22K", "R$ 18-29K", "R$ 15-25K", "~R$ 0"],
            ["Break-even operacional", "Mes 10-12", "100-150K", "R$ 150-227K", "R$ 130-155K", "R$ 20-72K"],
            ["Lucratividade forte", "Mes 18-24", "350-600K", "R$ 677K-1,2M", "R$ 350-515K", "R$ 327-720K"],
        ],
        [35, 18, 20, 30, 30, 57],
    )

    # Key metrics
    pdf.section_title("6.4 Metricas-Chave para Acompanhar")

    pdf.subsection_title("Metricas North Star")
    pdf.table(
        ["Metrica", "Meta Mes 6", "Meta Mes 12", "Meta Mes 24"],
        [
            ["MAU", "32.000", "150.000", "600.000"],
            ["Taxa de conversao paga (assinatura + microtx)", "16%", "22%", "25%"],
            ["Taxa de assinatura Pro", "6%", "8%", "9%"],
            ["Churn mensal (Pro Mensal)", "<15%", "<12%", "<10%"],
            ["Churn anual (Pro Anual)", "<30%", "<25%", "<20%"],
            ["ARPU combinado (todos usuarios)", "R$ 1,42", "R$ 1,90", "R$ 2,06"],
            ["Cards compartilhados por usuario/evento", "1,2", "1,5", "1,8"],
            ["Coeficiente viral (novos usuarios/compartilhamento)", "0,15", "0,20", "0,25"],
        ],
        [55, 35, 35, 65],
    )

    # Risks
    pdf.section_title("6.5 Fatores de Risco e Mitigacoes")
    pdf.table(
        ["Risco", "Prob.", "Impacto", "Mitigacao"],
        [
            ["Fadiga de assinatura (cancelam apos 2-3 meses)", "Alta", "Medio", "Push agressivo de planos anuais. Criar valor sazonal (Brasileirao, festivais)."],
            ["Microtx canibaliza assinaturas", "Media", "Medio", "Monitorar de perto. Se ocorrer, limitar microtx a 2/mes para gratis."],
            ["Artistas demandam share maior", "Media", "Baixo", "Contratos com clausula de protecao. Novos contratos negociados a taxa de mercado."],
            ["Concorrente copia o conceito", "Baixa", "Alto", "Velocidade + parcerias exclusivas = moat. Vantagem de first-mover no Brasil."],
            ["Baixa penetracao de wearables no Brasil", "Media", "Alto", "Focar em Classes A/B que tem wearables. Pulseira TumTum na Fase 1 resolve."],
            ["Problemas com processamento de pagamento", "Baixa", "Medio", "Usar processador local (Stripe BR, Pagar.me, Mercado Pago) com Pix/boleto."],
        ],
        [38, 15, 15, 122],
    )

    # Final summary
    pdf.section_title("6.6 Blueprint Final de Precificacao")

    pdf.ln(2)
    pdf.set_fill_color(245, 245, 250)
    pdf.set_draw_color(*RED)
    y_start = pdf.get_y()
    pdf.rect(10, y_start, 190, 95, "D")

    pdf.set_x(15)
    pdf.set_font("Helvetica", "B", 14)
    pdf.set_text_color(*RED)
    pdf.cell(180, 10, "TUMTUM - BLUEPRINT DE PRECIFICACAO", new_x="LMARGIN", new_y="NEXT")

    items = [
        ("MODELO:", "Freemium + Hibrido (Assinatura + Microtransacoes)"),
        ("PLANOS:", "2 - TumTum (Gratis) + TumTum Pro"),
        ("PRO MENSAL:", "R$ 14,90/mes"),
        ("PRO ANUAL:", "R$ 119,90/ano (R$ 9,99/mes)"),
        ("PASSE SAZONAL:", "R$ 79,90/temporada (futebol)"),
        ("CARD PREMIUM:", "R$ 4,90 (microtx)"),
        ("CARD ANIMADO:", "R$ 6,90 (microtx)"),
        ("COMPARACAO ARTISTA:", "R$ 7,90/evento (microtx)"),
        ("REVENUE SHARE:", "75% artista / 25% TumTum"),
        ("META MES 12:", "150K MAU, R$ 227K/mes receita"),
        ("META MES 24:", "600K MAU, R$ 1,2M/mes receita"),
        ("PAGAMENTOS:", "Pix, Cartao (parcelamento), Boleto"),
    ]

    for label, value in items:
        pdf.set_x(15)
        pdf.set_font("Helvetica", "B", 9)
        pdf.set_text_color(80, 80, 80)
        pdf.cell(40, 6, label)
        pdf.set_font("Helvetica", "", 9)
        pdf.set_text_color(40, 40, 40)
        pdf.cell(140, 6, value, new_x="LMARGIN", new_y="NEXT")

    pdf.ln(8)
    pdf.highlight_box(
        "PRINCIPIO CENTRAL: Tier gratis alimenta viralidade. Tier Pro captura valor. "
        "Mantenha simples. Duas opcoes. Sem friccao."
    )

    return pdf


pdf = build_chapter6(pdf)

# Generate final PDF
output_path = "/home/user/tumtum/docs/tumtum-estrategia-precificacao.pdf"
pdf.output(output_path)
print(f"PDF gerado com sucesso: {output_path}")

import os
size = os.path.getsize(output_path)
print(f"Tamanho: {size / 1024:.1f} KB")
print(f"Total de paginas: {pdf.page_no()}")
