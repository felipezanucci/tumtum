#!/usr/bin/env python3
"""
Generate a dark-themed PDF from the Tumtum UX app-structure.md and user-flow-diagram.md files.
Uses fpdf2 library with Courier for ASCII wireframes.
"""

import os
import re
from fpdf import FPDF

# Brand colors (RGB)
BG_COLOR = (8, 8, 12)         # #08080C
TEXT_COLOR = (240, 240, 245)   # #F0F0F5
ACCENT_COLOR = (192, 57, 43)  # #C0392B
ACCENT2_COLOR = (231, 76, 60) # #E74C3C
MUTED_COLOR = (107, 107, 128) # #6B6B80
SURFACE_COLOR = (17, 17, 24)  # #111118
CODE_TEXT = (160, 160, 176)    # lighter for code


class TumtumPDF(FPDF):
    def __init__(self):
        super().__init__()
        self.set_auto_page_break(auto=True, margin=22)

    def header(self):
        pass

    def footer(self):
        if self.page_no() > 1:
            self.set_y(-15)
            self.set_font("Helvetica", "", 7)
            self.set_text_color(*MUTED_COLOR)
            self.cell(0, 10, f"TUMTUM  |  UX App Structure  |  Pag. {self.page_no()}", align="C")

    def add_dark_page(self):
        """Add a new page with dark background."""
        self.add_page()
        self.set_fill_color(*BG_COLOR)
        self.rect(0, 0, 210, 297, "F")


def clean_text(text):
    """Remove problematic unicode characters, keep box-drawing, replace emojis."""
    replacements = {
        "\u2705": "[OK]", "\u26a0\ufe0f": "[!]", "\u26a0": "[!]",
        "\u2699\ufe0f": "[*]", "\u2699": "[*]",
        "\U0001f3b5": "[music]", "\U0001f4cd": "[pin]", "\U0001f4c5": "[cal]",
        "\U0001f514": "[bell]", "\U0001f389": "[party]", "\U0001f4a1": "[tip]",
        "\U0001f50d": "[search]", "\U0001f3e0": "[home]", "\U0001f464": "[user]",
        "\U0001f512": "[lock]", "\U0001f4f1": "[phone]", "\U0001f510": "[lock]",
        "\U0001f947": "[1st]", "\U0001f948": "[2nd]", "\U0001f949": "[3rd]",
        "\U0001f534": "[R]", "\U0001f535": "[B]", "\u26ab": "[K]",
        "\U0001f7e3": "[P]", "\U0001f7e1": "[Y]",
        "\u25cf": "*", "\u25cb": "o", "\u25c9": "*",
        "\u2665": "<3", "\u2764": "<3", "\u2764\ufe0f": "<3",
        "\u23f1": "[timer]", "\u23f1\ufe0f": "[timer]",
        "\u23f0": "[timer]", "\u23f0\ufe0f": "[timer]",
        "\u231a": "[watch]", "\u231a\ufe0f": "[watch]",
        "\u231b": "[time]", "\u231b\ufe0f": "[time]",
    }
    for emoji, replacement in replacements.items():
        text = text.replace(emoji, replacement)

    # Handle box-drawing characters -> ASCII equivalents
    box_map = {
        "\u2500": "-", "\u2502": "|", "\u250c": "+", "\u2510": "+",
        "\u2514": "+", "\u2518": "+", "\u251c": "+", "\u2524": "+",
        "\u252c": "+", "\u2534": "+", "\u253c": "+",
        "\u2550": "=", "\u2551": "|", "\u2554": "+", "\u2557": "+",
        "\u255a": "+", "\u255d": "+",
        "\u2500": "-",
    }

    result = []
    for ch in text:
        if ch in box_map:
            result.append(box_map[ch])
            continue
        try:
            ch.encode("latin-1")
            result.append(ch)
        except UnicodeEncodeError:
            if ch == "\u2026":
                result.append("...")
            elif ch in ("\u2014", "\u2015"):
                result.append("--")
            elif ch == "\u2013":
                result.append("-")
            elif ch in ("\u2018", "\u2019"):
                result.append("'")
            elif ch in ("\u201c", "\u201d"):
                result.append('"')
            elif ch == "\u2022":
                result.append("-")
            elif ch in ("\u25ba", "\u25b6"):
                result.append(">")
            elif ch == "\u2192":
                result.append("->")
            elif ch == "\u2190":
                result.append("<-")
            else:
                result.append(" ")
    return "".join(result)


def render_title_page(pdf):
    """Render the cover page."""
    pdf.add_dark_page()

    # Top red line
    pdf.set_fill_color(*ACCENT_COLOR)
    pdf.rect(20, 40, 170, 2, "F")

    # Title
    pdf.set_y(65)
    pdf.set_font("Helvetica", "B", 42)
    pdf.set_text_color(*TEXT_COLOR)
    pdf.cell(0, 18, "TUMTUM", align="C", new_x="LMARGIN", new_y="NEXT")

    # Subtitle
    pdf.set_y(95)
    pdf.set_font("Helvetica", "", 20)
    pdf.set_text_color(*ACCENT_COLOR)
    pdf.cell(0, 10, "UX App Structure", align="C", new_x="LMARGIN", new_y="NEXT")

    pdf.set_y(112)
    pdf.set_font("Helvetica", "", 14)
    pdf.set_text_color(*MUTED_COLOR)
    pdf.cell(0, 10, "Wireframes & User Flows", align="C", new_x="LMARGIN", new_y="NEXT")

    # Middle red line
    pdf.set_fill_color(*ACCENT_COLOR)
    pdf.rect(75, 132, 60, 1, "F")

    # Date
    pdf.set_y(142)
    pdf.set_font("Helvetica", "", 13)
    pdf.set_text_color(*MUTED_COLOR)
    pdf.cell(0, 10, "Abril 2026", align="C", new_x="LMARGIN", new_y="NEXT")

    # Description
    pdf.set_y(165)
    pdf.set_font("Helvetica", "I", 10)
    pdf.set_text_color(*MUTED_COLOR)
    pdf.multi_cell(0, 6,
        "Documento de arquitetura UX\n"
        "Contornos e fluxos de todas as telas\n"
        "22 telas  |  10 fluxos  |  Zero friccao",
        align="C")

    # Bottom red line
    pdf.set_fill_color(*ACCENT_COLOR)
    pdf.rect(20, 260, 170, 2, "F")

    # Tagline
    pdf.set_y(248)
    pdf.set_font("Helvetica", "I", 9)
    pdf.set_text_color(*MUTED_COLOR)
    pdf.cell(0, 8, "Sinta cada momento. Compartilhe a emocao.", align="C")


def render_section_header(pdf, title):
    """Render a major section header in red with accent bar."""
    pdf.set_font("Helvetica", "B", 16)
    pdf.set_text_color(*ACCENT_COLOR)

    y = pdf.get_y()
    pdf.set_fill_color(*ACCENT_COLOR)
    pdf.rect(15, y, 3, 10, "F")

    pdf.set_x(22)
    pdf.multi_cell(0, 8, clean_text(title), new_x="LMARGIN", new_y="NEXT")

    # Underline
    y2 = pdf.get_y()
    pdf.set_fill_color(*ACCENT_COLOR)
    pdf.rect(15, y2, 170, 0.5, "F")
    pdf.ln(5)


def render_sub_header(pdf, title):
    """Render a ### sub-header."""
    pdf.set_font("Helvetica", "B", 11)
    pdf.set_text_color(*ACCENT2_COLOR)
    pdf.multi_cell(0, 6, clean_text(title), new_x="LMARGIN", new_y="NEXT")
    pdf.ln(2)


def render_text_line(pdf, text):
    """Render a single paragraph of normal text."""
    pdf.set_font("Helvetica", "", 10)
    pdf.set_text_color(*TEXT_COLOR)
    pdf.set_x(15)
    pdf.multi_cell(0, 5, clean_text(text), new_x="LMARGIN", new_y="NEXT")
    pdf.ln(1)


def render_bold_text(pdf, text):
    """Render bold text."""
    pdf.set_font("Helvetica", "B", 10)
    pdf.set_text_color(*TEXT_COLOR)
    pdf.multi_cell(0, 5, clean_text(text), new_x="LMARGIN", new_y="NEXT")
    pdf.ln(2)


def render_wireframe(pdf, code_lines):
    """Render ASCII wireframe in monospace font on a dark surface."""
    # Strip empty leading/trailing
    while code_lines and not code_lines[0].strip():
        code_lines.pop(0)
    while code_lines and not code_lines[-1].strip():
        code_lines.pop()

    if not code_lines:
        return

    line_h = 3.0
    block_h = len(code_lines) * line_h + 8
    available = 297 - pdf.get_y() - 24

    if block_h > available:
        pdf.add_dark_page()

    y_start = pdf.get_y()

    # Surface background
    pdf.set_fill_color(*SURFACE_COLOR)
    pdf.rect(14, y_start, 182, block_h, "F")

    # Left accent bar
    pdf.set_fill_color(50, 50, 65)
    pdf.rect(14, y_start, 2, block_h, "F")

    pdf.set_y(y_start + 4)
    pdf.set_font("Courier", "", 7)
    pdf.set_text_color(*CODE_TEXT)

    for line in code_lines:
        cleaned = clean_text(line)
        # Truncate very long lines
        if len(cleaned) > 110:
            cleaned = cleaned[:110]
        pdf.set_x(19)
        pdf.cell(0, line_h, cleaned, new_x="LMARGIN", new_y="NEXT")

    pdf.set_y(y_start + block_h + 2)
    pdf.ln(2)


def render_table(pdf, table_lines):
    """Render a markdown table with styled rows."""
    data = []
    for line in table_lines:
        line = line.strip()
        if line.startswith("|") and not all(c in "|-: " for c in line):
            cells = [c.strip() for c in line.split("|")[1:-1]]
            data.append(cells)

    if not data:
        return

    headers = data[0]
    rows = data[1:]
    ncols = len(headers)

    avail = 176
    if ncols == 4:
        col_w = [14, 60, 50, 30]
        if sum(col_w) > avail:
            col_w = [avail / ncols] * ncols
    elif ncols == 2:
        col_w = [50, 126]
    elif ncols == 5:
        col_w = [25, 45, 25, 35, 46]
    else:
        col_w = [avail / ncols] * ncols

    row_h = 5.5
    needed = (len(rows) + 2) * row_h + 10
    if needed > (297 - pdf.get_y() - 24):
        pdf.add_dark_page()

    x0 = 17

    # Header
    pdf.set_fill_color(30, 30, 42)
    pdf.set_font("Helvetica", "B", 8)
    pdf.set_text_color(*TEXT_COLOR)
    pdf.set_x(x0)
    for i, h in enumerate(headers):
        w = col_w[i] if i < len(col_w) else 30
        pdf.cell(w, row_h, clean_text(h), fill=True)
    pdf.ln()

    # Red separator
    y = pdf.get_y()
    pdf.set_draw_color(*ACCENT_COLOR)
    pdf.line(x0, y, x0 + sum(col_w[:ncols]), y)
    pdf.ln(1)

    # Rows
    pdf.set_font("Helvetica", "", 8)
    for idx, row in enumerate(rows):
        if idx % 2 == 0:
            pdf.set_fill_color(14, 14, 20)
        else:
            pdf.set_fill_color(22, 22, 32)
        pdf.set_text_color(200, 200, 210)
        pdf.set_x(x0)
        for i, cell in enumerate(row):
            w = col_w[i] if i < len(col_w) else 30
            pdf.cell(w, row_h, clean_text(cell), fill=True)
        pdf.ln()

    pdf.ln(4)


def render_bullet_list(pdf, items):
    """Render bullet points."""
    pdf.set_font("Helvetica", "", 10)
    for item in items:
        text = item.lstrip("- *").strip()
        if not text:
            continue
        if pdf.get_y() > 272:
            pdf.add_dark_page()
        pdf.set_x(20)
        pdf.set_text_color(*ACCENT_COLOR)
        pdf.cell(5, 5, ">")
        pdf.set_text_color(*TEXT_COLOR)
        pdf.multi_cell(0, 5, " " + clean_text(text), new_x="LMARGIN", new_y="NEXT")
    pdf.ln(2)


def render_numbered_list(pdf, items):
    """Render numbered list."""
    pdf.set_font("Helvetica", "", 10)
    pdf.set_text_color(*TEXT_COLOR)
    for item in items:
        if pdf.get_y() > 272:
            pdf.add_dark_page()
        pdf.set_x(20)
        pdf.multi_cell(0, 5, clean_text(item), new_x="LMARGIN", new_y="NEXT")
    pdf.ln(2)


# Patterns for sections that should start on a new page
NEW_PAGE_PATTERNS = [
    "mapa geral", "fluxo 1", "fluxo 2", "fluxo 3", "fluxo 4",
    "fluxo 5", "fluxo 6", "fluxo 7", "fluxo 8", "fluxo 9",
    "fluxo auth", "navegacao global", "principios de ux",
    "mapa de estados e transicoes", "inventario completo",
    "proximos passos",
    # user-flow-diagram
    "fluxo geral", "fluxo detalhado", "navegacao por tab",
    "mapa de estados da tela", "fluxo de permissoes",
    "inventario de telas",
]


def should_new_page(title):
    t = title.lower()
    for pat in NEW_PAGE_PATTERNS:
        if pat in t:
            return True
    return False


def render_section_content(pdf, content):
    """Parse and render content lines."""
    lines = content.split("\n")
    i = 0
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        if not stripped:
            i += 1
            continue

        # Code block
        if stripped.startswith("```"):
            code_lines = []
            i += 1
            while i < len(lines) and not lines[i].strip().startswith("```"):
                code_lines.append(lines[i])
                i += 1
            if i < len(lines):
                i += 1  # skip closing ```
            render_wireframe(pdf, code_lines)
            continue

        # Sub-header
        if stripped.startswith("### "):
            if pdf.get_y() > 255:
                pdf.add_dark_page()
            render_sub_header(pdf, stripped[4:])
            i += 1
            continue

        # Table
        if stripped.startswith("|"):
            table_lines = []
            while i < len(lines) and lines[i].strip().startswith("|"):
                table_lines.append(lines[i])
                i += 1
            render_table(pdf, table_lines)
            continue

        # Bullet list
        if stripped.startswith("- ") or stripped.startswith("* "):
            items = []
            while i < len(lines) and (lines[i].strip().startswith("- ") or lines[i].strip().startswith("* ")):
                items.append(lines[i].strip())
                i += 1
            render_bullet_list(pdf, items)
            continue

        # Numbered list
        if re.match(r"^\d+\.", stripped):
            items = []
            while i < len(lines) and re.match(r"^\d+\.", lines[i].strip()):
                items.append(lines[i].strip())
                i += 1
            render_numbered_list(pdf, items)
            continue

        # Bold standalone
        if stripped.startswith("**") and stripped.endswith("**"):
            if pdf.get_y() > 272:
                pdf.add_dark_page()
            render_bold_text(pdf, stripped.strip("* "))
            i += 1
            continue

        # Blockquote
        if stripped.startswith(">"):
            text = stripped.lstrip("> ").strip()
            if text:
                if pdf.get_y() > 272:
                    pdf.add_dark_page()
                y = pdf.get_y()
                pdf.set_fill_color(*ACCENT_COLOR)
                pdf.rect(17, y, 1.5, 6, "F")
                pdf.set_x(22)
                pdf.set_font("Helvetica", "I", 9)
                pdf.set_text_color(*MUTED_COLOR)
                pdf.multi_cell(0, 5, clean_text(text), new_x="LMARGIN", new_y="NEXT")
                pdf.ln(2)
            i += 1
            continue

        # Horizontal rule
        if stripped == "---":
            y = pdf.get_y()
            pdf.set_draw_color(40, 40, 55)
            pdf.line(15, y + 2, 195, y + 2)
            pdf.ln(6)
            i += 1
            continue

        # TOC-style links - skip
        if re.match(r"^\d+\.\s*\[", stripped):
            i += 1
            continue

        # Regular text
        if stripped:
            if pdf.get_y() > 272:
                pdf.add_dark_page()
            # Check for inline bold
            if "**" in stripped:
                # Render with bold parts
                cleaned = clean_text(stripped.replace("**", ""))
                render_text_line(pdf, cleaned)
            else:
                render_text_line(pdf, stripped)
        i += 1


def parse_sections(filepath):
    """Split markdown by ## headers."""
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()

    sections = []
    current = {"title": "", "content": ""}

    for line in content.split("\n"):
        if line.startswith("## "):
            if current["title"] or current["content"].strip():
                sections.append(current)
            current = {"title": line[3:].strip(), "content": ""}
        else:
            current["content"] += line + "\n"

    if current["title"] or current["content"].strip():
        sections.append(current)

    return sections


def render_divider_page(pdf, title, subtitle, note=""):
    """Render a section divider page."""
    pdf.add_dark_page()

    pdf.set_fill_color(*ACCENT_COLOR)
    pdf.rect(20, 80, 170, 2, "F")

    pdf.set_y(95)
    pdf.set_font("Helvetica", "B", 24)
    pdf.set_text_color(*TEXT_COLOR)
    pdf.cell(0, 12, clean_text(title), align="C", new_x="LMARGIN", new_y="NEXT")

    pdf.set_y(115)
    pdf.set_font("Helvetica", "", 12)
    pdf.set_text_color(*MUTED_COLOR)
    for line in subtitle.split("\n"):
        pdf.cell(0, 7, clean_text(line), align="C", new_x="LMARGIN", new_y="NEXT")

    if note:
        pdf.set_y(140)
        pdf.set_font("Helvetica", "I", 10)
        pdf.set_text_color(*MUTED_COLOR)
        pdf.cell(0, 7, clean_text(note), align="C", new_x="LMARGIN", new_y="NEXT")

    pdf.set_fill_color(*ACCENT_COLOR)
    pdf.rect(20, 260, 170, 2, "F")


def main():
    pdf = TumtumPDF()
    pdf.set_margin(15)

    # ============================================================
    # Title page
    # ============================================================
    render_title_page(pdf)

    # ============================================================
    # Part 1: app-structure.md
    # ============================================================
    sections = parse_sections("/home/user/tumtum/docs/ux/app-structure.md")

    for section in sections:
        title = section["title"]
        content = section["content"]

        if not title and not content.strip():
            continue

        # Skip pure index section
        if title.lower().startswith("indice"):
            continue

        if title and should_new_page(title):
            pdf.add_dark_page()
            render_section_header(pdf, title)
        elif title:
            if pdf.get_y() > 235:
                pdf.add_dark_page()
            render_section_header(pdf, title)

        if content.strip():
            render_section_content(pdf, content)

    # ============================================================
    # Divider for user-flow-diagram.md
    # ============================================================
    render_divider_page(
        pdf,
        "Diagramas de Fluxo UX",
        "Todas as telas e transicoes do app,\ndo primeiro acesso ao compartilhamento",
        "(Mermaid diagrams rendered as text descriptions)"
    )

    # ============================================================
    # Part 2: user-flow-diagram.md
    # ============================================================
    sections2 = parse_sections("/home/user/tumtum/docs/ux/user-flow-diagram.md")

    for section in sections2:
        title = section["title"]
        content = section["content"]

        if not title and not content.strip():
            continue

        if title and should_new_page(title):
            pdf.add_dark_page()
            render_section_header(pdf, title)
        elif title:
            if pdf.get_y() > 235:
                pdf.add_dark_page()
            render_section_header(pdf, title)

        if content.strip():
            render_section_content(pdf, content)

    # ============================================================
    # Save
    # ============================================================
    output_path = "/home/user/tumtum/docs/ux/tumtum-ux-structure.pdf"
    pdf.output(output_path)

    size = os.path.getsize(output_path)
    print(f"PDF generated: {output_path}")
    print(f"File size: {size:,} bytes ({size / 1024:.1f} KB)")


if __name__ == "__main__":
    main()
