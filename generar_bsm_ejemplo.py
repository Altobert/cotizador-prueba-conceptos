#!/usr/bin/env python3
"""
Genera un archivo Excel de ejemplo basado en el formato de BSM CATERING
"""
from openpyxl import Workbook
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from datetime import datetime

# Crear workbook
wb = Workbook()
ws = wb.active
ws.title = "RFQ"

# Configurar anchos de columna
column_widths = {
    'A': 8, 'B': 4, 'C': 15, 'D': 40, 'E': 20, 'F': 15,
    'G': 10, 'H': 10, 'I': 12, 'J': 12, 'K': 12,
    'L': 15, 'M': 12, 'N': 12, 'O': 15, 'P': 12,
    'Q': 8, 'R': 8, 'S': 20, 'T': 4, 'U': 20, 'V': 15
}
for col, width in column_widths.items():
    ws.column_dimensions[col].width = width

# Estilos
header_fill = PatternFill(start_color="9DBEC3", end_color="9DBEC3", fill_type="solid")
header_font = Font(name='Calibri', size=11, bold=True, color="1F497D")
precio_vss_fill = PatternFill(start_color="FFFF00", end_color="FFFF00", fill_type="solid")
precio_vss_font = Font(name='Calibri', size=11, bold=False, color="000000")
border = Border(
    left=Side(style='thin'),
    right=Side(style='thin'),
    top=Side(style='thin'),
    bottom=Side(style='thin')
)

# ============================================================================
# COMPANY DETAILS (filas 1-11)
# ============================================================================

# Fila 1: Logo placeholder
ws['A1'] = 'LOGO'
ws['A1'].font = Font(name='Calibri', size=14, bold=True)

# Filas 4-7: Company Name (merged cells)
ws.merge_cells('A4:A7')
ws['A4'] = """BERNHARD SCHULTE SHIPMANAGEMENT (SINGAPORE) PTE. LTD.
BSM - CATERING SERVICES,
BERNHARD SCHULTE SHIPMANAGEMENT (L) LTD.,
C\O Bernhard Schulte Shipmanagement (India) Pvt. Limited"""
ws['A4'].font = Font(name='Calibri', size=10, bold=True)
ws['A4'].alignment = Alignment(wrap_text=True, vertical='top')

# Fila 8: Address
ws.merge_cells('A8:C8')
ws['A8'] = '401 Olympia, Hiranandani Gardens, Powai, Mumbai 400 076, India'
ws['A8'].font = Font(name='Calibri', size=9)

# Fila 9: Contact
ws.merge_cells('A9:C9')
ws['A9'] = 'Tel: +91-22-40017300        Fax: +91-22-40017555'
ws['A9'].font = Font(name='Calibri', size=9)

# Fila 10: Email
ws['A10'] = 'Email:'
ws['C10'] = 'seachef@seachef.com'
ws['A10'].font = Font(name='Calibri', size=9, bold=True)
ws['C10'].font = Font(name='Calibri', size=9, color="0000FF", underline='single')

# Fila 11: Web
ws['A11'] = 'Web:'
ws['C11'] = 'www.seachef.com /  www.bs-shipmanagement.com'
ws['A11'].font = Font(name='Calibri', size=9, bold=True)
ws['C11'].font = Font(name='Calibri', size=9, color="0000FF", underline='single')

# ============================================================================
# RFQ INFORMATION (filas 4-17 en columnas M-O)
# ============================================================================

# Fila 4: Vessel
ws['M4'] = 'Vessel:'
ws['M4'].font = Font(name='Calibri', size=10, bold=True)
ws['O4'] = 'MSC Lome V'
ws['O4'].font = Font(name='Calibri', size=11)

# Fila 5: RFQ Number
ws['M5'] = 'RFQ Number:'
ws['M5'].font = Font(name='Calibri', size=10, bold=True)
ws['O5'] = 'EQ/SCF/CISA/0152'
ws['O5'].font = Font(name='Calibri', size=11)

# Fila 6: RFQ Date
ws['M6'] = 'RFQ Date:'
ws['M6'].font = Font(name='Calibri', size=10, bold=True)
ws['O6'] = datetime(2025, 10, 24)
ws['O6'].number_format = 'DD/MM/YYYY'

# Fila 7: Submit Quote Before
ws['M7'] = 'Submit Quote Before:'
ws['M7'].font = Font(name='Calibri', size=10, bold=True)
ws['O7'] = datetime(2025, 10, 24)
ws['O7'].number_format = 'DD/MM/YYYY'

# Fila 8: Port of Delivery
ws['K8'] = 'Port of Delivery:'
ws['K8'].font = Font(name='Calibri', size=10, bold=True)
ws['M8'] = 'VALPARAISO, Chile'
ws['M8'].font = Font(name='Calibri', size=10)

# Fila 9: Vessel ETA
ws['M9'] = 'Vessel ETA:'
ws['M9'].font = Font(name='Calibri', size=10, bold=True)
ws['O9'] = datetime(2025, 10, 31, 12, 0)
ws['O9'].number_format = 'DD/MM/YYYY HH:MM'

# Fila 10: Payment Terms
ws['M10'] = 'Payment Terms:'
ws['M10'].font = Font(name='Calibri', size=10, bold=True)
ws['O10'] = 'Credit'
ws['O10'].font = Font(name='Calibri', size=10)

# Fila 10: Payment Days
ws['Q10'] = 'Days:'
ws['Q10'].font = Font(name='Calibri', size=10, bold=True)
ws['R10'] = 7
ws['R10'].font = Font(name='Calibri', size=10)

# Fila 11: Vendor Reference
ws['K11'] = 'Vendor Reference:'
ws['K11'].font = Font(name='Calibri', size=10, bold=True)

# Fila 12: Delivery Term
ws['M12'] = 'Delivery Term:'
ws['M12'].font = Font(name='Calibri', size=10, bold=True)
ws['O12'] = 'Free On Board'
ws['O12'].font = Font(name='Calibri', size=10)

# Fila 13: Currency
ws['M13'] = 'Currency:'
ws['M13'].font = Font(name='Calibri', size=10, bold=True)
ws['O13'] = 'USD'
ws['O13'].font = Font(name='Calibri', size=10)

# Fila 14: Discount Percentage
ws['K14'] = 'Discount (%) for all items:'
ws['K14'].font = Font(name='Calibri', size=10, bold=True)

# Fila 15: VAT Percentage
ws['K15'] = 'VAT (%) for all items:'
ws['K15'].font = Font(name='Calibri', size=10, bold=True)

# Fila 16: Place
ws['K16'] = 'Place (CITY):'
ws['K16'].font = Font(name='Calibri', size=10, bold=True)

# ============================================================================
# VENDOR DETAILS (filas 13-17 en columnas C-D)
# ============================================================================

# Fila 13: Vendor Name
ws['B13'] = 'Vendor Name:'
ws['B13'].font = Font(name='Calibri', size=9, bold=True)
ws['D13'] = 'Valparaiso Ship Services S.A.'
ws['D13'].font = Font(name='Calibri', size=10)

# Fila 14: Vendor Address
ws['B14'] = 'Vendor Address:'
ws['B14'].font = Font(name='Calibri', size=9, bold=True)
ws['D14'] = 'Calle Tercera N°820 , Placilla, Valparaiso'
ws['D14'].font = Font(name='Calibri', size=9)

# Fila 15: Vendor Phone
ws['B15'] = 'Vendor Phone:'
ws['B15'].font = Font(name='Calibri', size=9, bold=True)
ws['D15'] = '56 32 2298972'
ws['D15'].font = Font(name='Calibri', size=9)

# Fila 16: Vendor Email
ws['B16'] = 'Vendor Email:'
ws['B16'].font = Font(name='Calibri', size=9, bold=True)
ws['D16'] = 'vss@vsschile.cl'
ws['D16'].font = Font(name='Calibri', size=9, color="0000FF", underline='single')

# ============================================================================
# HEADER ROW (fila 19 - índice 18 en 0-based)
# ============================================================================

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
    ('L19', 'Unit Price ( USD )'),
    ('N19', 'Discount %'),
    ('O19', 'VAT %'),
    ('P19', 'Total Price'),
    ('Q19', 'MD'),
    ('R19', 'sDoC'),
    ('S19', 'Vendor Remarks'),
    ('U19', 'Office Remarks'),
    ('V19', 'PRECIO VSS')
]

for cell_ref, header_text in headers:
    cell = ws[cell_ref]
    cell.value = header_text
    
    # Aplicar estilo según la columna
    if cell_ref == 'V19':  # PRECIO VSS
        cell.fill = precio_vss_fill
        cell.font = precio_vss_font
    else:
        cell.fill = header_fill
        cell.font = header_font
    
    cell.alignment = Alignment(horizontal='center', vertical='center', wrap_text=True)
    cell.border = border

# Ajustar altura de fila del header
ws.row_dimensions[19].height = 30

# ============================================================================
# DATA ROWS (filas 20-25 con datos de ejemplo)
# ============================================================================

sample_data = [
    {
        'no': 1,
        'product_code': 'MISC001',
        'description': 'BEEF STRIPLOIN - EACH 5,5-6,5KG, BONELESS',
        'part_number': 'BF-STR-001',
        'brand': 'PREMIUM',
        'weight': '6.0',
        'unit': 'KG',
        'package': 'BOX',
        'contract_price': '',
        'quantity': 50,
        'unit_price': 18.50,
        'discount': 0,
        'vat': 0,
        'total': 925.00,
        'md': '',
        'sdoc': '',
        'vendor_remarks': 'Fresh, Grade A',
        'office_remarks': '',
        'precio_vss': 21.00
    },
    {
        'no': 2,
        'product_code': 'MISC002',
        'description': 'CHICKEN BREAST BONELESS SKINLESS - FROZEN',
        'part_number': 'CH-BRS-002',
        'brand': 'STANDARD',
        'weight': '1.0',
        'unit': 'KG',
        'package': 'BAG',
        'contract_price': '',
        'quantity': 100,
        'unit_price': 6.50,
        'discount': 5,
        'vat': 0,
        'total': 617.50,
        'md': '',
        'sdoc': '',
        'vendor_remarks': 'Frozen',
        'office_remarks': '',
        'precio_vss': 7.50
    },
    {
        'no': 3,
        'product_code': 'PROV003',
        'description': 'ONION RED - FRESH',
        'part_number': 'VEG-ONI-003',
        'brand': '',
        'weight': '0.5',
        'unit': 'KG',
        'package': 'BAG',
        'contract_price': '',
        'quantity': 30,
        'unit_price': 1.80,
        'discount': 0,
        'vat': 0,
        'total': 54.00,
        'md': '',
        'sdoc': '',
        'vendor_remarks': '',
        'office_remarks': '',
        'precio_vss': 2.10
    }
]

start_row = 20
for idx, item in enumerate(sample_data):
    row = start_row + idx
    
    # Datos
    ws[f'A{row}'] = item['no']
    ws[f'C{row}'] = item['product_code']
    ws[f'D{row}'] = item['description']
    ws[f'E{row}'] = item['part_number']
    ws[f'F{row}'] = item['brand']
    ws[f'G{row}'] = item['weight']
    ws[f'H{row}'] = item['unit']
    ws[f'I{row}'] = item['package']
    ws[f'J{row}'] = item['contract_price']
    ws[f'K{row}'] = item['quantity']
    ws[f'L{row}'] = item['unit_price']
    ws[f'N{row}'] = item['discount']
    ws[f'O{row}'] = item['vat']
    ws[f'P{row}'] = item['total']
    ws[f'Q{row}'] = item['md']
    ws[f'R{row}'] = item['sdoc']
    ws[f'S{row}'] = item['vendor_remarks']
    ws[f'U{row}'] = item['office_remarks']
    ws[f'V{row}'] = item['precio_vss']
    
    # Estilos para datos
    for col in ['A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'N', 'O', 'P', 'Q', 'R', 'S', 'U', 'V']:
        cell = ws[f'{col}{row}']
        cell.border = border
        cell.font = Font(name='Calibri', size=10)
        
        # Alineación numérica para columnas de números
        if col in ['K', 'L', 'N', 'O', 'P', 'V']:
            cell.alignment = Alignment(horizontal='right')
            if col in ['L', 'P', 'V']:
                cell.number_format = '#,##0.00'
        else:
            cell.alignment = Alignment(horizontal='left', vertical='center')
    
    # Color para PRECIO VSS
    ws[f'V{row}'].fill = precio_vss_fill

# ============================================================================
# TOTALS ROW (opcional)
# ============================================================================
total_row = start_row + len(sample_data)
ws[f'J{total_row}'] = 'TOTAL:'
ws[f'J{total_row}'].font = Font(name='Calibri', size=11, bold=True)
ws[f'P{total_row}'] = sum(item['total'] for item in sample_data)
ws[f'P{total_row}'].font = Font(name='Calibri', size=11, bold=True)
ws[f'P{total_row}'].number_format = '#,##0.00'

# Guardar archivo
output_file = '/Users/albertosanmartin/Documents/VSS-COTIZACIONES/cotizacion-organizer/BSM_CATERING_EJEMPLO.xlsx'
wb.save(output_file)

print(f"✅ Archivo generado exitosamente: {output_file}")
print(f"")
print(f"Estructura del archivo:")
print(f"- Header row: Fila 19 (índice 18 en 0-based)")
print(f"- Columnas: 19 columnas (A, C-V)")
print(f"- Datos de ejemplo: {len(sample_data)} items")
print(f"- Metadata: Company Details, RFQ Information, Vendor Details")
