#!/usr/bin/env python3
"""TumTum Pricing Strategy PDF Generator - Part 3: Chapter 3"""

import pickle
import sys
sys.path.insert(0, "/home/user/tumtum/docs")
from generate_pdf_part1 import TumTumPDF, RED, CYAN

with open("/tmp/tumtum_pdf_state.pkl", "rb") as f:
    pdf = pickle.load(f)


def build_chapter3(pdf):
    """Chapter 3: Estrutura de Planos, Nomes e Precos"""
    pdf.chapter_title("3", "Estrutura de Planos, Nomes e Precos")

    pdf.section_title("3.1 Numero de Planos: Dois (Free + Pro)")

    pdf.subsection_title("Racional para Dois Planos (Nao Tres ou Quatro)")
    pdf.table(
        ["Fator", "Decisao", "Motivo"],
        [
            ["Fase 0 MVP", "2 planos max", "Nao ha dados suficientes para justificar segmentacao em 3+ planos."],
            ["Comportamento do consumidor BR", "Menos escolhas = decisoes mais rapidas", "Consumidores brasileiros convertem mais com escolha binaria ('Gratis ou Pro?')."],
            ["Padrao de uso TumTum", "Nao complexo o suficiente para 3 planos", "O valor e consistente: ver FC, fazer cards, compartilhar. Mais/melhor da mesma coisa."],
            ["Simplicidade operacional", "Menos suporte, menos confusao", "Dois planos = um caminho de upgrade. Sem friccao de 'qual plano e pra mim?'."],
            ["Flexibilidade futura", "Pode dividir depois com dados", "Adicionar um plano e facil; remover um e doloroso."],
        ],
        [42, 48, 100],
    )

    pdf.subsection_title("Quando Adicionar um Terceiro Plano")
    pdf.body_text("Adicionar um terceiro plano SOMENTE quando:")
    pdf.bullet("Recurso de comparacao com artista/atleta lancar (Fase 1) E dados de demanda confirmarem preco separado")
    pdf.bullet("Caso de uso B2B/corporativo surgir (ex: promotores de eventos querendo dashboards de analytics)")
    pdf.bullet("Pesquisa mostrar segmento claro disposto a pagar 2x o preco do Pro por funcionalidades especificas")
    pdf.ln(2)

    pdf.section_title("3.2 Posicionamento dos Planos e Clientes-Alvo")

    pdf.subsection_title("Plano 1: TumTum (Gratuito)")
    pdf.bold_text("Cliente-alvo: Todos. Cada pessoa em cada evento. Topo do funil.")
    pdf.bold_text("Posicionamento: 'Seu coracao estava la. Veja. Compartilhe.'")
    pdf.body_text(
        "O tier gratuito NAO e um trial. E um produto completo que entrega o momento 'aha'. "
        "Usuarios devem se sentir gratos, nao frustrados. Frustacao = desinstalacao. Gratidao = eventual upgrade."
    )
    pdf.body_text("Personas-alvo:")
    pdf.bullet("Usuario de primeira vez explorando o app")
    pdf.bullet("Fa casual que vai a 2-3 eventos por ano")
    pdf.bullet("Usuarios das classes C/D que nao podem justificar nenhuma assinatura")
    pdf.bullet("Qualquer pessoa na fase de 'descoberta' do funil")
    pdf.ln(2)

    pdf.subsection_title("Plano 2: TumTum Pro")
    pdf.bold_text("Cliente-alvo: Frequentadores regulares de eventos que querem conteudo social premium.")
    pdf.bold_text("Posicionamento: 'Cada batida. Cada momento. Lindamente seu.'")
    pdf.body_text(
        "Usuarios Pro pagam por duas coisas: (1) conteudo de melhor aparencia para redes sociais, "
        "e (2) recursos competitivos/sociais (comparacao com amigos, historico completo). "
        "E sobre status e completude."
    )
    pdf.body_text("Personas-alvo:")
    pdf.bullet("Socios de clubes de futebol (2-4 jogos/mes)")
    pdf.bullet("Frequentadores ativos de shows (1-2+ eventos/mes)")
    pdf.bullet("Usuarios ativos em redes sociais (Instagram/TikTok)")
    pdf.bullet("Grupos de amigos que vao a eventos juntos (recurso de comparacao)")
    pdf.bullet("Fas dedicados que querem seu historico emocional completo")
    pdf.ln(2)

    pdf.section_title("3.3 Estrategia de Nomes")

    pdf.table(
        ["Nome", "Racional"],
        [
            ["TumTum (gratis)", "O produto E o TumTum. Sem qualificador necessario. Usar 'Free' ou 'Basico' faz o produto parecer menor."],
            ["TumTum Pro", "'Pro' e universalmente entendido no Brasil (Spotify, Canva, LinkedIn usam 'Pro'). Sinaliza 'melhor' sem ser pretensioso. Curto, limpo, memoravel."],
        ],
        [40, 150],
    )

    pdf.subsection_title("Nomes Rejeitados")
    pdf.table(
        ["Nome", "Por Que Rejeitado"],
        [
            ["TumTum Premium", "Soa como seguro ou banco. Corporativo demais para produto emocional."],
            ["TumTum Plus", "Super utilizado (Disney+, Apple TV+). Nao transmite valor suficiente."],
            ["TumTum VIP", "Muito 'balada'. Pode parecer cafona."],
            ["TumTum Gold/Black", "Nao conecta com o core emocional do produto. Arbitrario."],
            ["TumTum Pulse", "Legal mas confuso - e um produto diferente? Uma feature?"],
        ],
        [40, 150],
    )

    pdf.section_title("3.4 Pontos de Preco e Racional")

    pdf.subsection_title("Precificacao do TumTum Pro")
    pdf.table(
        ["Ciclo de Cobranca", "Preco", "Mensal Efetivo", "Desconto", "Racional"],
        [
            ["Mensal", "R$ 14,90/mes", "R$ 14,90", "-", "Preco ancora. Abaixo do Spotify (R$ 21,90) e YouTube Premium (R$ 24,90)."],
            ["Anual", "R$ 119,90/ano", "R$ 9,99/mes", "33%", "Cruza barreira psicologica dos R$ 10. Cria sensacao de 'nao pensar duas vezes'."],
            ["Sazonal (futebol)", "R$ 79,90/temporada", "R$ 9,99/mes", "33%", "Acompanha calendario do Brasileirao (abril-dezembro). Perfeito para fa so de futebol."],
        ],
        [32, 32, 25, 16, 85],
    )

    pdf.subsection_title("Precificacao de Microtransacoes (Para Nao-Assinantes)")
    pdf.table(
        ["Item", "Preco", "Racional"],
        [
            ["Card premium (unitario, sem marca d'agua)", "R$ 4,90", "Zona de compra por impulso. Mesma faixa que FutebolCard."],
            ["Card animado/video (unitario)", "R$ 6,90", "Maior valor de producao = maior preco. Ainda abaixo de R$ 10."],
            ["Pacote de cards (3 cards premium)", "R$ 9,90", "Desconto para compra antecipada. Bom para frequentadores de festivais."],
            ["Desbloqueio de comparacao artista/atleta", "R$ 7,90", "Microtransacao premium para o recurso matador. Alta DaP por ser exclusivo."],
        ],
        [60, 20, 110],
    )

    pdf.section_title("3.5 Comparacao com o Mercado")

    pdf.body_text("Posicionamento do TumTum Pro vs. assinaturas concorrentes:")
    pdf.table(
        ["App", "Preco Mensal", "Uso", "Diferenca vs TumTum"],
        [
            ["Spotify", "R$ 21,90", "Diario", "TumTum e 32% mais barato"],
            ["YouTube Premium", "R$ 24,90", "Diario", "TumTum e 40% mais barato"],
            ["Strava", "R$ 31,99", "Semanal", "TumTum e 53% mais barato"],
            ["Amazon Prime", "R$ 19,90", "Diario", "TumTum e 25% mais barato"],
            ["Globoplay", "R$ 24,90", "Semanal", "TumTum e 40% mais barato"],
            ["Apple Fitness+", "R$ 21,90", "Semanal", "TumTum e 32% mais barato"],
        ],
        [42, 35, 40, 73],
    )

    pdf.highlight_box(
        "R$ 14,90 posiciona o TumTum como a assinatura premium mais barata na stack do usuario. Facil de justificar manter."
    )

    pdf.section_title("3.6 Passe Sazonal de Futebol - Detalhes")

    pdf.bold_text("Passe do Brasileirao: R$ 79,90 (~abril a dezembro)")
    pdf.table(
        ["Aspecto", "Detalhe"],
        [
            ["Duracao", "~8 meses (acompanha calendario do Brasileirao)"],
            ["Taxa mensal efetiva", "R$ 9,99/mes"],
            ["Usuario-alvo", "Torcedor que vai a jogos regularmente mas nao frequenta shows"],
            ["Vantagem vs. mensal", "33% de economia + sem fadiga de decisao todo mes"],
            ["Vantagem vs. anual", "Nao paga por jan-marco quando nao ha jogos"],
            ["Estrategia de renovacao", "Auto-renovacao por temporada com aviso de 15 dias"],
        ],
        [45, 145],
    )

    pdf.body_text(
        "O futebol brasileiro e intensamente sazonal. Torcedores sao altamente engajados de abril a dezembro "
        "e praticamente dormentes de janeiro a marco (exceto pre-temporada). Um passe sazonal respeita essa "
        "realidade e evita o churn de 'por que estou pagando se nao tem jogos?'."
    )

    pdf.section_title("3.7 Principios de UX da Pagina de Precos")

    pdf.bullet("Mostrar mensal e anual lado a lado com a economia anual destacada ('Economize 33%' em verde)")
    pdf.bullet("Selecionar o plano anual por padrao. Assinantes anuais tem 3x menos churn.")
    pdf.bullet("Mostrar o preco mensal efetivo para anual (R$ 9,99/mes) - nao so o total de R$ 119,90")
    pdf.bullet("Aceitar Pix e boleto. ~30% dos pagamentos digitais brasileiros sao via Pix")
    pdf.bullet("Oferecer parcelamento no cartao. R$ 119,90 em 12x R$ 9,99 e psicologicamente mais facil")
    pdf.bullet("Sem tabela de comparacao com 10+ funcionalidades. Mostrar 3-4 diferencas-chave. Decisao em <10 segundos")
    pdf.bullet("Prova social na pagina de precos. '12.000 fas ja usam TumTum Pro' com numeros reais")
    pdf.ln(2)

    return pdf


pdf = build_chapter3(pdf)

with open("/tmp/tumtum_pdf_state.pkl", "wb") as f:
    pickle.dump(pdf, f)

print("Chapter 3 done. PDF state saved.")
