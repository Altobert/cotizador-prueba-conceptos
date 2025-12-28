# Organizador de Cotizaciones por Cliente

Sistema en Java que organiza automáticamente cotizaciones de diferentes brokers agrupándolas por cliente/vessel usando Apache POI.

## Características

- ✅ **Detecta automáticamente** el formato de cada broker (MCTC, Oceanic, CMA CGM, Garrets, Procureship, etc.)
- ✅ **Extrae información clave**: Vessel name, IMO number, número de cotización
- ✅ **Organiza por cliente**: Agrupa archivos usando IMO o nombre del vessel
- ✅ **Genera metadata**: Crea archivo de información por cada cliente
- ✅ **Mantiene archivos originales**: Solo copia, no mueve
- ✅ **Detector de columnas**: Identifica automáticamente la estructura de columnas de cada broker
- ✅ **Mapeo de campos**: Mapea columnas a campos estándar (ITEM_NAME, QUANTITY, PRICE, etc.)

## Requisitos

- Java 7 o superior
- Maven 3.x

## Brokers Soportados

El sistema reconoce automáticamente los formatos de:

1. **MCTC MARINE LTD**
   - Extrae: Vessel Name, IMO Number, Quotation Number
   
2. **OCEANIC CATERING LTD**
   - Extrae: Vessel Name, Quotation Request Number
   
3. **CMA CGM**
   - Extrae: Vessel Name
   
4. **GARRETS INTERNATIONAL LTD**
   - Extrae: RFQ Number, Vessel Name
   
5. **PROCURESHIP**
   - Extrae: Vessel Name, IMO Number, Requisition Number

6. **Formato Genérico**
   - Búsqueda automática de campos comunes

## Instalación

```bash
cd cotizacion-organizer
mvn clean package
```

## Uso

### Compilar el proyecto

```bash
mvn clean compile
```

### Ejecutar

```bash
mvn exec:java -Dexec.mainClass="cl.vsschile.CotizacionOrganizer" \
  -Dexec.args="/ruta/completa/a/BROKERS"
```

### Ejemplo con tu estructura actual

```bash
cd /Users/albertosanmartin/Documents/VSS-COTIZACIONES/cotizacion-organizer

mvn clean compile

mvn exec:java -Dexec.mainClass="cl.vsschile.CotizacionOrganizer" \
  -Dexec.args="/Users/albertosanmartin/Documents/VSS-COTIZACIONES/cotizaciones-por-cliente/OneDrive_1_20-12-2025/BROKERS"
```

### Probar detector de columnas

Para analizar y ver qué columnas detecta el sistema en cada formato de broker:

```bash
mvn exec:java -Dexec.mainClass="cl.vsschile.ColumnDetectorTest" \
  -Dexec.args="/Users/albertosanmartin/Documents/VSS-COTIZACIONES/cotizaciones-por-cliente/OneDrive_1_20-12-2025/BROKERS"
```

Esto mostrará un reporte detallado de:
- Fila de headers detectada
- Mapeo de cada columna a campos estándar
- Ejemplos de datos extraídos

## Estructura de Salida

```
cotizaciones-por-cliente/
├── IMO_9388895/
│   ├── MCTC MARINE LTD_QTN_LOU_233.xlsx
│   ├── MCTC MARINE LTD_QTN_LOU_234.xlsx
│   └── metadata.txt
├── SEAWAYS_ATHENS/
│   ├── OCEANIC CATERING LTD_OAS1210RO008910122025150143766.xlsx
│   └── metadata.txt
└── IMO_9456789/
    ├── PROCURESHIP_ATRA-ST-25-104_3.xlsx
    └── metadata.txt
```

## Archivo metadata.txt

Cada carpeta de cliente contiene un archivo `metadata.txt` con información de todas las cotizaciones:

```
=====================================
Archivo: QTN_LOU_233.xlsx
Broker: MCTC MARINE LTD
Vessel: Louise Auerbach
IMO: 9388895
Cotización: QTN/LOU/233
Fecha procesado: Fri Dec 20 15:46:00 CLT 2025

=====================================
Archivo: QTN_LOU_234.xlsx
Broker: MCTC MARINE LTD
Vessel: Louise Auerbach
IMO: 9388895
Cotización: QTN/LOU/234
Fecha procesado: Fri Dec 20 15:46:01 CLT 2025
```

## Lógica de Agrupación

El sistema prioriza la identificación del cliente en este orden:

1. **IMO Number** (más confiable) → `IMO_9388895`
2. **Vessel Name** → `SEAWAYS_ATHENS`
3. **Quotation Number** → `QUOTE_QTN_LOU_233`
4. **Unknown** → `UNKNOWN_<timestamp>`

## Detección de Columnas

El sistema detecta automáticamente las columnas de cada formato y las mapea a campos estándar:

### Campos Estándar

- **ITEM_NAME**: Descripción del producto
- **ITEM_CODE**: Código/Part Number del ítem
- **CATEGORY**: Categoría del producto
- **QUANTITY**: Cantidad solicitada/ofrecida
- **UOM**: Unidad de medida (KG, LTR, PC, etc.)
- **UNIT_PRICE**: Precio unitario
- **TOTAL**: Precio total de la línea
- **BRAND**: Marca del producto
- **DISCOUNT**: Descuento aplicado
- **SUPPLIER_COMMENTS**: Comentarios del proveedor

### Mapeo por Broker

**MCTC MARINE LTD** (Fila header: 10)
- ITEM_CODE → Columna B (MCTC'S REF NO)
- CATEGORY → Columna C (FOOD CATEGORIES)
- ITEM_NAME → Columna D (ITEM)
- DESCRIPTION → Columna E (ITEM DESCRIPTION)
- UOM → Columna F (UNIT OF MEASURE)
- QUANTITY → Columna G (QUANTITY ORDER)
- UNIT_PRICE → Columna H (PRICE)
- TOTAL → Columna K (TOTAL)

**OCEANIC CATERING LTD** (Fila header: 13)
- CATEGORY → Columna A (Category)
- ITEM_NAME → Columna B (Item - Description)
- OCL_CODE → Columna C (OCL Code)
- VSC_CODE → Columna D (VSC Code)
- QUANTITY → Columna F (Requested Quantity)
- UOM → Columna G (OCL Uom)
- UNIT_PRICE → Columna O (Unit Cost)

**CMA CGM** (Fila header: 19)
- LINE_NO → Columna A (No)
- ITEM_CODE → Columna C (Item Code)
- ITEM_NAME → Columna D (Description)
- BRAND → Columna F (Brand)
- UOM → Columna H (Unit)
- QUANTITY → Columna J (Quantity)
- UNIT_PRICE → Columna L (Unit Price)

**GARRETS INTERNATIONAL LTD** (Fila header: 25)
- LINE_NO → Columna A (No.)
- ITEM_CODE → Columna B (Part#)
- VESSEL → Columna C (Vessel)
- ITEM_NAME → Columna D (Description)
- UOM → Columna J (Unit)
- QUANTITY → Columna K (Quantity)

**PROCURESHIP** (Fila header: 14)
- LINE_NO → Columna B (No.)
- ITEM_NAME → Columna C (Description)
- ITEM_CODE → Columna F (Item Code / Part No.)
- QUANTITY_REQUESTED → Columna I (Quantity Requested)
- UOM → Columna J (UoM)
- UNIT_PRICE → Columna M (Unit Cost)

## Agregar Nuevo Formato de Broker

Para agregar soporte a un nuevo broker, edita `CotizacionOrganizer.java`:

```java
// En extractQuotationInfo(), agregar:
else if (brokerName.contains("NUEVO_BROKER")) {
    extractNuevoBrokerInfo(sheet, info);
}

// Crear método específico:
private void extractNuevoBrokerInfo(Sheet sheet, QuotationInfo info) {
    // Lógica específica del formato
    Row row = sheet.getRow(numeroFila);
    Cell cell = row.getCell(numeroColumna);
    info.vesselName = getCellValueAsString(cell);
    // etc...
}
```

## Personalización

### Cambiar directorio de salida

Edita la constante en `CotizacionOrganizer.java`:

```java
private static final String OUTPUT_DIR = "cotizaciones-por-cliente";
```

### Ajustar rango de búsqueda

En los métodos `extract*Info()`, modifica los límites de las iteraciones:

```java
for (int i = 0; i < 30; i++) { // Cambiar 30 por el rango deseado
```

## Notas

- Los archivos originales **no se modifican ni mueven**
- Solo se copian a las carpetas de destino
- El sistema es seguro para ejecutar múltiples veces
- Compatible con formatos `.xls` y `.xlsx`

## Solución de Problemas

### Error: "No se encontró información de cliente"

- El formato del broker no está reconocido
- Verifica manualmente dónde está la información del vessel/IMO en el Excel
- Agrega un método específico para ese formato

### Error al leer archivos

- Verifica que los archivos no estén abiertos en Excel
- Confirma que los permisos de lectura sean correctos

## Licencia

Uso interno para VSS (Valparaiso Ship Services)
# cotizador-prueba-conceptos
