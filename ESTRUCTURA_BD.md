# Estructura de Base de Datos - Sistema de Cotizaciones

## 📊 Tablas Base

### 1. `brokers`
Catálogo de brokers que envían cotizaciones.

**Columnas:**
- `broker_id` (PK)
- `broker_name` (UNIQUE)
- `descripcion`
- `contacto`
- `email`
- `activo`
- `fecha_creacion`
- `fecha_actualizacion`

---

### 2. `broker_formatos`
Formatos/plantillas de Excel por broker (permite versionado).

**Columnas:**
- `formato_id` (PK)
- `broker_id` (FK → brokers)
- `version`
- `header_row`
- `descripcion`
- `archivo_ejemplo`
- `activo`
- `fecha_creacion`
- `fecha_actualizacion`

**Uso:** Define el formato estándar de un broker (1 formato por broker)

---

### 3. `formato_columnas`
Mapeo de columnas del formato a campos estándar.

**Columnas:**
- `columna_id` (PK)
- `formato_id` (FK → broker_formatos)
- `campo_estandar` (ej: ITEM_NAME, QUANTITY, UNIT_PRICE)
- `nombre_columna_original`
- `indice_columna`
- `letra_columna`
- `tipo_dato`
- `requerido`
- `descripcion`
- `color_fondo`
- `color_texto`
- `es_negrita`
- `es_cursiva`
- `tiene_borde`
- `fecha_creacion`

**Uso:** Define qué columna es qué (plantilla del formato)

---

### 4. `cotizaciones_archivos`
Registro de cada archivo/planilla de cotización procesado.

**Columnas:**
- `archivo_id` (PK)
- `broker_id` (FK → brokers)
- `formato_id` (FK → broker_formatos)
- `nombre_archivo` (UNIQUE con broker_id)
- `ruta_archivo`
- `vessel_name`
- `imo_number`
- `numero_cotizacion`
- `fecha_archivo`
- `fecha_procesado`

**Uso:** UN registro por cada archivo Excel procesado

---

### 5. `archivo_colores`
Colores y estilos específicos de cada archivo de cotización.

**Columnas:**
- `color_id` (PK)
- `archivo_id` (FK → cotizaciones_archivos)
- `campo_estandar`
- `nombre_columna_original`
- `indice_columna`
- `letra_columna`
- `color_fondo`
- `color_texto`
- `es_negrita`
- `es_cursiva`
- `tiene_borde`
- `fecha_deteccion`

**Uso:** Colores específicos de cada archivo individual

---

## 👁️ Vistas

### 1. `v_formatos_activos`
**Basada en:**
- `broker_formatos` (bf)
- `brokers` (b)
- `formato_columnas` (fc)

**Muestra:** Resumen de formatos/plantillas activos con conteo de columnas.

**Uso:** Ver los formatos estándar de cada broker.

---

### 2. `v_columnas_detalladas`
**Basada en:**
- `formato_columnas` (fc)
- `broker_formatos` (bf)
- `brokers` (b)

**Muestra:** Detalle de columnas con sus colores para cada formato/plantilla.

**Uso:** Ver la estructura de columnas del formato estándar de un broker.

---

### 3. `v_archivos_cotizaciones`
**Basada en:**
- `cotizaciones_archivos` (ca)
- `brokers` (b)
- `archivo_colores` (ac)

**Muestra:** Lista de archivos procesados con estadísticas de colores.

**Uso:** Ver todos los archivos procesados con resumen de columnas y colores.

---

### 4. `v_colores_por_archivo`
**Basada en:**
- `archivo_colores` (ac)
- `cotizaciones_archivos` (ca)
- `brokers` (b)

**Muestra:** Detalle de colores por cada archivo individual.

**Uso:** Ver los colores específicos de un archivo en particular.

---

### 5. `v_comparacion_colores_broker`
**Basada en:**
- `archivo_colores` (ac)
- `cotizaciones_archivos` (ca)
- `brokers` (b)

**Muestra:** Variaciones de color entre archivos del mismo broker.

**Uso:** Detectar si un broker usa diferentes colores en diferentes cotizaciones.

---

## 🔗 Relaciones

```
brokers (1) ──────┬──> (N) broker_formatos
                  │         │
                  │         └──> (N) formato_columnas
                  │
                  └──> (N) cotizaciones_archivos
                            │
                            └──> (N) archivo_colores
```

## 📋 Diferencia Clave

### Tabla de Formato (plantilla):
- `formato_columnas` → Define el formato **estándar** del broker
- 1 formato por broker
- Usado como plantilla/referencia

### Tabla de Archivos (datos reales):
- `archivo_colores` → Colores de **cada archivo individual**
- N registros por broker (uno por cada archivo)
- Datos reales de cada cotización

## 🎯 Ejemplo Práctico

**MCTC MARINE LTD tiene:**

### En formato_columnas:
- 9 columnas (plantilla)
- Columna B → #D9E1F2

### En archivo_colores:
- 20 archivos × 9 columnas = 180 registros
- `QTN_LOU_233.xlsx` Columna B → #D9E1F2
- `QTN_LOU_234.xlsx` Columna B → #D9E1F2
- `QTN_PPR_342.xlsx` Columna B → #D9E1F2
- ... (20 archivos en total)

## 📊 Consultas Útiles

```sql
-- Ver plantilla/formato de un broker
SELECT * FROM v_columnas_detalladas 
WHERE broker_name = 'MCTC MARINE LTD';

-- Ver archivos procesados de un broker
SELECT * FROM v_archivos_cotizaciones 
WHERE broker_name = 'MCTC MARINE LTD';

-- Ver colores de un archivo específico
SELECT * FROM v_colores_por_archivo 
WHERE nombre_archivo = 'QTN_LOU_233.xlsx';

-- Detectar variaciones de color
SELECT * FROM v_comparacion_colores_broker;
```
