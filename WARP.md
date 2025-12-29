# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

Sistema de organización y visualización de cotizaciones para VSS (Valparaiso Ship Services). Procesa archivos Excel de múltiples brokers (MCTC, Oceanic, CMA CGM, Garrets, Procureship, BSM), extrae información de vessels/clientes, organiza cotizaciones por cliente y visualiza datos con formatos originales preservados.

## Build & Compilation

```bash
# Compile the project
mvn clean compile

# Build package
mvn clean package
```

## Running Applications

### Main Applications

1. **Cotization Organizer** (organize quotations by client)
```bash
mvn exec:java -Dexec.mainClass="cl.vsschile.CotizacionOrganizer" \
  -Dexec.args="<path-to-BROKERS-directory>"
```

2. **Quotation Viewer** (JavaFX GUI to visualize quotations)
```bash
mvn exec:java -Dexec.mainClass="cl.vsschile.CotizacionViewerApp"
# Or use the script:
./run-viewer.sh
```

3. **Column Detector Test** (analyze and test column detection)
```bash
mvn exec:java -Dexec.mainClass="cl.vsschile.ColumnDetectorTest" \
  -Dexec.args="<path-to-BROKERS-directory>"
```

4. **Format Saver** (save broker formats to database)
```bash
mvn exec:java -Dexec.mainClass="cl.vsschile.FormatoSaver" \
  -Dexec.args="<path-to-BROKERS-directory> [db-password]"
```

5. **Archive Color Saver** (save individual file color information)
```bash
mvn exec:java -Dexec.mainClass="cl.vsschile.ArchivoColorSaver" \
  -Dexec.args="<path-to-BROKERS-directory>"
```

6. **Template Recreator** (recreate broker Excel templates)
```bash
mvn exec:java -Dexec.mainClass="cl.vsschile.TemplateRecreator"
```

7. **Excel Report Generator** (generate broker analysis reports)
```bash
mvn exec:java -Dexec.mainClass="cl.vsschile.ExcelReportGenerator"
```

8. **Broker Excel Generator** (generate sample broker files)
```bash
mvn exec:java -Dexec.mainClass="cl.vsschile.BrokerExcelGenerator"
```

## Architecture

### Core Components

**1. CotizacionOrganizer**
- Main entry point for processing quotations
- Scans broker directories, extracts metadata (vessel name, IMO, quotation number)
- Organizes files by client into `cotizaciones-por-cliente/` directory
- Generates `metadata.txt` for each client folder

**2. ColumnDetector**
- Detects header rows and column mappings for each broker format
- Returns `ColumnMapping` objects containing:
  - `headerRow`: Row index where headers are located
  - `columns`: Map of standard field names to column indices
  - `columnStyles`: Cell style information (colors, fonts, borders)
- Broker-specific detection methods for each supported format

**3. FormatoDatabaseManager**
- PostgreSQL interface for storing/retrieving broker formats
- Database: `sistema_cotizacion_2025` (localhost:5432, user: postgres)
- Tables: `brokers`, `broker_formatos`, `formato_columnas`, `cotizaciones_archivos`, `archivo_colores`
- Views: `v_formatos_activos`, `v_columnas_detalladas`, `v_archivos_cotizaciones`, etc.

**4. CotizacionViewerApp (JavaFX)**
- GUI application to visualize quotations with original broker formatting
- Loads broker formats from database
- Applies colors, fonts, and styles dynamically based on DB configuration
- Uses `RowData` for dynamic column handling

### Data Flow

```
Excel Files → CotizacionOrganizer → Organized by Client
              ↓
         ColumnDetector → Detect Format
              ↓
         FormatoSaver → PostgreSQL
              ↓
    CotizacionViewerApp → Visualize with Format
```

### Broker Support

The system auto-detects these broker formats:
- **MCTC MARINE LTD**: Header row 10, fields include vessel name, IMO, quotation number
- **OCEANIC CATERING LTD**: Header row 13, vessel name and request number
- **CMA CGM**: Header row 19, vessel name
- **GARRETS INTERNATIONAL LTD**: Header row 25, RFQ number and vessel
- **PROCURESHIP**: Header row 14, vessel, IMO, requisition number
- **BSM**: Custom detection logic
- **Generic**: Fallback auto-detection for unknown formats

### Standard Field Mapping

Fields mapped across all broker formats:
- `ITEM_NAME`, `ITEM_CODE`, `CATEGORY`, `DESCRIPTION`
- `QUANTITY`, `UOM` (unit of measure), `UNIT_PRICE`, `TOTAL`
- `BRAND`, `DISCOUNT`, `SUPPLIER_COMMENTS`

### Key Classes

- `QuotationInfo`: Metadata container (vessel, IMO, quotation number, broker)
- `ColumnMapping`: Column structure and style info for a broker format
- `CellStyleInfo`: Cell formatting (background color, font, borders)
- `RowData`: Dynamic row data for JavaFX TableView

## Database

**PostgreSQL Database:** `sistema_cotizacion_2025`
- **Connection:** localhost:5432, user: postgres, password: (empty or provided)
- **Key Tables:** See ESTRUCTURA_BD.md for full schema
- **Views:** Pre-built queries for common operations

**Testing Connection:**
```bash
psql -U postgres -d sistema_cotizacion_2025
```

## Adding New Broker Support

1. Add broker detection in `CotizacionOrganizer.extractQuotationInfo()`
2. Create column detection method in `ColumnDetector.detect*Columns()`
3. Add extraction logic in `CotizacionOrganizer.extract*Info()`
4. Run `FormatoSaver` to save format to database

## File Structure

- `src/main/java/cl/vsschile/` - Java source files
- `cotizaciones-por-cliente/` - Output directory (organized quotations)
- `ESTRUCTURA_BD.md` - Database schema documentation
- `README.md` - Main usage documentation
- `README_VIEWER.md` - JavaFX viewer documentation
- `WORKFLOW_VIEWER.md` - Detailed viewer workflow

## Dependencies

- Apache POI 3.17 (Excel processing)
- PostgreSQL 9.4.1212 (JDBC driver)
- JavaFX 17 (GUI framework)
- Java 17 (compiler target)

## Notes

- Files are **copied**, never moved or modified
- System is safe to run multiple times
- Supports both `.xls` and `.xlsx` formats
- Output preserves original filenames with broker prefix
