#!/usr/bin/env python3
"""Generate PDF from the Block AI Strategy research markdown document."""

import markdown
from weasyprint import HTML
from pathlib import Path

MD_FILE = Path(__file__).parent / "block-ai-strategy-applied-to-tumtum.md"
PDF_FILE = Path(__file__).parent / "block-ai-strategy-applied-to-tumtum.pdf"

CSS = """
@page {
    size: A4;
    margin: 2cm 2.2cm;
    @bottom-center {
        content: "TumTum — Pesquisa Estratégica AI  •  Página " counter(page) " de " counter(pages);
        font-size: 8pt;
        color: #6B6B80;
        font-family: system-ui, -apple-system, sans-serif;
    }
}

body {
    font-family: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
    font-size: 10.5pt;
    line-height: 1.55;
    color: #1a1a2e;
    max-width: 100%;
}

h1 {
    font-family: Georgia, serif;
    font-size: 22pt;
    color: #C0392B;
    border-bottom: 3px solid #C0392B;
    padding-bottom: 8px;
    margin-top: 0;
    letter-spacing: 0.5px;
}

h2 {
    font-family: Georgia, serif;
    font-size: 16pt;
    color: #08080C;
    border-bottom: 1.5px solid #E74C3C;
    padding-bottom: 5px;
    margin-top: 28px;
    page-break-after: avoid;
}

h3 {
    font-size: 13pt;
    color: #C0392B;
    margin-top: 22px;
    page-break-after: avoid;
}

h4 {
    font-size: 11.5pt;
    color: #333;
    margin-top: 18px;
    page-break-after: avoid;
}

h5 {
    font-size: 10.5pt;
    color: #C0392B;
    margin-top: 16px;
    font-weight: 700;
    page-break-after: avoid;
}

blockquote {
    border-left: 4px solid #C0392B;
    background: #fdf2f2;
    padding: 10px 16px;
    margin: 16px 0;
    font-style: italic;
    color: #444;
}

table {
    width: 100%;
    border-collapse: collapse;
    margin: 12px 0;
    font-size: 9.5pt;
    page-break-inside: auto;
}

thead {
    display: table-header-group;
}

tr {
    page-break-inside: avoid;
    page-break-after: auto;
}

th {
    background: #C0392B;
    color: white;
    padding: 7px 10px;
    text-align: left;
    font-weight: 600;
    font-size: 9pt;
}

td {
    padding: 6px 10px;
    border-bottom: 1px solid #e0e0e0;
    vertical-align: top;
}

tr:nth-child(even) td {
    background: #f9f9fb;
}

code {
    background: #f0f0f5;
    padding: 1px 5px;
    border-radius: 3px;
    font-size: 9pt;
    font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
    color: #C0392B;
}

pre {
    background: #111118;
    color: #F0F0F5;
    padding: 14px 18px;
    border-radius: 6px;
    font-size: 8.5pt;
    line-height: 1.45;
    overflow-x: auto;
    page-break-inside: avoid;
    border-left: 4px solid #C0392B;
}

pre code {
    background: none;
    color: #F0F0F5;
    padding: 0;
}

a {
    color: #C0392B;
    text-decoration: none;
}

a:hover {
    text-decoration: underline;
}

strong {
    color: #1a1a2e;
}

hr {
    border: none;
    border-top: 2px solid #e0e0e0;
    margin: 24px 0;
}

ul, ol {
    padding-left: 22px;
}

li {
    margin-bottom: 3px;
}

/* Task list styling */
li input[type="checkbox"] {
    margin-right: 6px;
}

/* Cover-like styling for the first section */
h1 + blockquote {
    background: #fdf2f2;
    border-left: 4px solid #C0392B;
    font-size: 9.5pt;
}
"""


def main():
    md_content = MD_FILE.read_text(encoding="utf-8")

    html_body = markdown.markdown(
        md_content,
        extensions=["tables", "fenced_code", "toc", "smarty"],
    )

    full_html = f"""<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="utf-8">
    <style>{CSS}</style>
</head>
<body>
{html_body}
</body>
</html>"""

    HTML(string=full_html).write_pdf(str(PDF_FILE))
    print(f"PDF generated: {PDF_FILE}")
    print(f"Size: {PDF_FILE.stat().st_size / 1024:.0f} KB")


if __name__ == "__main__":
    main()
