#!/usr/bin/env python3
"""Generate Tumtum UX Structure PDF with dark theme using reportlab."""

from reportlab.lib.pagesizes import A4
from reportlab.lib.colors import HexColor
from reportlab.lib.units import mm, cm
from reportlab.pdfgen import canvas
from reportlab.lib.styles import getSampleStyleSheet
import os
import re

OUTPUT = os.path.join(os.path.dirname(__file__), "tumtum-ux-structure.pdf")
INPUT = os.path.join(os.path.dirname(__file__), "app-structure.md")

# Colors
BG = HexColor("#08080C")
SURFACE = HexColor("#111118")
RED = HexColor("#C0392B")
RED2 = HexColor("#E74C3C")
CYAN = HexColor("#00D2FF")
TEXT = HexColor("#F0F0F5")
MUTED = HexColor("#6B6B80")
WHITE = HexColor("#FFFFFF")

W, H = A4

def draw_bg(c):
    c.setFillColor(BG)
    c.rect(0, 0, W, H, fill=1, stroke=0)

def draw_page_number(c, num):
    c.setFont("Courier", 8)
    c.setFillColor(MUTED)
    c.drawCentredString(W/2, 15*mm, f"— {num} —")

def title_page(c):
    draw_bg(c)
    # Logo area
    c.setFillColor(RED)
    c.rect(W/2 - 40*mm, H/2 + 30*mm, 80*mm, 2*mm, fill=1, stroke=0)

    c.setFont("Courier-Bold", 36)
    c.setFillColor(WHITE)
    c.drawCentredString(W/2, H/2 + 15*mm, "TUMTUM")

    c.setFont("Courier", 14)
    c.setFillColor(MUTED)
    c.drawCentredString(W/2, H/2 - 5*mm, "UX App Structure")
    c.drawCentredString(W/2, H/2 - 20*mm, "Wireframes & User Flows")

    c.setFillColor(RED)
    c.rect(W/2 - 40*mm, H/2 - 35*mm, 80*mm, 2*mm, fill=1, stroke=0)

    c.setFont("Courier", 11)
    c.setFillColor(MUTED)
    c.drawCentredString(W/2, H/2 - 55*mm, "Abril 2026 • Versao 1.0")
    c.drawCentredString(W/2, H/2 - 70*mm, "22 telas • 10 fluxos • Zero friccao")

    c.showPage()

def toc_page(c):
    draw_bg(c)
    y = H - 40*mm

    c.setFont("Courier-Bold", 20)
    c.setFillColor(RED)
    c.drawString(30*mm, y, "INDICE")
    y -= 15*mm

    c.setFillColor(RED)
    c.rect(30*mm, y + 2*mm, 150*mm, 0.5*mm, fill=1, stroke=0)
    y -= 12*mm

    items = [
        ("01", "Mapa Geral de Telas"),
        ("02", "Primeiro Acesso e Onboarding"),
        ("03", "Autenticacao (Login / Cadastro)"),
        ("04", "Conexao do Wearable"),
        ("05", "Home e Descoberta de Eventos"),
        ("06", "Busca e Selecao de Evento"),
        ("07", "Pre-Evento"),
        ("08", "Durante o Evento (Modo Live)"),
        ("09", "Pos-Evento e Experiencia"),
        ("10", "Geracao e Compartilhamento do Card"),
        ("11", "Perfil e Colecao"),
        ("12", "Navegacao Global"),
        ("13", "Principios de UX Aplicados"),
        ("14", "Mapa de Estados e Transicoes"),
        ("15", "Inventario Completo de Telas"),
    ]

    for num, title in items:
        c.setFont("Courier-Bold", 11)
        c.setFillColor(RED)
        c.drawString(30*mm, y, num)
        c.setFont("Courier", 11)
        c.setFillColor(TEXT)
        c.drawString(45*mm, y, title)
        y -= 8*mm

    draw_page_number(c, 1)
    c.showPage()

def render_section(c, title, lines, page_num):
    """Render a section with its ASCII content."""
    draw_bg(c)
    y = H - 30*mm
    margin = 20*mm
    max_width = W - 2 * margin

    # Section title
    c.setFont("Courier-Bold", 18)
    c.setFillColor(RED)
    c.drawString(margin, y, title)
    y -= 5*mm

    c.setFillColor(RED)
    c.rect(margin, y, 170*mm, 0.5*mm, fill=1, stroke=0)
    y -= 10*mm

    in_code = False
    for line in lines:
        stripped = line.rstrip()

        # Check if we need a new page
        if y < 25*mm:
            draw_page_number(c, page_num[0])
            c.showPage()
            page_num[0] += 1
            draw_bg(c)
            y = H - 25*mm

        # Handle code blocks
        if stripped.startswith("```"):
            in_code = not in_code
            continue

        if in_code:
            # Monospace ASCII art - smaller font
            c.setFont("Courier", 6.5)
            c.setFillColor(MUTED)
            # Draw subtle background for code
            c.setFillColor(SURFACE)
            c.rect(margin - 2*mm, y - 1*mm, 175*mm, 4*mm, fill=1, stroke=0)
            c.setFillColor(HexColor("#A0A0B0"))
            c.drawString(margin, y, stripped[:120])
            y -= 4*mm
        elif stripped.startswith("### "):
            # Sub-header
            y -= 3*mm
            c.setFont("Courier-Bold", 12)
            c.setFillColor(RED2)
            c.drawString(margin, y, stripped.replace("### ", ""))
            y -= 8*mm
        elif stripped.startswith("## "):
            continue  # Already handled as section title
        elif stripped.startswith("| "):
            # Table row
            c.setFont("Courier", 7)
            c.setFillColor(TEXT)
            c.drawString(margin, y, stripped[:110])
            y -= 4.5*mm
        elif stripped.startswith("- ") or stripped.startswith("* "):
            # Bullet point
            c.setFont("Courier", 9)
            c.setFillColor(TEXT)
            c.drawString(margin + 3*mm, y, "•  " + stripped[2:])
            y -= 6*mm
        elif stripped.startswith("> "):
            # Blockquote
            c.setFillColor(RED)
            c.rect(margin, y - 1*mm, 1*mm, 5*mm, fill=1, stroke=0)
            c.setFont("Courier-Oblique", 9)
            c.setFillColor(MUTED)
            c.drawString(margin + 5*mm, y, stripped[2:])
            y -= 7*mm
        elif stripped == "---":
            y -= 3*mm
            c.setFillColor(HexColor("#1A1A24"))
            c.rect(margin, y, 170*mm, 0.3*mm, fill=1, stroke=0)
            y -= 5*mm
        elif stripped == "":
            y -= 3*mm
        else:
            # Regular text
            c.setFont("Courier", 9)
            c.setFillColor(TEXT)
            # Word wrap
            words = stripped.split()
            current_line = ""
            for word in words:
                test = current_line + " " + word if current_line else word
                if c.stringWidth(test, "Courier", 9) < max_width:
                    current_line = test
                else:
                    c.drawString(margin, y, current_line)
                    y -= 5.5*mm
                    current_line = word
                    if y < 25*mm:
                        draw_page_number(c, page_num[0])
                        c.showPage()
                        page_num[0] += 1
                        draw_bg(c)
                        y = H - 25*mm
            if current_line:
                c.drawString(margin, y, current_line)
                y -= 5.5*mm

    draw_page_number(c, page_num[0])
    c.showPage()
    page_num[0] += 1

def parse_and_render(c):
    """Parse the markdown file and render each section."""
    with open(INPUT, "r") as f:
        content = f.read()

    # Split by ## headers (sections)
    sections = re.split(r'\n(?=## \d)', content)

    page_num = [2]  # Start after TOC

    for section in sections:
        lines = section.split("\n")
        # Find the section title
        title = ""
        start = 0
        for i, line in enumerate(lines):
            if line.startswith("## "):
                title = line.replace("## ", "").strip()
                start = i + 1
                break

        if not title:
            continue

        render_section(c, title, lines[start:], page_num)

def closing_page(c, page_num):
    draw_bg(c)
    y = H/2 + 20*mm

    c.setFont("Courier-Bold", 24)
    c.setFillColor(RED)
    c.drawCentredString(W/2, y, "PROXIMOS PASSOS")
    y -= 20*mm

    steps = [
        "1. Wireframes de alta fidelidade no Figma",
        "2. Prototipo interativo clicavel",
        "3. Design system (tokens, componentes)",
        "4. Teste de usabilidade (5 usuarios)",
        "5. Especificacao de animacoes",
    ]

    for step in steps:
        c.setFont("Courier", 11)
        c.setFillColor(TEXT)
        c.drawCentredString(W/2, y, step)
        y -= 10*mm

    y -= 15*mm
    c.setFillColor(RED)
    c.rect(W/2 - 30*mm, y, 60*mm, 1*mm, fill=1, stroke=0)
    y -= 15*mm

    c.setFont("Courier-Bold", 14)
    c.setFillColor(WHITE)
    c.drawCentredString(W/2, y, "TUMTUM")
    y -= 8*mm
    c.setFont("Courier", 9)
    c.setFillColor(MUTED)
    c.drawCentredString(W/2, y, "Sinta cada momento.")

    draw_page_number(c, page_num)
    c.showPage()

def main():
    c = canvas.Canvas(OUTPUT, pagesize=A4)
    c.setTitle("Tumtum - UX App Structure")
    c.setAuthor("Tumtum Design Team")

    title_page(c)
    toc_page(c)
    parse_and_render(c)
    closing_page(c, 99)

    c.save()
    size = os.path.getsize(OUTPUT)
    print(f"PDF gerado: {OUTPUT}")
    print(f"Tamanho: {size / 1024:.0f} KB")

if __name__ == "__main__":
    main()
