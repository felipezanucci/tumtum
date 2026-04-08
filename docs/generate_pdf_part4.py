#!/usr/bin/env python3
"""TumTum Pricing Strategy PDF Generator - Part 4: Chapter 4"""

import pickle
import sys
sys.path.insert(0, "/home/user/tumtum/docs")
from generate_pdf_part1 import TumTumPDF, RED

with open("/tmp/tumtum_pdf_state.pkl", "rb") as f:
    pdf = pickle.load(f)


def build_chapter4(pdf):
    """Chapter 4: Matriz de Alocacao de Funcionalidades"""
    pdf.chapter_title("4", "Matriz de Alocacao de Funcionalidades")

    pdf.section_title("4.1 Tabela Completa de Alocacao")

    pdf.subsection_title("Dados e Visualizacao (Core)")
    pdf.table(
        ["Funcionalidade", "Free", "Pro", "Justificativa"],
        [
            ["Conectar wearable", "Ilimitado", "Ilimitado", "Table stakes. Cobrar pela conexao = zero adocao."],
            ["Gravar FC em eventos", "Ilimitado", "Ilimitado", "Coleta de dados deve ser gratis. Usuarios contribuem dados."],
            ["Ver curva de FC com timeline", "Completo", "Completo", "Momento 'aha'. Restringir = usuarios nunca entendem o valor."],
            ["Picos detectados", "Top 3/evento", "Ilimitados", "Gratis mostra o suficiente. Pro revela o quadro completo."],
            ["Timeline do evento", "Completo", "Completo", "Contexto essencial. Curva sem timeline nao faz sentido."],
            ["Estatisticas da sessao", "Completo", "Completo", "Stats basicas reforcam engajamento. Sem razao para restringir."],
        ],
        [45, 25, 25, 95],
    )

    pdf.subsection_title("Cards de Compartilhamento")
    pdf.table(
        ["Funcionalidade", "Free", "Pro", "Microtx"],
        [
            ["Card basico", "1/evento, com marca d'agua", "Ilimitado, sem marca", "R$ 4,90/card"],
            ["Templates premium", "Nao disponivel", "Todos incluidos", "Incluso na compra"],
            ["Cards animados/video", "Nao disponivel", "Incluido", "R$ 6,90/card"],
            ["Customizacao do card", "Nao disponivel", "Customizacao total", "Nao disponivel"],
            ["Exportacao HD (1080p+)", "Qualidade padrao 720p", "Full HD / 4K", "Incluso na compra"],
            ["Remocao da marca d'agua", "Marca d'agua presente", "Sem marca d'agua", "Incluso em qualquer microtx"],
        ],
        [45, 40, 40, 65],
    )

    pdf.subsection_title("Social e Comparacao")
    pdf.table(
        ["Funcionalidade", "Free", "Pro", "Microtx"],
        [
            ["Compartilhar nas redes", "Disponivel (cards gratis)", "Disponivel (todos)", "Disponivel"],
            ["Comparacao com amigos", "Nao disponivel", "Incluido", "Nao disponivel"],
            ["Ranking do evento", "Apenas sua posicao", "Ranking completo", "-"],
            ["Comparacao artista/atleta", "Nao disponivel", "Incluido", "R$ 7,90/evento"],
            ["% sincronia com artista", "Nao disponivel", "Incluido", "Incluso na comparacao"],
        ],
        [48, 42, 42, 58],
    )

    pdf.subsection_title("Historico e Colecoes")
    pdf.table(
        ["Funcionalidade", "Free", "Pro"],
        [
            ["Historico de eventos", "Ultimos 5 eventos", "Ilimitado"],
            ["Galeria de cards", "Ultimos 10 cards", "Ilimitada"],
            ["Reel de destaques (video auto-gerado)", "Nao disponivel", "Incluido (mensal + anual)"],
            ["Exportar dados (CSV/JSON)", "Nao disponivel", "Incluido"],
            ["Geracao prioritaria de cards", "Fila padrao", "Processamento prioritario"],
            ["Notificacoes push", "Basicas", "Avancadas (alertas sociais)"],
        ],
        [55, 65, 70],
    )

    # By segment
    pdf.section_title("4.2 Alocacao por Segmento")

    pdf.subsection_title("Para Torcedores de Futebol")
    pdf.bold_text("Valor do tier GRATIS:")
    pdf.bullet("Gravar FC durante a partida (90 min + acrescimos)")
    pdf.bullet("Ver curva de FC com gols, cartoes e intervalo marcados")
    pdf.bullet("Top 3 picos (geralmente = 3 momentos mais emocionantes do jogo)")
    pdf.bullet("1 card basico por partida")
    pdf.bullet("Compartilhar no WhatsApp/Instagram")
    pdf.ln(2)

    pdf.bold_text("Gatilhos de upgrade para PRO:")
    pdf.bullet("Comparacao com amigos ('Quem sentiu mais o gol?')")
    pdf.bullet("Historico da temporada (todos os jogos do Brasileirao)")
    pdf.bullet("Todos os picos (cada momento emocional, nao apenas top 3)")
    pdf.bullet("Cards premium (templates nas cores do clube, momentos de gol animados)")
    pdf.bullet("Ranking do evento ('Fui o #3 mais emocionado na Neo Quimica Arena')")
    pdf.bullet("Comparacao com jogador - Fase 1 ('85% sincronizado com o goleiro nos penaltis')")
    pdf.ln(2)

    pdf.subsection_title("Para Fas de Shows/Festivais")
    pdf.bold_text("Valor do tier GRATIS:")
    pdf.bullet("Gravar FC durante o show (2-3 horas)")
    pdf.bullet("Ver curva de FC com musicas do setlist marcadas")
    pdf.bullet("Top 3 picos (suas musicas mais emocionantes)")
    pdf.bullet("1 card basico por show")
    pdf.bullet("Compartilhar no Instagram Stories/TikTok")
    pdf.ln(2)

    pdf.bold_text("Gatilhos de upgrade para PRO:")
    pdf.bullet("Cards animados (curva de FC pulsando no ritmo - ouro no TikTok)")
    pdf.bullet("Templates premium (tematicos do artista, de festivais)")
    pdf.bullet("Sem marca d'agua (estetica limpa para o feed do Instagram)")
    pdf.bullet("Multiplos cards por show (um por musica favorita)")
    pdf.bullet("Comparacao com artista - Fase 1 ('78% sincronizado com Anitta')")
    pdf.bullet("Reel de destaques (video dos melhores momentos)")
    pdf.ln(2)

    # Anti-patterns
    pdf.section_title("4.3 Anti-Padroes a Evitar")
    pdf.table(
        ["Anti-Padrao", "Por Que E Ruim", "Abordagem do TumTum"],
        [
            ["Restringir a curva de FC", "Usuarios contribuiram seus dados biometricos. Esconder atras de paywall parece exploracao.", "Curva de FC sempre gratis. Visibilidade total dos dados."],
            ["Limitar eventos/mes no gratis", "Cria escassez artificial na coleta. Usuarios param de usar o device.", "Gravacao ilimitada em todos os planos."],
            ["Exigir Pro para compartilhar", "Compartilhar E o motor de crescimento. Bloquear = matar viralidade.", "Usuarios gratis sempre podem compartilhar (com marca d'agua)."],
            ["Paywall total na criacao de cards", "Se usuarios gratis nao podem criar NENHUM card, nunca experimentam o valor.", "1 card gratis basico por evento. Sempre."],
            ["Muitas opcoes de microtransacao", "Fadiga de decisao. Confusao entre opcoes.", "Max 4 opcoes de microtx. Diferenciacao clara."],
        ],
        [40, 65, 85],
    )

    # Upsell touchpoints
    pdf.section_title("4.4 Pontos de Upsell na Jornada do Usuario")
    pdf.table(
        ["Momento", "O Que o Usuario Ve", "CTA de Upsell"],
        [
            ["Apos deteccao de picos", "'Voce teve 8 picos. Veja todos com Pro.'", "Banner suave abaixo da lista"],
            ["Geracao de card (preview)", "Card premium borrado ao lado do basico", "Comparacao lado a lado"],
            ["Tentando remover marca d'agua", "'Cards sem marca com Pro ou R$ 4,90 avulso'", "Modal com duas opcoes"],
            ["Amigo no mesmo evento", "'Voce e @amigo estiveram no mesmo show!'", "Push notification + card in-app"],
            ["Historico (6o evento)", "'Seu historico completo esta no Pro'", "Banner na pagina de historico"],
            ["Artista posta FC (Fase 1)", "'Anitta compartilhou seus batimentos!'", "Push + card de comparacao bloqueado"],
        ],
        [40, 65, 85],
    )

    return pdf


pdf = build_chapter4(pdf)

with open("/tmp/tumtum_pdf_state.pkl", "wb") as f:
    pickle.dump(pdf, f)

print("Chapter 4 done. PDF state saved.")
