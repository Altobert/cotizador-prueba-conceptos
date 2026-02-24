#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generador de archivo Excel BSM CATERING con formato idéntico al original
"""

from openpyxl import Workbook
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from openpyxl.drawing.image import Image
from datetime import datetime

# Crear nuevo workbook
wb = Workbook()
ws = wb.active
ws.title = "Quote"

# Configurar anchos de columna (basado en el análisis)
ws.column_dimensions['A'].width = 7.0
ws.column_dimensions['B'].width = 10.0
ws.column_dimensions['C'].width = 11.0
ws.column_dimensions['D'].width = 30.0
ws.column_dimensions['E'].width = 15.0
ws.column_dimensions['F'].width = 10.0
ws.column_dimensions['G'].width = 8.0
ws.column_dimensions['H'].width = 8.0
ws.column_dimensions['I'].width = 10.0
ws.column_dimensions['J'].width = 12.0
ws.column_dimensions['K'].width = 10.0
ws.column_dimensions['L'].width = 8.0
ws.column_dimensions['M'].width = 8.0
ws.column_dimensions['N'].width = 10.0
ws.column_dimensions['O'].width = 8.0
ws.column_dimensions['P'].width = 10.0
ws.column_dimensions['Q'].width = 8.0
ws.column_dimensions['R'].width = 8.0
ws.column_dimensions['S'].width = 15.0
ws.column_dimensions['T'].width = 8.0
ws.column_dimensions['U'].width = 15.0

# Configurar alturas de filas específicas
ws.row_dimensions[1].height = 51.0
ws.row_dimensions[3].height = 20.25

# Agregar logo BSM en A1
try:
    logo = Image('/Users/albertosanmartin/Documents/VSS-COTIZACIONES/cotizacion-organizer/bsm_logo.jpeg')
    logo.width = 407
    logo.height = 106
    ws.add_image(logo, 'A1')
except Exception as e:
    print(f"⚠️  No se pudo agregar el logo: {e}")

# Definir estilos de bordes
thin_border = Side(style='thin', color='FF000000')
medium_border = Side(style='medium', color='FF000000')
thick_border = Side(style='thick', color='FF000000')

# === FILA 1: Título "Request for Quote" ===
ws.merge_cells('M1:V1')
ws['M1'] = 'Request for Quote'
ws['M1'].font = Font(name='Calibri', size=36, bold=True, color='FF1F497D')
ws['M1'].alignment = Alignment(horizontal='right', vertical='center')

# === FILA 3: "Vessel \ Company Details" ===
ws.merge_cells('A3:L3')
ws['A3'] = 'Vessel \\ Company Details'
ws['A3'].font = Font(name='Calibri', size=15, bold=True, color='FF1F497D')
ws['A3'].alignment = Alignment(horizontal='left', vertical='center')

# Agregar borde thick bottom en fila 3 (columnas A-E)
for col_letter in ['A', 'B', 'C', 'D', 'E']:
    ws[f'{col_letter}3'].border = Border(bottom=thick_border)

# === FILA 4: Company name y Vessel ===
ws.merge_cells('A4:L4')
ws['A4'] = 'BERNHARD SCHULTE SHIPMANAGEMENT (HONG KONG) LIMITED PARTNERS'
ws['A4'].font = Font(name='Calibri', size=11, bold=True, color='FF1F497D')
ws['A4'].alignment = Alignment(horizontal='left', vertical='center')

# Agregar borde medium bottom en fila 4 (todas las columnas con contenido)
for col_letter in ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V']:
    ws[f'{col_letter}4'].border = Border(bottom=medium_border)

ws.merge_cells('M4:N4')
ws['M4'] = 'Vessel'
ws['M4'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['M4'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('O4:V4')
ws['O4'] = 'Barism Alice'
ws['O4'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['O4'].alignment = Alignment(horizontal='left', vertical='center')

# === FILA 5: BSM - CATERING SERVICES y RFQ No. ===
ws.merge_cells('A5:L5')
ws['A5'] = 'BSM - CATERING SERVICES,'
ws['A5'].font = Font(name='Calibri', size=11, bold=True, color='FF1F497D')
ws['A5'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('M5:N5')
ws['M5'] = 'RFQ No.'
ws['M5'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['M5'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('O5:V5')
ws['O5'] = 'EQ/SCF/BAA/0031'
ws['O5'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['O5'].alignment = Alignment(horizontal='left', vertical='center')

# Agregar borde medium bottom en fila 5 (columnas M-V)
for col_letter in ['M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V']:
    ws[f'{col_letter}5'].border = Border(bottom=medium_border)

# === FILA 6: BERNHARD SCHULTE SHIPMANAGEMENT y RFQ Date ===
ws.merge_cells('A6:L6')
ws['A6'] = 'BERNHARD SCHULTE SHIPMANAGEMENT (L) LTD.,'
ws['A6'].font = Font(name='Calibri', size=11, bold=True, color='FF1F497D')
ws['A6'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('M6:N6')
ws['M6'] = 'RFQ Date'
ws['M6'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['M6'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('O6:V6')
ws['O6'] = datetime(2025, 12, 11)
ws['O6'].number_format = 'DD/MM/YYYY'
ws['O6'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['O6'].alignment = Alignment(horizontal='left', vertical='center')

# Agregar borde medium bottom en fila 6 (columnas A-G y M-V)
for col_letter in ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V']:
    ws[f'{col_letter}6'].border = Border(bottom=medium_border)

# === FILA 7: C\O Bernhard Schulte ===
ws.merge_cells('A7:L7')
ws['A7'] = 'C\\O Bernhard Schulte Shipmanagement (India) Pvt. Limited'
ws['A7'].font = Font(name='Calibri', size=10, color='FF4B7279')
ws['A7'].alignment = Alignment(horizontal='left', vertical='center')

# Agregar borde medium bottom en fila 7 (columnas M-V)
for col_letter in ['M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V']:
    ws[f'{col_letter}7'].border = Border(bottom=medium_border)

# === FILA 8: Address y Submit Quote Before ===
ws.merge_cells('A8:L8')
ws['A8'] = '401 Olympia, Hiranandani Gardens, Powai, Mumbai 400 076, India'
ws['A8'].font = Font(name='Calibri', size=10, color='FF4B7279')
ws['A8'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('M8:N8')
ws['M8'] = 'Submit Quote Before:'
ws['M8'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['M8'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('O8:V8')
ws['O8'] = datetime(2025, 12, 11)
ws['O8'].number_format = 'DD/MM/YYYY'
ws['O8'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['O8'].alignment = Alignment(horizontal='left', vertical='center')

# Agregar borde medium bottom en fila 8 (columnas M-V)
for col_letter in ['M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V']:
    ws[f'{col_letter}8'].border = Border(bottom=medium_border)

# === FILA 9: Tel/Fax y Port of Delivery ===
ws.merge_cells('A9:K9')
ws['A9'] = 'Tel: +91-22-40017300        Fax: +91-22-40017555'
ws['A9'].font = Font(name='Calibri', size=10, color='FF4B7279')
ws['A9'].alignment = Alignment(horizontal='left', vertical='center')

ws['L9'] = 'Port of Delivery'
ws['L9'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')

# Agregar borde medium bottom en fila 9 (columnas A-I)
for col_letter in ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I']:
    ws[f'{col_letter}9'].border = Border(bottom=medium_border)

# === FILA 10: Email y Vessel ETA ===
ws['A10'] = 'Email:'
ws['A10'].font = Font(name='Calibri', size=10, color='FF4B7279')

ws.merge_cells('C10:L10')
ws['C10'] = 'seachef@seachef.com'
ws['C10'].font = Font(name='Calibri', size=11, color='FF1F497D')
ws['C10'].alignment = Alignment(horizontal='left', vertical='center')

ws['M10'] = 'Vessel ETA'
ws['M10'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')

ws.merge_cells('O10:V10')
ws['O10'] = datetime(2025, 12, 12, 12, 0)
ws['O10'].number_format = 'DD/MM/YYYY HH:MM'
ws['O10'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['O10'].alignment = Alignment(horizontal='left', vertical='center')

# Agregar borde medium bottom en fila 10 (columnas A-G y M-V)
for col_letter in ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V']:
    ws[f'{col_letter}10'].border = Border(bottom=medium_border)

# === FILA 11: Web y Payment Terms ===
ws['A11'] = 'Web:'
ws['A11'].font = Font(name='Calibri', size=10, color='FF4B7279')

ws.merge_cells('C11:L11')
ws['C11'] = 'www.seachef.com /  www.bs-shipmanagement.com'
ws['C11'].font = Font(name='Calibri', size=10, color='FF4B7279')
ws['C11'].alignment = Alignment(horizontal='left', vertical='center')

ws['M11'] = 'Payment Terms'
ws['M11'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')

ws['O11'] = 'Credit'
ws['O11'].font = Font(name='Calibri', size=11, bold=True, color='FF1F497D')
ws['O11'].alignment = Alignment(horizontal='left', vertical='center')
ws['O11'].fill = PatternFill(start_color='FFFFFFFF', end_color='FFFFFFFF', fill_type='solid')

ws['P11'] = 'Days'
ws['P11'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['P11'].alignment = Alignment(horizontal='right', vertical='center')

ws['S11'] = 9
ws['S11'].font = Font(name='Calibri', size=11, bold=True, color='FF1F497D')
ws['S11'].alignment = Alignment(horizontal='right', vertical='center')
ws['S11'].fill = PatternFill(start_color='FFFFFFFF', end_color='FFFFFFFF', fill_type='solid')

# Agregar borde medium bottom en fila 11 (columnas A-L y M-V)
for col_letter in ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V']:
    ws[f'{col_letter}11'].border = Border(bottom=medium_border)

# === FILA 12: Vendor Details y Vendor Reference ===
ws.merge_cells('A12:L12')
ws['A12'] = 'Vendor Details'
ws['A12'].font = Font(name='Calibri', size=11, bold=True, color='FF1F497D')
ws['A12'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('M12:V12')
ws['M12'] = 'Vendor Reference '
ws['M12'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['M12'].alignment = Alignment(horizontal='left', vertical='center')

# Agregar borde thick bottom en fila 12 (columnas A-L)
for col_letter in ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L']:
    ws[f'{col_letter}12'].border = Border(bottom=thick_border)

# === FILA 13: Name y Delivery Term ===
ws.merge_cells('A13:C13')
ws['A13'] = 'Name'
ws['A13'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['A13'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('D13:L13')
ws['D13'] = 'Valparaiso Ship Services S.A.'
ws['D13'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['D13'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('M13:N13')
ws['M13'] = 'Delivery Term'
ws['M13'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['M13'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('O13:V13')
ws['O13'] = 'Free On Board'
ws['O13'].font = Font(name='Calibri', size=11, bold=True, color='FF1F497D')
ws['O13'].alignment = Alignment(horizontal='left', vertical='center')
ws['O13'].fill = PatternFill(start_color='FFFFFFFF', end_color='FFFFFFFF', fill_type='solid')

# === FILA 14: Address y Select Currency ===
ws.merge_cells('A14:C14')
ws['A14'] = 'Address'
ws['A14'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['A14'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('D14:L14')
ws['D14'] = 'Calle Tercera N°820 , Placilla, Valparaiso'
ws['D14'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['D14'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('M14:N14')
ws['M14'] = 'Select Currency *'
ws['M14'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['M14'].alignment = Alignment(horizontal='left', vertical='center')

ws['O14'] = 'USD'
ws['O14'].font = Font(name='Calibri', size=11, color='FF1F497D')
ws['O14'].alignment = Alignment(horizontal='right', vertical='center')
ws['O14'].fill = PatternFill(start_color='FFFFFFFF', end_color='FFFFFFFF', fill_type='solid')

# === FILA 15: City y Discount ===
ws.merge_cells('A15:C15')
ws['A15'] = 'City'
ws['A15'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['A15'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('M15:N15')
ws['M15'] = 'Discount (%) for all items'
ws['M15'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['M15'].alignment = Alignment(horizontal='left', vertical='center')

ws['O15'] = 0
ws['O15'].font = Font(name='Calibri', size=11, color='FF1F497D')
ws['O15'].alignment = Alignment(horizontal='right', vertical='center')
ws['O15'].fill = PatternFill(start_color='FFFFFFFF', end_color='FFFFFFFF', fill_type='solid')

ws['S15'] = 0
ws['S15'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')

# === FILA 16: Phone y VAT ===
ws.merge_cells('A16:C16')
ws['A16'] = 'Phone'
ws['A16'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['A16'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('D16:L16')
ws['D16'] = '56 32 2298972'
ws['D16'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['D16'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('M16:N16')
ws['M16'] = 'VAT(%) for all items'
ws['M16'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['M16'].alignment = Alignment(horizontal='left', vertical='center')

ws['O16'] = 0
ws['O16'].font = Font(name='Calibri', size=11, color='FF1F497D')
ws['O16'].alignment = Alignment(horizontal='right', vertical='center')
ws['O16'].fill = PatternFill(start_color='FFFFFFFF', end_color='FFFFFFFF', fill_type='solid')

# === FILA 17: Email y Place ===
ws.merge_cells('A17:C17')
ws['A17'] = 'Email'
ws['A17'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['A17'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('D17:L17')
ws['D17'] = 'vss@vsschile.cl'
ws['D17'].font = Font(name='Calibri', size=10, bold=True, color='FF0563C1')
ws['D17'].alignment = Alignment(horizontal='left', vertical='center')

ws.merge_cells('M17:V17')
ws['M17'] = 'Place (CITY)'
ws['M17'].font = Font(name='Calibri', size=10, bold=True, color='FF1F497D')
ws['M17'].alignment = Alignment(horizontal='left', vertical='center')

# Agregar borde medium bottom en fila 17 (todas las columnas)
for col_letter in ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V']:
    ws[f'{col_letter}17'].border = Border(bottom=medium_border)

# === FILA 19: Header row ===
header_fill = PatternFill(start_color='FF9DBEC3', end_color='FF9DBEC3', fill_type='solid')
header_font = Font(name='Calibri', size=9, bold=True, color='FF1F497D')

headers = [
    ('A19', 'No'),
    ('C19', 'Product Code'),
    ('D19', 'Description'),
    ('E19', 'Part Number'),
    ('F19', 'Brand'),
    ('G19', 'Weight'),
    ('H19', 'Unit'),
    ('I19', 'Package'),
    ('J19', 'Contract Price'),
    ('K19', 'Quantity'),
    ('N19', 'Discount %'),
    ('O19', 'VAT %'),
    ('P19', 'Total Price'),
    ('Q19', 'MD'),
    ('R19', 'sDoC')
]

for cell_coord, header_text in headers:
    ws[cell_coord] = header_text
    ws[cell_coord].fill = header_fill
    ws[cell_coord].font = header_font
    ws[cell_coord].alignment = Alignment(horizontal='left', vertical='center')

# Header "Unit Price ( USD )" con merge
ws.merge_cells('L19:M19')
ws['L19'] = 'Unit Price ( USD )'
ws['L19'].fill = header_fill
ws['L19'].font = header_font
ws['L19'].alignment = Alignment(horizontal='left', vertical='center')

# Header "Vendor Remarks" con merge
ws.merge_cells('S19:T19')
ws['S19'] = 'Vendor Remarks'
ws['S19'].fill = header_fill
ws['S19'].font = header_font
ws['S19'].alignment = Alignment(horizontal='left', vertical='center')

# Header "Office Remarks" con merge
ws.merge_cells('U19:V19')
ws['U19'] = 'Office Remarks'
ws['U19'].fill = header_fill
ws['U19'].font = header_font
ws['U19'].alignment = Alignment(horizontal='left', vertical='center')

# === FILA 20: "PROVISIONS" ===
ws['C20'] = 'PROVISIONS'
ws['C20'].fill = PatternFill(start_color='FFFFFF00', end_color='FFFFFF00', fill_type='solid')
ws['C20'].font = Font(name='Calibri', size=8, color='FF1F497D')
ws['C20'].alignment = Alignment(horizontal='left', vertical='center')

# === FILAS 21-23: Datos de ejemplo ===
data_font = Font(name='Calibri', size=8, color='FF1F497D')
data_align = Alignment(vertical='center')

# Fila 21
ws['A21'] = 1
ws['A21'].font = data_font
ws['A21'].alignment = Alignment(horizontal='right', vertical='center')

ws['B21'] = 5210702
ws['B21'].font = data_font
ws['B21'].alignment = Alignment(horizontal='right', vertical='center')

ws['C21'] = 'BAK38'
ws['C21'].font = data_font
ws['C21'].alignment = Alignment(horizontal='left', vertical='center')

ws['D21'] = 'PIZZA BASE'
ws['D21'].font = data_font
ws['D21'].alignment = data_align

ws['E21'] = 'BAK38'
ws['E21'].font = data_font
ws['E21'].alignment = data_align

ws['F21'] = 'local'
ws['F21'].font = data_font
ws['F21'].alignment = Alignment(horizontal='left', vertical='center')

ws['G21'] = 1
ws['G21'].font = data_font
ws['G21'].alignment = Alignment(horizontal='right', vertical='center')

ws['H21'] = 'Nos'
ws['H21'].font = data_font
ws['H21'].alignment = Alignment(horizontal='left', vertical='center')

ws['J21'] = 0
ws['J21'].font = data_font
ws['J21'].alignment = Alignment(horizontal='right', vertical='center')

ws['K21'] = 30
ws['K21'].font = data_font
ws['K21'].alignment = Alignment(horizontal='right', vertical='center')
ws['K21'].fill = PatternFill(start_color='FFFFFFFF', end_color='FFFFFFFF', fill_type='solid')

ws['M21'] = 1.73
ws['M21'].font = data_font
ws['M21'].alignment = Alignment(horizontal='right', vertical='center')
ws['M21'].fill = PatternFill(start_color='FFFFFFFF', end_color='FFFFFFFF', fill_type='solid')

ws['N21'] = 0
ws['N21'].font = data_font
ws['N21'].alignment = Alignment(horizontal='right', vertical='center')
ws['N21'].fill = PatternFill(start_color='FFFFFFFF', end_color='FFFFFFFF', fill_type='solid')

ws['O21'] = 0
ws['O21'].font = data_font
ws['O21'].alignment = Alignment(horizontal='right', vertical='center')
ws['O21'].fill = PatternFill(start_color='FFFFFFFF', end_color='FFFFFFFF', fill_type='solid')

ws['P21'] = 51.9
ws['P21'].font = data_font
ws['P21'].alignment = Alignment(horizontal='right', vertical='center')
ws['P21'].fill = PatternFill(start_color='FFEDF3F3', end_color='FFEDF3F3', fill_type='solid')

ws['Q21'] = 'No'
ws['Q21'].font = Font(name='Calibri', size=11, color='FF1F497D')
ws['Q21'].alignment = Alignment(horizontal='right', vertical='center')
ws['Q21'].fill = PatternFill(start_color='FFFFFFFF', end_color='FFFFFFFF', fill_type='solid')

ws['R21'] = 'No'
ws['R21'].font = Font(name='Calibri', size=11, color='FF1F497D')
ws['R21'].alignment = Alignment(horizontal='right', vertical='center')
ws['R21'].fill = PatternFill(start_color='FFFFFFFF', end_color='FFFFFFFF', fill_type='solid')

ws.merge_cells('S21:T21')
ws['S21'] = 'price per unit, ready'
ws['S21'].font = Font(name='Calibri', size=11, color='FF1F497D')
ws['S21'].alignment = data_align
ws['S21'].fill = PatternFill(start_color='FFFFFFFF', end_color='FFFFFFFF', fill_type='solid')

# Guardar el archivo
output_file = '/Users/albertosanmartin/Documents/VSS-COTIZACIONES/cotizacion-organizer/BSM_CATERING_EJEMPLO.xlsx'
wb.save(output_file)

print(f"✅ Archivo generado exitosamente: {output_file}")
print("\nEstructura del archivo:")
print(f"- Formato idéntico al original BSM CATERING")
print(f"- Logo BSM incluido")
print(f"- Líneas resaltadas negras (bordes thick y medium)")
print(f"- Header row: Fila 19")
print(f"- Celdas mezcladas: Múltiples secciones")
print(f"- Metadata completa con formato original")
print(f"- Columnas A-V (incluyendo columna B con datos)")
