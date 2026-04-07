#!/usr/bin/env python3
"""Convert strategy markdown files to styled PDFs using WeasyPrint."""

import sys
import markdown
from weasyprint import HTML
from pathlib import Path

STRATEGY_DIR = Path(__file__).parent

DOCUMENTS = {
    "gtm": {
        "md": STRATEGY_DIR / "GTM_STRATEGY.md",
        "pdf": STRATEGY_DIR / "TumTum_GTM_Strategy.pdf",
        "footer": "TumTum — GTM Strategy",
    },
    "partnerships": {
        "md": STRATEGY_DIR / "PARTNERSHIP_ANALYSIS.md",
        "pdf": STRATEGY_DIR / "TumTum_Partnership_Analysis.pdf",
        "footer": "TumTum — Partnership Analysis",
    },
}

CSS_TEMPLATE = """
@page {{
    size: A4;
    margin: 2.2cm 2cm 2.2cm 2cm;
    @bottom-center {{
        content: "{footer}  |  Página " counter(page) " de " counter(pages);
        font-size: 8pt;
        color: #6B6B80;
        font-family: system-ui, -apple-system, sans-serif;
    }}
}}

body {{
    font-family: system-ui, -apple-system, 'Segoe UI', sans-serif;
    font-size: 10.5pt;
    line-height: 1.6;
    color: #1a1a2e;
    background: #ffffff;
}}

h1 {{
    font-family: Georgia, serif;
    font-size: 24pt;
    color: #C0392B;
    text-transform: uppercase;
    letter-spacing: 2px;
    border-bottom: 3px solid #C0392B;
    padding-bottom: 10px;
    margin-top: 0;
    page-break-before: avoid;
}}

h2 {{
    font-family: Georgia, serif;
    font-size: 16pt;
    color: #C0392B;
    margin-top: 28px;
    padding-top: 12px;
    border-top: 1px solid #e0e0e0;
    page-break-after: avoid;
}}

h3 {{
    font-size: 12pt;
    color: #2c2c4a;
    margin-top: 18px;
    font-weight: 700;
    page-break-after: avoid;
}}

h4 {{
    font-size: 11pt;
    color: #444466;
    margin-top: 14px;
    font-weight: 600;
}}

blockquote {{
    border-left: 4px solid #C0392B;
    background: #fdf2f2;
    margin: 14px 0;
    padding: 10px 16px;
    font-style: italic;
    color: #333;
    border-radius: 0 4px 4px 0;
    page-break-inside: avoid;
}}

table {{
    width: 100%;
    border-collapse: collapse;
    margin: 12px 0;
    font-size: 9.5pt;
    page-break-inside: avoid;
}}

th {{
    background: #C0392B;
    color: white;
    padding: 8px 10px;
    text-align: left;
    font-weight: 600;
    font-size: 9pt;
}}

td {{
    padding: 7px 10px;
    border-bottom: 1px solid #e8e8e8;
    vertical-align: top;
}}

tr:nth-child(even) td {{
    background: #f8f8fa;
}}

code {{
    font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
    font-size: 9pt;
    background: #f4f4f8;
    padding: 1px 4px;
    border-radius: 3px;
    color: #C0392B;
}}

pre {{
    background: #0d0d14;
    color: #e8e8f0;
    padding: 14px 16px;
    border-radius: 6px;
    font-size: 8.5pt;
    line-height: 1.5;
    overflow-x: auto;
    page-break-inside: avoid;
    border-left: 4px solid #C0392B;
}}

pre code {{
    background: none;
    color: #e8e8f0;
    padding: 0;
}}

strong {{
    color: #1a1a2e;
}}

a {{
    color: #C0392B;
    text-decoration: none;
}}

hr {{
    border: none;
    border-top: 2px solid #C0392B;
    margin: 24px 0;
    opacity: 0.3;
}}

ul, ol {{
    padding-left: 20px;
}}

li {{
    margin-bottom: 4px;
}}

/* Cover-like first section */
h1:first-of-type {{
    font-size: 28pt;
    margin-top: 60px;
    margin-bottom: 6px;
    text-align: center;
    border-bottom: none;
}}

/* Make the subtitle blockquote look like a cover element */
h1:first-of-type + blockquote {{
    text-align: center;
    border-left: none;
    background: none;
    font-size: 9.5pt;
    color: #6B6B80;
}}
"""


def generate_pdf(doc_key: str) -> None:
    doc = DOCUMENTS[doc_key]
    md_text = doc["md"].read_text(encoding="utf-8")
    css = CSS_TEMPLATE.format(footer=doc["footer"])

    extensions = ["tables", "fenced_code", "toc", "smarty"]
    html_body = markdown.markdown(md_text, extensions=extensions)

    full_html = f"""<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="utf-8">
    <style>{css}</style>
</head>
<body>
{html_body}
</body>
</html>"""

    HTML(string=full_html).write_pdf(str(doc["pdf"]))
    size_kb = doc["pdf"].stat().st_size / 1024
    print(f"PDF generated: {doc['pdf']} ({size_kb:.0f} KB)")


def main():
    targets = sys.argv[1:] if len(sys.argv) > 1 else list(DOCUMENTS.keys())
    for target in targets:
        if target not in DOCUMENTS:
            print(f"Unknown document: {target}. Available: {list(DOCUMENTS.keys())}")
            continue
        generate_pdf(target)


if __name__ == "__main__":
    main()
