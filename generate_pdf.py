#!/usr/bin/env python3
"""Generate a professional PDF from the TumTum IDEA_REVIEW.md analysis."""

import re
from fpdf import FPDF

# Brand colors
RED = (192, 57, 43)        # #C0392B
DARK_BG = (8, 8, 12)       # #08080C
SURFACE = (17, 17, 24)     # #111118
TEXT_PRIMARY = (240, 240, 245)  # #F0F0F5
TEXT_MUTED = (107, 107, 128)    # #6B6B80
ACCENT_CYAN = (0, 210, 255)    # #00D2FF
WHITE = (255, 255, 255)
BLACK = (0, 0, 0)
DARK_GRAY = (30, 30, 40)
MID_GRAY = (60, 60, 75)
LIGHT_GRAY = (200, 200, 210)
TABLE_HEADER_BG = (40, 40, 55)
TABLE_ROW_BG = (22, 22, 32)
TABLE_ROW_ALT = (28, 28, 38)


class TumTumPDF(FPDF):
    def __init__(self):
        super().__init__()
        self.add_font("DejaVu", "", "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", uni=True)
        self.add_font("DejaVu", "B", "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", uni=True)
        self.add_font("DejaVuMono", "", "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf", uni=True)
        self.set_auto_page_break(auto=True, margin=20)

    def header(self):
        if self.page_no() == 1:
            return
        self.set_fill_color(*DARK_BG)
        self.rect(0, 0, 210, 12, "F")
        self.set_font("DejaVu", "B", 7)
        self.set_text_color(*TEXT_MUTED)
        self.set_y(3)
        self.cell(0, 5, "TUMTUM \u2014 REVIS\u00c3O COMPLETA DA IDEIA  |  ABRIL 2026", align="C")
        self.ln(12)

    def footer(self):
        self.set_y(-15)
        self.set_font("DejaVu", "", 7)
        self.set_text_color(*TEXT_MUTED)
        self.cell(0, 10, f"P\u00e1gina {self.page_no()}", align="C")


def draw_cover(pdf):
    """Draw the cover page."""
    pdf.add_page()
    # Dark background
    pdf.set_fill_color(*DARK_BG)
    pdf.rect(0, 0, 210, 297, "F")

    # Red accent bar at top
    pdf.set_fill_color(*RED)
    pdf.rect(0, 0, 210, 4, "F")

    # Logo / Title area
    pdf.set_y(70)
    pdf.set_font("DejaVu", "B", 42)
    pdf.set_text_color(*WHITE)
    pdf.cell(0, 20, "TUMTUM", align="C", new_x="LMARGIN", new_y="NEXT")

    # Heartbeat line decoration
    pdf.set_draw_color(*RED)
    pdf.set_line_width(1.5)
    y = pdf.get_y() + 5
    cx = 105
    points = [
        (cx - 40, y), (cx - 25, y), (cx - 18, y - 12), (cx - 12, y + 8),
        (cx - 6, y - 18), (cx, y + 10), (cx + 6, y - 6), (cx + 12, y),
        (cx + 25, y), (cx + 40, y)
    ]
    for i in range(len(points) - 1):
        pdf.line(points[i][0], points[i][1], points[i + 1][0], points[i + 1][1])

    pdf.set_y(y + 25)
    pdf.set_font("DejaVu", "B", 18)
    pdf.set_text_color(*LIGHT_GRAY)
    pdf.cell(0, 10, "Revis\u00e3o Completa da Ideia", align="C", new_x="LMARGIN", new_y="NEXT")

    pdf.ln(5)
    pdf.set_font("DejaVu", "", 11)
    pdf.set_text_color(*TEXT_MUTED)
    pdf.cell(0, 8, "An\u00e1lise Estrat\u00e9gica  |  Mercado  |  Tecnologia  |  Financeiro  |  Produto", align="C", new_x="LMARGIN", new_y="NEXT")

    # Verdict box
    pdf.set_y(170)
    pdf.set_fill_color(*SURFACE)
    pdf.set_draw_color(*RED)
    pdf.set_line_width(0.8)
    pdf.rect(30, 168, 150, 45, "DF")

    pdf.set_y(173)
    pdf.set_font("DejaVu", "B", 13)
    pdf.set_text_color(*RED)
    pdf.cell(0, 8, "VEREDITO", align="C", new_x="LMARGIN", new_y="NEXT")

    pdf.set_font("DejaVu", "B", 22)
    pdf.set_text_color(*WHITE)
    pdf.cell(0, 12, "PROSSEGUIR  \u2014  7.5/10", align="C", new_x="LMARGIN", new_y="NEXT")

    pdf.set_font("DejaVu", "", 9)
    pdf.set_text_color(*TEXT_MUTED)
    pdf.cell(0, 7, "Com 5 ressalvas cr\u00edticas a resolver antes de investir", align="C", new_x="LMARGIN", new_y="NEXT")

    # Bottom info
    pdf.set_y(250)
    pdf.set_font("DejaVu", "", 9)
    pdf.set_text_color(*TEXT_MUTED)
    pdf.cell(0, 6, "Abril 2026  |  5 An\u00e1lises Independentes", align="C", new_x="LMARGIN", new_y="NEXT")
    pdf.cell(0, 6, "Mercado & Competi\u00e7\u00e3o  \u2022  GTM & Pricing  \u2022  Tecnologia  \u2022  Financeiro  \u2022  Produto & UX", align="C", new_x="LMARGIN", new_y="NEXT")

    # Red accent bar at bottom
    pdf.set_fill_color(*RED)
    pdf.rect(0, 293, 210, 4, "F")


def new_content_page(pdf):
    """Add a new page with dark background."""
    pdf.add_page()
    pdf.set_fill_color(*DARK_BG)
    pdf.rect(0, 0, 210, 297, "F")
    pdf.set_y(18)


def write_section_title(pdf, text):
    """Write a major section title with red accent."""
    if pdf.get_y() > 240:
        new_content_page(pdf)
    pdf.ln(6)
    pdf.set_fill_color(*RED)
    pdf.rect(15, pdf.get_y(), 3, 10, "F")
    pdf.set_x(22)
    pdf.set_font("DejaVu", "B", 16)
    pdf.set_text_color(*WHITE)
    pdf.cell(0, 10, text, new_x="LMARGIN", new_y="NEXT")
    pdf.ln(3)


def write_subsection(pdf, text):
    """Write a subsection title."""
    if pdf.get_y() > 255:
        new_content_page(pdf)
    pdf.ln(4)
    pdf.set_font("DejaVu", "B", 12)
    pdf.set_text_color(*ACCENT_CYAN)
    pdf.cell(0, 8, text, new_x="LMARGIN", new_y="NEXT")
    pdf.ln(1)


def write_subsubsection(pdf, text):
    """Write a sub-subsection title."""
    if pdf.get_y() > 260:
        new_content_page(pdf)
    pdf.ln(3)
    pdf.set_font("DejaVu", "B", 10)
    pdf.set_text_color(*LIGHT_GRAY)
    pdf.cell(0, 7, text, new_x="LMARGIN", new_y="NEXT")
    pdf.ln(1)


def write_paragraph(pdf, text):
    """Write a paragraph of body text."""
    if pdf.get_y() > 268:
        new_content_page(pdf)
    pdf.set_font("DejaVu", "", 9)
    pdf.set_text_color(*LIGHT_GRAY)
    pdf.set_x(15)
    pdf.multi_cell(180, 5, text)
    pdf.ln(2)


def write_bold_paragraph(pdf, text):
    """Write bold text."""
    if pdf.get_y() > 268:
        new_content_page(pdf)
    pdf.set_font("DejaVu", "B", 9)
    pdf.set_text_color(*WHITE)
    pdf.set_x(15)
    pdf.multi_cell(180, 5, text)
    pdf.ln(1)


def write_bullet(pdf, text):
    """Write a bullet point."""
    if pdf.get_y() > 270:
        new_content_page(pdf)
    pdf.set_font("DejaVu", "", 9)
    pdf.set_text_color(*LIGHT_GRAY)
    pdf.set_x(20)
    pdf.cell(5, 5, "\u2022")
    pdf.set_x(26)
    pdf.multi_cell(164, 5, text)
    pdf.ln(1)


def write_checkbox(pdf, text, checked=False):
    """Write a checkbox item."""
    if pdf.get_y() > 270:
        new_content_page(pdf)
    pdf.set_font("DejaVu", "", 9)
    pdf.set_text_color(*LIGHT_GRAY)
    mark = "\u2611" if checked else "\u2610"
    pdf.set_x(20)
    pdf.cell(5, 5, mark)
    pdf.set_x(27)
    pdf.multi_cell(163, 5, text)
    pdf.ln(1)


def write_quote(pdf, text):
    """Write a blockquote."""
    if pdf.get_y() > 260:
        new_content_page(pdf)
    pdf.set_fill_color(35, 35, 50)
    y_start = pdf.get_y()
    pdf.set_x(20)
    pdf.set_font("DejaVu", "B", 9)
    pdf.set_text_color(*ACCENT_CYAN)
    pdf.multi_cell(170, 5.5, text)
    y_end = pdf.get_y()
    pdf.set_fill_color(*RED)
    pdf.rect(16, y_start, 2, y_end - y_start, "F")
    pdf.ln(3)


def write_table(pdf, headers, rows):
    """Write a formatted table."""
    if pdf.get_y() > 230:
        new_content_page(pdf)

    num_cols = len(headers)
    available_width = 180
    col_widths = [available_width / num_cols] * num_cols

    # Try to make smart column widths based on content
    if num_cols == 2:
        col_widths = [70, 110]
    elif num_cols == 3:
        col_widths = [50, 70, 60]
    elif num_cols == 4:
        col_widths = [35, 50, 50, 45]
    elif num_cols == 5:
        col_widths = [35, 35, 35, 35, 40]
    elif num_cols == 6:
        col_widths = [25, 30, 30, 30, 30, 35]

    # Ensure widths sum to available_width
    total = sum(col_widths)
    col_widths = [w * available_width / total for w in col_widths]

    # Header
    pdf.set_font("DejaVu", "B", 7.5)
    pdf.set_fill_color(*TABLE_HEADER_BG)
    pdf.set_text_color(*WHITE)
    x_start = 15
    pdf.set_x(x_start)
    for i, h in enumerate(headers):
        pdf.cell(col_widths[i], 7, f" {h}", fill=True, border=0)
    pdf.ln()

    # Rows
    pdf.set_font("DejaVu", "", 7.5)
    for row_idx, row in enumerate(rows):
        if pdf.get_y() > 272:
            new_content_page(pdf)
            # Re-draw header
            pdf.set_font("DejaVu", "B", 7.5)
            pdf.set_fill_color(*TABLE_HEADER_BG)
            pdf.set_text_color(*WHITE)
            pdf.set_x(x_start)
            for i, h in enumerate(headers):
                pdf.cell(col_widths[i], 7, f" {h}", fill=True, border=0)
            pdf.ln()
            pdf.set_font("DejaVu", "", 7.5)

        bg = TABLE_ROW_BG if row_idx % 2 == 0 else TABLE_ROW_ALT
        pdf.set_fill_color(*bg)
        pdf.set_text_color(*LIGHT_GRAY)
        pdf.set_x(x_start)

        # Calculate max height needed
        max_lines = 1
        for i, cell in enumerate(row):
            text = str(cell).strip()
            char_width = col_widths[i] / 3.2  # approximate chars per line
            lines = max(1, len(text) / max(char_width, 1))
            max_lines = max(max_lines, lines)

        row_h = max(7, int(max_lines * 5.5))

        for i, cell in enumerate(row):
            text = f" {str(cell).strip()}"
            pdf.cell(col_widths[i], row_h, text, fill=True, border=0)
        pdf.ln()

    pdf.ln(4)


def parse_markdown_and_render(pdf, filepath):
    """Parse the markdown file and render to PDF."""
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()

    lines = content.split("\n")
    i = 0
    in_table = False
    table_headers = []
    table_rows = []

    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        # Skip empty lines
        if not stripped:
            if in_table and table_headers:
                write_table(pdf, table_headers, table_rows)
                in_table = False
                table_headers = []
                table_rows = []
            i += 1
            continue

        # Table detection
        if "|" in stripped and not stripped.startswith("```"):
            cells = [c.strip() for c in stripped.split("|")]
            cells = [c for c in cells if c]

            # Skip separator rows
            if all(re.match(r'^[-:]+$', c) for c in cells):
                i += 1
                continue

            if not in_table:
                in_table = True
                # Clean bold markers from headers
                table_headers = [c.replace("**", "").strip() for c in cells]
            else:
                # Clean bold markers from cells
                clean_cells = [c.replace("**", "").strip() for c in cells]
                table_rows.append(clean_cells)
            i += 1
            continue

        # If we were in a table and now we're not, flush it
        if in_table and table_headers:
            write_table(pdf, table_headers, table_rows)
            in_table = False
            table_headers = []
            table_rows = []

        # H1
        if stripped.startswith("# ") and not stripped.startswith("##"):
            # Skip the main title (it's on the cover)
            i += 1
            continue

        # H2
        if stripped.startswith("## "):
            text = stripped[3:].strip()
            write_section_title(pdf, text)
            i += 1
            continue

        # H3
        if stripped.startswith("### "):
            text = stripped[4:].strip()
            write_subsection(pdf, text)
            i += 1
            continue

        # H4
        if stripped.startswith("#### "):
            text = stripped[5:].strip()
            write_subsubsection(pdf, text)
            i += 1
            continue

        # Blockquote
        if stripped.startswith("> "):
            text = stripped[2:].strip()
            # Collect multi-line quotes
            while i + 1 < len(lines) and lines[i + 1].strip().startswith("> "):
                i += 1
                text += " " + lines[i].strip()[2:].strip()
            text = text.replace("**", "")
            write_quote(pdf, text)
            i += 1
            continue

        # Horizontal rule
        if stripped == "---":
            if pdf.get_y() > 265:
                new_content_page(pdf)
            pdf.ln(3)
            pdf.set_draw_color(*MID_GRAY)
            pdf.set_line_width(0.3)
            pdf.line(15, pdf.get_y(), 195, pdf.get_y())
            pdf.ln(5)
            i += 1
            continue

        # Checkbox
        if stripped.startswith("- [ ] ") or stripped.startswith("- [x] "):
            checked = stripped.startswith("- [x]")
            text = stripped[6:].strip().replace("**", "")
            write_checkbox(pdf, text, checked)
            i += 1
            continue

        # Bullet points
        if stripped.startswith("- ") or stripped.startswith("* "):
            text = stripped[2:].strip().replace("**", "")
            write_bullet(pdf, text)
            i += 1
            continue

        # Numbered lists
        if re.match(r'^\d+\.\s', stripped):
            text = re.sub(r'^\d+\.\s*', '', stripped).replace("**", "")
            write_bullet(pdf, text)
            i += 1
            continue

        # Italic/emphasis block (standalone)
        if stripped.startswith("*") and stripped.endswith("*") and not stripped.startswith("**"):
            text = stripped.strip("*").strip()
            pdf.set_font("DejaVu", "", 8)
            pdf.set_text_color(*TEXT_MUTED)
            pdf.set_x(15)
            pdf.multi_cell(180, 5, text)
            pdf.ln(2)
            i += 1
            continue

        # Bold paragraph
        if stripped.startswith("**") and stripped.endswith("**"):
            text = stripped.strip("*").strip()
            write_bold_paragraph(pdf, text)
            i += 1
            continue

        # Regular paragraph
        text = stripped.replace("**", "").replace("*", "")
        # Clean up markdown links
        text = re.sub(r'\[([^\]]+)\]\([^)]+\)', r'\1', text)
        write_paragraph(pdf, text)
        i += 1

    # Flush any remaining table
    if in_table and table_headers:
        write_table(pdf, table_headers, table_rows)


def main():
    pdf = TumTumPDF()
    pdf.set_title("TumTum - Revisao Completa da Ideia")
    pdf.set_author("TumTum Strategic Analysis")

    # Cover page
    draw_cover(pdf)

    # First content page
    new_content_page(pdf)

    # Parse and render the markdown
    parse_markdown_and_render(pdf, "/home/user/tumtum/IDEA_REVIEW.md")

    # Final page - closing quote
    new_content_page(pdf)
    pdf.set_y(100)

    pdf.set_fill_color(*SURFACE)
    pdf.set_draw_color(*RED)
    pdf.set_line_width(0.8)
    pdf.rect(25, 90, 160, 50, "DF")

    pdf.set_y(97)
    pdf.set_font("DejaVu", "B", 14)
    pdf.set_text_color(*WHITE)
    pdf.cell(0, 10, '"O batimento card\u00edaco \u00e9 s\u00f3 o meio.', align="C", new_x="LMARGIN", new_y="NEXT")
    pdf.cell(0, 10, 'A emo\u00e7\u00e3o compartilhada \u00e9 o produto."', align="C", new_x="LMARGIN", new_y="NEXT")

    pdf.ln(5)
    pdf.set_font("DejaVu", "", 9)
    pdf.set_text_color(*TEXT_MUTED)
    pdf.cell(0, 6, "\u2014 Conclus\u00e3o da An\u00e1lise Estrat\u00e9gica TumTum, Abril 2026", align="C")

    output_path = "/home/user/tumtum/IDEA_REVIEW.pdf"
    pdf.output(output_path)
    print(f"PDF generated: {output_path}")


if __name__ == "__main__":
    main()
