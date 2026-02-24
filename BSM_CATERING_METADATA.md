# Metadata de BSM CATERING - Archivo Generado

## Archivo Excel
**Ubicación:** `BSM_CATERING_EJEMPLO.xlsx`

## Metadata Incluida en el Archivo

### 📋 COMPANY DETAILS (Filas 1-11, Columna A)

| Campo | Valor | Ubicación |
|-------|-------|-----------|
| Logo | "LOGO" | Fila 1, Columna A |
| **Company Name (Líneas 1-4)** | | Filas 4-7, Columna A (merged) |
| - Línea 1 | BERNHARD SCHULTE SHIPMANAGEMENT (SINGAPORE) PTE. LTD. | Fila 4 |
| - Línea 2 | BSM - CATERING SERVICES, | Fila 5 |
| - Línea 3 | BERNHARD SCHULTE SHIPMANAGEMENT (L) LTD., | Fila 6 |
| - Línea 4 | C\O Bernhard Schulte Shipmanagement (India) Pvt. Limited | Fila 7 |
| **Company Address** | 401 Olympia, Hiranandani Gardens, Powai, Mumbai 400 076, India | Fila 8, Columnas A-C (merged) |
| **Company Contact** | Tel: +91-22-40017300        Fax: +91-22-40017555 | Fila 9, Columnas A-C (merged) |
| **Company Email** | | Fila 10 |
| - Label | Email: | Columna A |
| - Value | seachef@seachef.com | Columna C |
| **Company Web** | | Fila 11 |
| - Label | Web: | Columna A |
| - Value | www.seachef.com /  www.bs-shipmanagement.com | Columna C |

---

### 📝 RFQ INFORMATION (Filas 4-17, Columnas L-O, S)

| Campo | Valor | Ubicación |
|-------|-------|-----------|
| **Vessel** | MSC Lome V | Fila 4, Columna O |
| **RFQ Number** | EQ/SCF/CISA/0152 | Fila 5, Columna O |
| **RFQ Date** | 24/10/2025 | Fila 6, Columna O |
| **Submit Quote Before** | 24/10/2025 | Fila 8, Columna O |
| **Port of Delivery** | Port of Delivery | Fila 9, Columna L |
| **Vessel ETA** | 31/10/2025 12:00 | Fila 10, Columna O |
| **Payment Terms** | Credit | Fila 11, Columna O |
| **Payment Days** | 7 | Fila 11, Columna S |
| **Vendor Reference** | Vendor Reference | Fila 12, Columna M |
| **Delivery Term** | Free On Board | Fila 13, Columna O |
| **Currency** | USD | Fila 14, Columna O |
| **Discount Percentage** | Discount (%) for all items | Fila 15, Columna M |
| **VAT Percentage** | VAT(%) for all items | Fila 16, Columna M |
| **Place (City)** | Place (CITY) | Fila 17, Columna M |

---

### 🏢 VENDOR DETAILS (Filas 13-17, Columna D)

| Campo | Valor | Ubicación |
|-------|-------|-----------|
| **Vendor Name** | Valparaiso Ship Services S.A. | Fila 13, Columna D |
| **Vendor Address** | Calle Tercera N°820 , Placilla, Valparaiso | Fila 14, Columna D |
| **Vendor Phone** | 56 32 2298972 | Fila 16, Columna D |
| **Vendor Email** | vss@vsschile.cl | Fila 17, Columna D |

---

## 📊 ESTRUCTURA DE DATOS

### Header Row (Fila 19)
**Total de columnas:** 19 columnas

| # | Columna | Campo Estándar | Nombre en Excel | Color Fondo | Negrita |
|---|---------|----------------|-----------------|-------------|---------|
| 1 | A | LINE_NO | No | #9DBEC3 | ✓ |
| 2 | C | PRODUCT_CODE | Product Code | #9DBEC3 | ✓ |
| 3 | D | ITEM_NAME | Description | #9DBEC3 | ✓ |
| 4 | E | ITEM_CODE | Part Number | #9DBEC3 | ✓ |
| 5 | F | BRAND | Brand | #9DBEC3 | ✓ |
| 6 | G | WEIGHT | Weight | #9DBEC3 | ✓ |
| 7 | H | UOM | Unit | #9DBEC3 | ✓ |
| 8 | I | PACKAGE | Package | #9DBEC3 | ✓ |
| 9 | J | CONTRACT_PRICE | Contract Price | #9DBEC3 | ✓ |
| 10 | K | QUANTITY | Quantity | #9DBEC3 | ✓ |
| 11 | L | UNIT_PRICE | Unit Price ( USD ) | #9DBEC3 | ✓ |
| 12 | N | DISCOUNT | Discount % | #9DBEC3 | ✓ |
| 13 | O | VAT | VAT % | #9DBEC3 | ✓ |
| 14 | P | TOTAL | Total Price | #9DBEC3 | ✓ |
| 15 | Q | MD | MD | #9DBEC3 | ✓ |
| 16 | R | SDOC | sDoC | #9DBEC3 | ✓ |
| 17 | S | VENDOR_REMARKS | Vendor Remarks | #9DBEC3 | ✓ |
| 18 | U | OFFICE_REMARKS | Office Remarks | #9DBEC3 | ✓ |
| 19 | V | PRECIO_VSS | PRECIO VSS | #FFFF00 | ✗ |

---

## 📦 DATOS DE EJEMPLO (Filas 20-22)

| No | Product Code | Description | Quantity | Unit Price | Total | Precio VSS |
|----|-------------|-------------|----------|------------|-------|------------|
| 1 | MISC001 | BEEF STRIPLOIN - EACH 5,5-6,5KG, BONELESS | 50 | $18.50 | $925.00 | $21.00 |
| 2 | MISC002 | CHICKEN BREAST BONELESS SKINLESS - FROZEN | 100 | $6.50 | $617.50 | $7.50 |
| 3 | PROV003 | ONION RED - FRESH | 30 | $1.80 | $54.00 | $2.10 |

**TOTAL:** $1,596.50

---

## ✅ Verificación de Metadata

### Metadata en Base de Datos
```sql
SELECT COUNT(*) FROM broker_metadata 
WHERE formato_id = (
    SELECT formato_id FROM broker_formatos 
    WHERE broker_id = (SELECT broker_id FROM brokers WHERE broker_name = 'BSM CATERING')
);
```
**Resultado:** 28 registros

### Metadata en Archivo Excel
- ✅ **Company Details:** 10 campos
- ✅ **RFQ Information:** 14 campos  
- ✅ **Vendor Details:** 4 campos

**Total:** 28 campos incluidos en el archivo

---

## 🎨 Estilos Aplicados

### Colores de Encabezado
- **Fondo:** #9DBEC3 (azul grisáceo)
- **Texto:** #1F497D (azul oscuro)
- **Negrita:** Sí

### Columna PRECIO_VSS
- **Fondo:** #FFFF00 (amarillo)
- **Texto:** #000000 (negro)
- **Negrita:** No

### Bordes
- Todas las celdas de datos tienen bordes finos

---

## 📍 Ubicaciones Clave

| Elemento | Ubicación |
|----------|-----------|
| Company Name | A4:A7 (merged) |
| RFQ Number | O5 |
| Vessel | O4 |
| Currency | O14 |
| Header Row | Fila 19 |
| First Data Row | Fila 20 |
| Vendor Name | D13 |
| Total Row | Fila 23 |

---

## 🔄 Regenerar el Archivo

Para regenerar el archivo con metadata actualizada:

```bash
python3 generar_bsm_ejemplo.py
```

El script consulta automáticamente la base de datos `sistema_cotizacion_2025` y obtiene toda la metadata registrada para BSM CATERING.

---

## 📝 Notas

1. **Todas las fechas** están en formato DD/MM/YYYY
2. **Los precios** están formateados como currency (#,##0.00)
3. **La metadata** corresponde exactamente a los 28 registros en la base de datos
4. **Header row** está en la fila 19 (índice 18 en 0-based) según la configuración de la BD
5. **Columnas no consecutivas:** El formato BSM tiene columnas en A, C-V (la columna B no se usa)

---

## 🔗 Referencias

- **Base de datos:** sistema_cotizacion_2025
- **Broker ID:** 6
- **Formato ID:** 7
- **Header Row:** 18 (0-based) / 19 (1-based)
- **Total Columnas:** 19
- **Total Metadata:** 28 registros
