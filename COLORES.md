# Detección de Colores y Estilos en Cotizaciones

El sistema ahora detecta y almacena información de colores y estilos de las celdas del header de cada formato de broker.

## 📊 Información Capturada

Para cada columna del header se detecta:

- **Color de Fondo** (Background): Formato hexadecimal `#RRGGBB`
- **Color de Texto** (Foreground): Formato hexadecimal `#RRGGBB`
- **Negrita** (Bold): Si/No
- **Cursiva** (Italic): Si/No
- **Bordes**: Si la celda tiene bordes

## 🗄️ Estructura en Base de Datos

La tabla `formato_columnas` ahora incluye:

```sql
- color_fondo VARCHAR(20)        -- Ej: #FF0000 (rojo)
- color_texto VARCHAR(20)        -- Ej: #000000 (negro)
- es_negrita BOOLEAN
- es_cursiva BOOLEAN
- tiene_borde BOOLEAN
```

## 🚀 Uso

### 1. Actualizar la base de datos

Si ya tienes las tablas creadas, ejecuta esta migración:

```sql
-- Agregar columnas de estilo a la tabla existente
ALTER TABLE formato_columnas 
    ADD COLUMN IF NOT EXISTS color_fondo VARCHAR(20),
    ADD COLUMN IF NOT EXISTS color_texto VARCHAR(20),
    ADD COLUMN IF NOT EXISTS es_negrita BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS es_cursiva BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS tiene_borde BOOLEAN DEFAULT FALSE;

-- Recrear la vista con los nuevos campos
DROP VIEW IF EXISTS v_columnas_detalladas;
CREATE OR REPLACE VIEW v_columnas_detalladas AS
SELECT 
    b.broker_name,
    bf.version,
    bf.header_row,
    fc.campo_estandar,
    fc.nombre_columna_original,
    fc.indice_columna,
    fc.letra_columna,
    fc.tipo_dato,
    fc.requerido,
    fc.descripcion,
    fc.color_fondo,
    fc.color_texto,
    fc.es_negrita,
    fc.es_cursiva,
    fc.tiene_borde
FROM formato_columnas fc
INNER JOIN broker_formatos bf ON fc.formato_id = bf.formato_id
INNER JOIN brokers b ON bf.broker_id = b.broker_id
WHERE bf.activo = TRUE AND b.activo = TRUE
ORDER BY b.broker_name, bf.version, fc.indice_columna;
```

### 2. Guardar formatos con colores

```bash
cd /Users/albertosanmartin/Documents/VSS-COTIZACIONES/cotizacion-organizer

mvn clean compile

mvn exec:java -Dexec.mainClass="cl.vsschile.FormatoSaver" \
  -Dexec.args="/Users/albertosanmartin/Documents/VSS-COTIZACIONES/cotizaciones-por-cliente/OneDrive_1_20-12-2025/BROKERS"
```

La herramienta automáticamente detectará y guardará los colores.

## 📝 Consultas Útiles

### Ver columnas con sus colores

```sql
SELECT 
    broker_name,
    campo_estandar,
    letra_columna,
    nombre_columna_original,
    color_fondo,
    color_texto,
    CASE 
        WHEN es_negrita THEN 'Bold' 
        ELSE '' 
    END as estilo
FROM v_columnas_detalladas
WHERE color_fondo IS NOT NULL
ORDER BY broker_name, indice_columna;
```

### Encontrar todas las columnas con fondo rojo

```sql
SELECT broker_name, campo_estandar, letra_columna, color_fondo
FROM v_columnas_detalladas
WHERE color_fondo LIKE '#FF%'  -- Tonos rojos
ORDER BY broker_name;
```

### Columnas con estilo especial (negrita o color)

```sql
SELECT 
    broker_name,
    campo_estandar,
    letra_columna,
    COALESCE(color_fondo, 'Sin color') as fondo,
    CASE 
        WHEN es_negrita THEN '✓' 
        ELSE '✗' 
    END as negrita
FROM v_columnas_detalladas
WHERE es_negrita = TRUE OR color_fondo IS NOT NULL
ORDER BY broker_name;
```

### Resumen de colores por broker

```sql
SELECT 
    broker_name,
    COUNT(*) as total_columnas,
    COUNT(color_fondo) as con_color_fondo,
    COUNT(CASE WHEN es_negrita THEN 1 END) as columnas_negrita
FROM v_columnas_detalladas
GROUP BY broker_name
ORDER BY broker_name;
```

## 🎨 Formato de Colores

Los colores se guardan en formato hexadecimal RGB:

- `#FFFFFF` - Blanco
- `#000000` - Negro
- `#FF0000` - Rojo
- `#00FF00` - Verde
- `#0000FF` - Azul
- `#FFFF00` - Amarillo
- `#FFA500` - Naranja
- `#C0C0C0` - Gris claro
- `#808080` - Gris
- etc.

Para archivos `.xls` antiguos con colores indexados, el sistema mapea automáticamente los índices a colores RGB.

## 🔍 Ejemplo de Salida

Cuando ejecutas `FormatoSaver`, verás algo así:

```
Procesando: MCTC MARINE LTD
  Archivo ejemplo: QTN_LOU_233.xlsx
  ✓ Columnas detectadas: 9
  ✓ Header en fila: 10
  ✓ Guardado en BD (formato_id: 1)

┌─────────────────────────────────────────────
│ MCTC MARINE LTD
├─────────────────────────────────────────────
│ ID: 1
│ Versión: 1.0
│ Header Row: 10
│ Total Columnas: 9
│
│ Columnas:
│   CATEGORY            -> C (3) "FOOD CATEGORIES" [BG:#E6E6FA Bold]
│   ITEM_CODE           -> B (2) "MCTC 'S REF NO" [BG:#FFD700 Bold]
│   ITEM_NAME           -> D (4) "ITEM" [Bold]
│   QUANTITY            -> G (7) "QUANTITY ORDER" [BG:#90EE90]
│   UNIT_PRICE          -> H (8) "PRICE" [BG:#90EE90]
└─────────────────────────────────────────────
```

## 📌 Notas

- Los colores se detectan tanto en archivos `.xlsx` (modernos) como `.xls` (legacy)
- Los colores condicionales o aplicados por fórmulas pueden no detectarse
- Si una celda no tiene color definido, el valor será `NULL` en la BD
- Los estilos solo se capturan del header, no de las filas de datos
