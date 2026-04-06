#!/usr/bin/env python3
"""Convert the social hooks research markdown to PDF."""

import markdown
from weasyprint import HTML

INPUT = "/home/user/tumtum/docs/research/social-hooks-research.md"
OUTPUT = "/home/user/tumtum/docs/research/social-hooks-research.pdf"

with open(INPUT, "r", encoding="utf-8") as f:
    md_text = f.read()

html_body = markdown.markdown(
    md_text,
    extensions=["tables", "toc", "fenced_code"],
)

full_html = f"""<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<style>
  @page {{
    size: A4;
    margin: 2cm 2.5cm;
    @bottom-center {{
      content: counter(page);
      font-size: 10px;
      color: #6B6B80;
    }}
  }}
  body {{
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
    font-size: 11pt;
    line-height: 1.6;
    color: #1a1a2e;
    max-width: 100%;
  }}
  h1 {{
    color: #C0392B;
    font-size: 22pt;
    border-bottom: 3px solid #C0392B;
    padding-bottom: 8px;
    margin-top: 40px;
    page-break-before: always;
  }}
  h1:first-of-type {{
    page-break-before: avoid;
  }}
  h2 {{
    color: #C0392B;
    font-size: 16pt;
    border-bottom: 1px solid #E74C3C;
    padding-bottom: 4px;
    margin-top: 30px;
    page-break-before: always;
  }}
  h2:first-of-type {{
    page-break-before: avoid;
  }}
  h3 {{
    color: #2c2c54;
    font-size: 13pt;
    margin-top: 20px;
  }}
  table {{
    border-collapse: collapse;
    width: 100%;
    margin: 15px 0;
    font-size: 10pt;
  }}
  th {{
    background-color: #C0392B;
    color: white;
    padding: 8px 12px;
    text-align: left;
    font-weight: 600;
  }}
  td {{
    padding: 6px 12px;
    border-bottom: 1px solid #ddd;
  }}
  tr:nth-child(even) td {{
    background-color: #f9f9f9;
  }}
  a {{
    color: #C0392B;
    text-decoration: none;
  }}
  strong {{
    color: #1a1a2e;
  }}
  code {{
    background: #f4f4f8;
    padding: 2px 5px;
    border-radius: 3px;
    font-size: 10pt;
  }}
  pre {{
    background: #111118;
    color: #F0F0F5;
    padding: 15px;
    border-radius: 6px;
    font-size: 9pt;
    line-height: 1.4;
    overflow-x: auto;
    white-space: pre-wrap;
  }}
  pre code {{
    background: none;
    color: inherit;
    padding: 0;
  }}
  blockquote {{
    border-left: 4px solid #C0392B;
    margin-left: 0;
    padding-left: 16px;
    color: #555;
  }}
  hr {{
    border: none;
    border-top: 2px solid #eee;
    margin: 30px 0;
  }}
</style>
</head>
<body>
{html_body}
</body>
</html>"""

HTML(string=full_html).write_pdf(OUTPUT)
print(f"PDF generated: {OUTPUT}")
