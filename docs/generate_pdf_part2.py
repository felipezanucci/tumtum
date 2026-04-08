#!/usr/bin/env python3
"""TumTum Pricing Strategy PDF Generator - Part 2: Chapter 2"""

import pickle
import sys
sys.path.insert(0, "/home/user/tumtum/docs")
from generate_pdf_part1 import TumTumPDF, RED

with open("/tmp/tumtum_pdf_state.pkl", "rb") as f:
    pdf = pickle.load(f)


def build_chapter2(pdf):
    """Chapter 2: Analise de Valor e Disposicao a Pagar"""
    pdf.chapter_title("2", "Analise de Valor para o Cliente e Disposicao a Pagar")

    pdf.section_title("2.1 Principais Resultados Pelos Quais os Clientes Pagam")
    pdf.body_text(
        "Entender o que os clientes realmente valorizam - nao o que achamos que valorizam - "
        "e a base da precificacao. No TumTum, clientes pagam por prova emocional e moeda social, "
        "nao por dados ou tecnologia."
    )

    pdf.subsection_title("Resultado #1: Validacao Emocional")
    pdf.bold_text("'Eu estava LA e senti ISSO'")
    pdf.body_text(
        "Ver sua frequencia cardiaca disparar durante o gol da vitoria ou o refrao da sua musica favorita. "
        "A prova de que o momento foi real e seu corpo reagiu. Na cultura brasileira, onde a paixao por futebol "
        "e musica esta profundamente ligada a identidade pessoal, essa prova tem imenso valor emocional."
    )

    pdf.table(
        ["Segmento", "Gatilho Emocional", "Exemplo"],
        [
            ["Futebol", "Gol do seu time, defesa de penalti, vitoria nos acrescimos",
             "Meu coracao bateu 165 BPM quando o Palmeiras fez gol aos 92'"],
            ["Shows", "Musica favorita ao vivo, encore, interacao do artista",
             "Meu coracao chegou a 172 BPM durante 'Evidencias' ao vivo"],
        ],
        [30, 65, 95],
    )

    pdf.subsection_title("Resultado #2: Moeda Social")
    pdf.bold_text("'Olha o que eu vivi'")
    pdf.body_text(
        "Um card visualmente incrivel que comunica 'eu estava nesse evento e foi assim que me senti' - "
        "otimizado para Instagram Stories, TikTok, X e WhatsApp. "
        "O Brasil tem 150M+ de usuarios de redes sociais, entre os mais engajados do mundo. "
        "Um card do TumTum e um novo formato para mostrar 'eu estava la' - melhor que uma foto borrada de show."
    )

    pdf.body_text("Fatores que impulsionam a disposicao a pagar:")
    pdf.bullet("Qualidade do card (templates premium vs. basico)")
    pdf.bullet("Remocao da marca d'agua (status social)")
    pdf.bullet("Cards em video/animados (maior engajamento nas redes)")
    pdf.bullet("Customizacao (escolher quais picos destacar)")
    pdf.ln(2)

    pdf.subsection_title("Resultado #3: Comparacao Competitiva")
    pdf.bold_text("'Como eu me comparo?'")
    pdf.body_text(
        "Comparar sua frequencia cardiaca com amigos no mesmo evento, ou (na Fase 1) com o proprio artista/atleta. "
        "Brasileiros sao intensamente competitivos e sociais. O Cartola FC tem 10M+ de usuarios impulsionados por competicao. "
        "O recurso de 'porcentagem de sincronia com seu idolo' explora a mesma psicologia mas com dados biometricos reais."
    )
    pdf.highlight_box("Este e o diferencial premium. Nenhum outro app oferece isso. Justifica a assinatura sozinho.")

    # 2.2 Table Stakes vs Differentiators
    pdf.section_title("2.2 Funcionalidades: Table Stakes vs. Diferenciais")

    pdf.subsection_title("Table Stakes (Devem Ser Gratuitas)")
    pdf.body_text(
        "Estas funcionalidades DEVEM ser gratuitas. Restringi-las mataria o crescimento "
        "e pareceria explorar usuarios que compartilham seus dados de saude."
    )
    pdf.table(
        ["Funcionalidade", "Por Que Deve Ser Gratuita"],
        [
            ["Conectar wearable", "Friccao de setup deve ser zero. Cobrar pela conexao = DOA."],
            ["Gravar FC durante eventos", "Coleta de dados central. Cobrar para gravar mata o funil."],
            ["Ver curva de FC", "Usuarios contribuiram seus dados. Mostrar de volta e o minimo."],
            ["Ver top 3 picos por evento", "O momento 'aha'. E o que faz usuarios dizerem 'uau' e quererem mais."],
            ["Gerar 1 card basico por evento", "O MECANISMO viral. Cada card gratuito = marketing gratuito."],
            ["Ver timeline do evento", "Contexto para a curva de FC. Sem ela, os dados nao fazem sentido."],
        ],
        [55, 135],
    )

    pdf.subsection_title("Diferenciais Verdadeiros (Funcionalidades Premium)")
    pdf.table(
        ["Funcionalidade", "Poder de Diferenciacao", "Por Que Pagam"],
        [
            ["Templates premium ilimitados", "ALTO", "Card basico e bom. Cards premium sao deslumbrantes."],
            ["Sem marca d'agua", "ALTO", "Marca d'agua = 'uso a versao gratis'. Remove-la e sinal de status."],
            ["Cards animados/video", "ALTO", "Cards animados tem 3-5x mais engajamento no Instagram/TikTok."],
            ["Todos os picos (ilimitados)", "MEDIO", "Gratis mostra top 3. Pro mostra cada pico."],
            ["Comparacao com amigos", "MEDIO-ALTO", "Comparar FC com amigos no mesmo evento. Competicao social."],
            ["Comparacao com artista/atleta", "MUITO ALTO", "O recurso matador. 'Eu estava 78% sincronizado com Neymar.'"],
            ["Historico de eventos", "MEDIO", "Gratis mantem 5 ultimos. Pro mantem historico ilimitado."],
            ["Reel de destaques", "MEDIO", "Video auto-gerado dos seus melhores momentos."],
        ],
        [55, 40, 95],
    )

    # 2.3 Willingness to Pay
    pdf.section_title("2.3 Disposicao a Pagar - Mercado Brasileiro")

    pdf.subsection_title("Ancoras de Preco de Referencia")
    pdf.body_text(
        "Consumidores brasileiros avaliam o preco do TumTum contra estas assinaturas existentes:"
    )
    pdf.table(
        ["App", "Preco Mensal (BRL)", "Uso", "Valor Percebido"],
        [
            ["Amazon Prime", "R$ 19,90", "Diario (video + frete)", "Muito alto (bundle)"],
            ["Spotify", "R$ 21,90", "Diario (musica)", "Alto (essencial)"],
            ["YouTube Premium", "R$ 24,90", "Diario (video)", "Medio-alto"],
            ["Strava", "R$ 31,99", "Semanal (fitness)", "Medio (nicho)"],
            ["Globoplay", "R$ 24,90", "Semanal (streaming)", "Medio"],
            ["TumTum Pro", "R$ 14,90", "Periodico (eventos)", "Deve parecer uma pechincha"],
        ],
        [38, 42, 48, 62],
    )

    pdf.subsection_title("Por Que R$ 14,90 e o Preco Ideal")
    pdf.bullet(
        "Abaixo do limiar de 'uso diario': Apps diarios (Spotify, Netflix) custam R$ 20-30. "
        "TumTum e usado em eventos (2-8x/mes). R$ 14,90 reconhece isso honestamente."
    )
    pdf.bullet(
        "Acima da zona 'descartavel': Abaixo de R$ 9,90, o produto parece barato. "
        "R$ 14,90 sinaliza qualidade mantendo acessibilidade."
    )
    pdf.bullet(
        "Opcao anual cria urgencia: R$ 119,90/ano = R$ 9,99/mes efetivo. "
        "Cruza a barreira psicologica dos R$ 10."
    )
    pdf.bullet(
        "Matematica do salario minimo: R$ 14,90 = ~1% do salario minimo (R$ 1.518). "
        "Pesquisas mostram que Classes B/C gastam ate 1,5% da renda mensal em uma assinatura de entretenimento."
    )
    pdf.ln(2)

    # WTP by segment
    pdf.subsection_title("Disposicao a Pagar por Segmento - Futebol")
    pdf.table(
        ["Perfil", "Eventos/Mes", "DaP (Mensal)", "Comportamento Provavel"],
        [
            ["Fa casual (assiste na TV, 1-2 jogos/ano)", "0-1", "R$ 0 (gratis)", "Usa tier gratis. Pode comprar 1 card premium no classico."],
            ["Frequentador regular (socio, 2-4 jogos/mes)", "2-4", "R$ 9,90-14,90", "Forte candidato a assinatura. Multiplos eventos justificam."],
            ["Ultra/Fanatico (todo jogo, viaja para fora)", "4-8", "R$ 14,90-19,90", "Assinante garantido. Quer tudo: cards, comparacoes, historico."],
        ],
        [50, 25, 30, 85],
    )

    pdf.subsection_title("Disposicao a Pagar por Segmento - Shows")
    pdf.table(
        ["Perfil", "Eventos/Mes", "DaP (Mensal)", "Comportamento Provavel"],
        [
            ["Frequentador ocasional (2-3 shows/ano)", "0-1", "R$ 0 ou microtx", "Usa gratis ou compra 1 card premium por evento."],
            ["Fa de musica ativo (1-2 shows/mes)", "1-2", "R$ 9,90-14,90", "Bom candidato a assinatura, especialmente em temporada de festivais."],
            ["Entusiasta de festivais (Lolla, Rock in Rio)", "2-5", "R$ 14,90", "Assinante na temporada. Multiplos eventos por festival."],
        ],
        [50, 25, 30, 85],
    )

    # Conversion triggers
    pdf.section_title("2.4 Gatilhos de Conversao - O Que Faz Usuarios Gratuitos Pagarem")

    pdf.subsection_title("Gatilho 1: O Momento da 'Marca D'agua Feia'")
    pdf.body_text(
        "Usuario cria um card incrivel mostrando 168 BPM durante o gol do classico. "
        "Vai compartilhar no Instagram Stories... e ve a marca d'agua. "
        "'R$ 4,90 para remover? Feito.'"
    )

    pdf.subsection_title("Gatilho 2: O Momento 'Quero o Animado'")
    pdf.body_text(
        "Usuario ve o card animado de um amigo no TikTok com a curva de FC pulsando. "
        "O card dele e um PNG estatico. 'Como consigo AQUELA versao?'"
    )

    pdf.subsection_title("Gatilho 3: O Momento 'Comparar com Meu Amigo'")
    pdf.body_text(
        "Dois amigos no mesmo show do Lollapalooza. Ambos usam TumTum. "
        "Tier gratis mostra curvas individuais. Pro mostra comparacao lado a lado com porcentagem de sincronia."
    )

    pdf.subsection_title("Gatilho 4: O Momento 'A Temporada Comecou'")
    pdf.body_text(
        "Brasileirao comeca em abril. O torcedor sabe que vai a 15+ jogos. "
        "R$ 14,90/mes para cards premium ilimitados vs. R$ 4,90 x 15 = R$ 73,50. "
        "Assinatura e a escolha obvia."
    )

    pdf.subsection_title("Gatilho 5: O Momento 'Comparacao com Artista' (Fase 1)")
    pdf.body_text(
        "Anitta compartilha seus batimentos de um show. "
        "Fas no mesmo show podem ver 'Voce estava 82% sincronizado com Anitta.' "
        "Este e o gatilho nuclear de conversao - exclusivo, emocional, compartilhavel."
    )

    return pdf


pdf = build_chapter2(pdf)

with open("/tmp/tumtum_pdf_state.pkl", "wb") as f:
    pickle.dump(pdf, f)

print("Chapter 2 done. PDF state saved.")
