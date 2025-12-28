# Flujo de Trabajo: Visor de Cotizaciones JavaFX

## Arquitectura del Sistema

```
┌─────────────────────┐
│   Archivo Excel     │
│   (Cotización)      │
└──────────┬──────────┘
           │
           │ 1. Usuario carga
           ↓
┌─────────────────────────────────────────────┐
│        Aplicación JavaFX                    │
│  ┌───────────────────────────────────────┐  │
│  │  ComboBox Broker: [MCTC MARINE LTD ▼] │  │
│  │  Archivo: [Seleccionar Excel]         │  │
│  │  Status: cotizacion_mctc_01.xlsx      │  │
│  │  [Visualizar con Formato]             │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  2. Lee formato del broker desde BD        │
│     ↓                                       │
│  ┌─────────────────────────────────────┐   │
│  │        TableView Dinámica            │   │
│  │ ┌─────┬──────────┬────────┬──────┐  │   │
│  │ │UNIT │DESCRIPTION│QUANTITY│PRICE │  │   │
│  │ ├─────┼──────────┼────────┼──────┤  │   │
│  │ │ EA  │Item 1    │  10    │100.00│  │   │
│  │ │ KG  │Item 2    │  20    │ 50.00│  │   │
│  │ └─────┴──────────┴────────┴──────┘  │   │
│  └─────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
           ↑
           │ 3. Consulta formato
           │
┌──────────┴──────────┐
│    PostgreSQL       │
│                     │
│  v_formatos_activos │
│  v_columnas_det...  │
└─────────────────────┘
```

## Proceso Paso a Paso

### 1. Inicio de Aplicación

```bash
./run-viewer.sh
```

**Acciones automáticas:**
- Conecta a PostgreSQL
- Carga lista de brokers desde `v_formatos_activos`
- Inicializa UI JavaFX

### 2. Selección de Broker

```
Usuario selecciona: "MCTC MARINE LTD"
                    ↓
BD consulta:        SELECT * FROM v_columnas_detalladas 
                    WHERE broker_name = 'MCTC MARINE LTD'
                    ↓
Obtiene:            - 9 columnas
                    - Header en fila 10
                    - Colores: #D9E1F2 (fondo)
                    - Estilos: negrita, bordes
```

### 3. Carga de Archivo

```
Usuario click: "Seleccionar Excel"
              ↓
FileChooser:  [cotizacion_mctc_01.xlsx]
              ↓
Lectura:      Apache POI → Workbook → Sheet
```

### 4. Visualización con Formato

```
Click: "Visualizar con Formato"
       ↓
┌─────────────────────────────────────────┐
│ 1. Crear columnas según BD              │
│    - UNIT                                │
│    - DESCRIPTION                         │
│    - QUANTITY                            │
│    - ...                                 │
│                                          │
│ 2. Aplicar colores a headers            │
│    - Background: #D9E1F2                 │
│    - Text: #000000                       │
│    - Font: Bold                          │
│                                          │
│ 3. Leer datos desde Excel                │
│    - Desde fila (header_row + 1)         │
│    - Hasta última fila con datos         │
│                                          │
│ 4. Aplicar estilos a celdas              │
│    - Bordes: lightgray                   │
│                                          │
│ 5. Mostrar en TableView                  │
└─────────────────────────────────────────┘
```

## Mapeo de Datos

### Información de BD → Interfaz

```
v_columnas_detalladas
├── nombre_columna_original → TableColumn Header
├── indice_columna          → Posición en TableView
├── color_fondo             → Header background color
├── color_texto             → Header text color
├── es_negrita              → Header font weight
└── tiene_borde             → Cell border style

Excel Cell (row, col)
├── getCellType()           → Detectar tipo de dato
├── getStringCellValue()    → Para texto
├── getNumericCellValue()   → Para números
└── getBooleanCellValue()   → Para booleanos
                ↓
         RowData object
                ↓
         TableView Cell
```

## Ejemplo Completo: MCTC MARINE LTD

### Base de Datos
```sql
-- Formato del broker
formato_id: 1
broker_name: 'MCTC MARINE LTD'
header_row: 10

-- Columnas (9 total)
B: UNIT         - #D9E1F2, negrita, bordes
C: DESCRIPTION  - #D9E1F2, negrita, bordes
D: REMARKS      - #D9E1F2, negrita, bordes
...
```

### Archivo Excel
```
Fila 1-9:   (Metadata del broker)
Fila 10:    [UNIT][DESCRIPTION][REMARKS]... ← Header con colores
Fila 11-50: Datos de cotización
```

### Resultado en JavaFX
```
┌─────────────────────────────────────────────────────────┐
│  TableView<RowData>                                     │
│  ┌──────────────────────────────────────────────────┐   │
│  │ UNIT │ DESCRIPTION  │ REMARKS │ QUANTITY │ ... │   │
│  │ (azul, negrita, bordes)                         │   │
│  ├──────┼──────────────┼─────────┼──────────┼─────┤   │
│  │ EA   │ Fresh Apples │ Grade A │    100   │ ... │   │
│  │ KG   │ Beef Meat    │ Frozen  │     50   │ ... │   │
│  │ ...  │ ...          │ ...     │    ...   │ ... │   │
│  └──────┴──────────────┴─────────┴──────────┴─────┘   │
└─────────────────────────────────────────────────────────┘
```

## Flujo de Datos Detallado

```
1. BD: formato_columnas
   ├── campo_estandar: "ITEM_CODE"
   ├── nombre_original: "UNIT"
   ├── indice_columna: 1 (columna B)
   ├── color_fondo: "#D9E1F2"
   └── es_negrita: true
         ↓
2. JavaFX: createTableColumns()
   ├── new TableColumn<>("UNIT")
   ├── setStyle("-fx-background-color: #D9E1F2; -fx-font-weight: bold;")
   └── column.setPrefWidth(120)
         ↓
3. Excel: Row 11, Cell B11 = "EA"
         ↓
4. RowData: setValue(1, "EA")
         ↓
5. TableView: Muestra "EA" en primera columna
```

## Componentes Clave del Código

### CotizacionViewerApp.java
```java
// 1. Cargar brokers
loadBrokers() → ComboBox<String>

// 2. Cargar formato del broker
loadBrokerColumns(format) → List<ColumnFormat>

// 3. Crear columnas dinámicas
createTableColumns(format) → TableView con N columnas

// 4. Leer datos Excel
loadExcelData(sheet, format) → ObservableList<RowData>

// 5. Aplicar estilos
getHeaderStyle(format) → String CSS
getCellStyle(format) → String CSS
```

### RowData.java
```java
// Array dinámico de propiedades
StringProperty[] properties;

// Acceso por índice
getProperty(int index) → StringProperty
setValue(int index, String value)
```

## Ventajas del Diseño

✅ **Dinámico**: Soporta cualquier broker sin cambios de código  
✅ **Escalable**: Agregar brokers = insertar en BD  
✅ **Consistente**: Formato siempre igual al original  
✅ **Mantenible**: Lógica separada en métodos claros  
✅ **Reutilizable**: Componentes independientes  

## Extensiones Futuras

1. **Edición en Tabla**: Modificar valores in-place
2. **Validación**: Verificar datos según reglas del broker
3. **Exportación**: Generar nuevo Excel con cambios
4. **Comparación**: Vista lado a lado de 2 cotizaciones
5. **Histórico**: Ver cambios en el tiempo
