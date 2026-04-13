#!/usr/bin/env python3
"""Generate a styled PDF from the product creation guide markdown."""

import markdown
from weasyprint import HTML

MD_PATH = "docs/product-creation-guide.md"
PDF_PATH = "docs/product-creation-guide.pdf"

CSS = """
@page {
    size: A4;
    margin: 2.5cm 2cm;
    @bottom-center {
        content: counter(page);
        font-size: 10px;
        color: #6B6B80;
    }
}

body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
    font-size: 11pt;
    line-height: 1.6;
    color: #F0F0F5;
    background-color: #08080C;
}

h1 {
    font-family: Georgia, serif;
    font-size: 28pt;
    font-weight: bold;
    color: #C0392B;
    text-transform: uppercase;
    letter-spacing: 2px;
    text-align: center;
    margin-top: 60px;
    margin-bottom: 10px;
    padding-bottom: 20px;
    border-bottom: 3px solid #C0392B;
}

h2 {
    font-family: Georgia, serif;
    font-size: 18pt;
    font-weight: bold;
    color: #E74C3C;
    margin-top: 35px;
    margin-bottom: 12px;
    padding-bottom: 6px;
    border-bottom: 1px solid #1A1A24;
}

h3 {
    font-family: Georgia, serif;
    font-size: 13pt;
    font-weight: bold;
    color: #F0F0F5;
    margin-top: 22px;
    margin-bottom: 8px;
}

p {
    margin-bottom: 10px;
}

blockquote {
    border-left: 4px solid #C0392B;
    margin: 16px 0;
    padding: 12px 20px;
    background-color: #111118;
    border-radius: 0 6px 6px 0;
    font-style: italic;
    color: #ddd;
}

blockquote p {
    margin: 0;
}

strong {
    color: #fff;
    font-weight: 700;
}

a {
    color: #00D2FF;
    text-decoration: none;
}

a:hover {
    text-decoration: underline;
}

ul, ol {
    margin-bottom: 12px;
    padding-left: 24px;
}

li {
    margin-bottom: 5px;
}

table {
    width: 100%;
    border-collapse: collapse;
    margin: 16px 0;
    font-size: 10pt;
}

thead {
    background-color: #C0392B;
}

th {
    color: #fff;
    font-weight: 700;
    padding: 10px 14px;
    text-align: left;
    font-size: 10pt;
}

td {
    padding: 9px 14px;
    border-bottom: 1px solid #1A1A24;
    color: #F0F0F5;
}

tr:nth-child(even) td {
    background-color: #111118;
}

tr:nth-child(odd) td {
    background-color: #0d0d14;
}

hr {
    border: none;
    border-top: 1px solid #1A1A24;
    margin: 30px 0;
}

code {
    background-color: #111118;
    padding: 2px 6px;
    border-radius: 3px;
    font-size: 10pt;
}
"""


def main():
    with open(MD_PATH, "r", encoding="utf-8") as f:
        md_content = f.read()

    html_body = markdown.markdown(
        md_content,
        extensions=["tables", "fenced_code"],
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

    HTML(string=full_html).write_pdf(PDF_PATH)
    print(f"PDF gerado com sucesso: {PDF_PATH}")


if __name__ == "__main__":
    main()
