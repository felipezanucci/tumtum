#!/usr/bin/env python3
"""Generate a styled PDF from the Tumtum Creative Brief markdown."""

import markdown
from weasyprint import HTML
from pathlib import Path

SCRIPT_DIR = Path(__file__).parent
MD_PATH = SCRIPT_DIR / "CREATIVE_BRIEF.md"
PDF_PATH = SCRIPT_DIR / "CREATIVE_BRIEF.pdf"

CSS = """
@page {
    size: A4;
    margin: 28mm 24mm 28mm 24mm;
    @bottom-center {
        content: "Tumtum — Briefing Criativo  |  Página " counter(page);
        font-family: system-ui, -apple-system, sans-serif;
        font-size: 8pt;
        color: #6B6B80;
    }
}

body {
    font-family: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
    font-size: 10.5pt;
    line-height: 1.65;
    color: #1a1a2e;
    background: #ffffff;
}

/* ---- Cover / Title ---- */
h1 {
    font-size: 28pt;
    font-weight: 800;
    color: #C0392B;
    border-bottom: 3px solid #C0392B;
    padding-bottom: 12px;
    margin-top: 0;
    margin-bottom: 6px;
    letter-spacing: 0.5px;
}

/* Subtitle line right after h1 */
h1 + p {
    color: #6B6B80;
    font-size: 9.5pt;
    margin-top: 0;
    margin-bottom: 24px;
}

h2 {
    font-size: 16pt;
    font-weight: 700;
    color: #C0392B;
    margin-top: 32px;
    margin-bottom: 10px;
    padding-bottom: 4px;
    border-bottom: 1.5px solid #E8E8EE;
    page-break-after: avoid;
}

h3 {
    font-size: 12pt;
    font-weight: 700;
    color: #1a1a2e;
    margin-top: 20px;
    margin-bottom: 8px;
    page-break-after: avoid;
}

h4 {
    font-size: 10.5pt;
    font-weight: 700;
    color: #444;
    margin-top: 16px;
    margin-bottom: 6px;
}

p {
    margin: 0 0 10px 0;
}

/* ---- Horizontal rule as section divider ---- */
hr {
    border: none;
    border-top: 1.5px solid #E8E8EE;
    margin: 28px 0;
}

/* ---- Blockquotes (used for the summary) ---- */
blockquote {
    border-left: 4px solid #C0392B;
    background: #FDF2F2;
    padding: 14px 18px;
    margin: 18px 0;
    font-style: italic;
    font-size: 11pt;
    color: #333;
    border-radius: 0 6px 6px 0;
}
blockquote p {
    margin: 0;
}

/* ---- Tables ---- */
table {
    width: 100%;
    border-collapse: collapse;
    margin: 14px 0 18px 0;
    font-size: 9.5pt;
}

thead {
    background: #C0392B;
    color: #fff;
}
thead th {
    padding: 8px 12px;
    text-align: left;
    font-weight: 600;
    border: 1px solid #C0392B;
}

tbody td {
    padding: 7px 12px;
    border: 1px solid #E0E0E8;
    vertical-align: top;
}

tbody tr:nth-child(even) {
    background: #F8F8FC;
}

/* ---- Lists ---- */
ul, ol {
    margin: 8px 0 14px 0;
    padding-left: 22px;
}
li {
    margin-bottom: 5px;
}

/* Checkbox lists */
li input[type="checkbox"] {
    margin-right: 6px;
}

/* ---- Inline code ---- */
code {
    background: #F0F0F5;
    padding: 1px 5px;
    border-radius: 3px;
    font-family: 'SF Mono', 'Fira Code', Consolas, monospace;
    font-size: 9pt;
    color: #C0392B;
}

/* ---- Strong / Bold ---- */
strong {
    font-weight: 700;
    color: #111;
}

/* ---- Emphasis ---- */
em {
    font-style: italic;
    color: #444;
}

/* ---- Avoid page breaks inside blocks ---- */
table, blockquote, ul, ol {
    page-break-inside: avoid;
}
h2, h3 {
    page-break-after: avoid;
}
tr {
    page-break-inside: avoid;
}

/* ---- Confidentiality footer ---- */
body > p:last-child {
    font-style: italic;
    font-size: 8.5pt;
    color: #999;
    margin-top: 32px;
    padding-top: 12px;
    border-top: 1px solid #E8E8EE;
}
"""

def main():
    md_text = MD_PATH.read_text(encoding="utf-8")

    # Convert checkbox syntax to HTML
    md_text = md_text.replace("- [ ]", "- ☐")
    md_text = md_text.replace("- [x]", "- ☑")

    html_body = markdown.markdown(
        md_text,
        extensions=["tables", "fenced_code", "smarty"],
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

    HTML(string=full_html).write_pdf(str(PDF_PATH))
    print(f"PDF generated: {PDF_PATH}")

if __name__ == "__main__":
    main()
