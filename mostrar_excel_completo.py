#!/usr/bin/env python3
"""
Muestra TODO el contenido del archivo BSM_CATERING_EJEMPLO.xlsx fila por fila
"""
from openpyxl import load_workbook

archivo = '/Users/albertosanmartin/Documents/VSS-COTIZACIONES/cotizacion-organizer/BSM_CATERING_EJEMPLO.xlsx'
wb = load_workbook(archivo, data_only=True)
ws = wb.active

print("=" * 100)
print("CONTENIDO COMPLETO DEL ARCHIVO BSM_CATERING_EJEMPLO.xlsx")
print("=" * 100)
print()

# Mostrar las primeras 25 filas y todas las columnas con contenido
for row_num in range(1, 26):
    row_content = []
    has_content = False
    
    for col in ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V']:
        cell = ws[f'{col}{row_num}']
        value = cell.value
        
        if value is not None:
            has_content = True
            # Truncar valores largos
            value_str = str(value)
            if len(value_str) > 30:
                value_str = value_str[:30] + "..."
            row_content.append(f"{col}={value_str}")
    
    if has_content:
        print(f"Fila {row_num:2d}: {', '.join(row_content)}")

print()
print("=" * 100)
print("¿El archivo tiene METADATA y COLUMNAS?")
print("=" * 100)

# Verificar metadata
metadata_exists = False
if ws['A1'].value or ws['A4'].value or ws['O4'].value or ws['D13'].value:
    metadata_exists = True
    print("✓ SÍ - Encontré metadata en el archivo")
else:
    print("✗ NO - No encontré metadata")

# Verificar columnas
columns_exist = False
header_cells = ['A19', 'C19', 'D19', 'E19', 'F19']
if any(ws[cell].value for cell in header_cells):
    columns_exist = True
    print("✓ SÍ - Encontré columnas (header row) en fila 19")
else:
    print("✗ NO - No encontré columnas")

# Verificar datos
data_exists = False
if ws['A20'].value or ws['C20'].value:
    data_exists = True
    print("✓ SÍ - Encontré datos de ejemplo")
else:
    print("✗ NO - No encontré datos")

print()
print("RESUMEN:")
print(f"  Metadata: {'✓ INCLUIDA' if metadata_exists else '✗ NO INCLUIDA'}")
print(f"  Columnas: {'✓ INCLUIDAS' if columns_exist else '✗ NO INCLUIDAS'}")
print(f"  Datos:    {'✓ INCLUIDOS' if data_exists else '✗ NO INCLUIDOS'}")
print("=" * 100)

wb.close()
