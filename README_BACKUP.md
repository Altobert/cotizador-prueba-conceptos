# 📦 Backup Completo - Tablas de Brokers y Formatos

## 📋 Información del Backup

**Archivo:** `backup_brokers_formatos_completo.sql`  
**Tamaño:** 76 KB  
**Líneas:** 1,470  
**Fecha:** 6 de enero de 2026  
**Base de datos:** `sistema_cotizacion_2025`  

---

## 📊 Contenido del Backup

### Tablas Incluidas (6)

1. **brokers** - Catálogo de brokers
2. **broker_formatos** - Formatos/plantillas por broker
3. **formato_columnas** - Mapeo de columnas a campos estándar
4. **broker_metadata** - Metadata de cabeceras de brokers
5. **cotizaciones_archivos** - Registro de archivos procesados
6. **archivo_colores** - Colores y estilos por archivo

### Datos por Tabla

| Tabla | Registros | Descripción |
|-------|-----------|-------------|
| `brokers` | 11 | Brokers activos en el sistema |
| `broker_formatos` | 6 | Formatos detectados y configurados |
| `formato_columnas` | 77 | Columnas mapeadas en total |
| `broker_metadata` | 51 | Campos de metadata extraídos |
| `cotizaciones_archivos` | 0 | Archivos procesados |
| `archivo_colores` | 0 | Configuraciones de colores |

### Elementos de Base de Datos

- ✅ **6 CREATE TABLE** statements con estructura completa
- 🔑 **6 Primary Keys**
- 🔗 **6 Foreign Keys**
- 🔢 **6 Sequences** para autoincrementales
- 📇 **11 Indexes** para optimización

---

## 🚀 Cómo Restaurar el Backup

### Opción 1: Restauración Completa (Reemplaza la base de datos)

```bash
psql -U postgres < backup_brokers_formatos_completo.sql
```

⚠️ **ADVERTENCIA:** Este comando eliminará y recreará la base de datos `sistema_cotizacion_2025`.

### Opción 2: Restauración Solo de Tablas (Sin recrear la base de datos)

1. Editar el archivo y comentar las líneas de DROP/CREATE DATABASE:
```bash
# Comentar estas líneas en el archivo:
# DROP DATABASE IF EXISTS sistema_cotizacion_2025;
# CREATE DATABASE sistema_cotizacion_2025 ...
```

2. Ejecutar la restauración:
```bash
psql -U postgres -d sistema_cotizacion_2025 < backup_brokers_formatos_completo.sql
```

### Opción 3: Restauración Selectiva de una Tabla

```bash
# Restaurar solo la tabla brokers
pg_restore -U postgres -d sistema_cotizacion_2025 -t brokers backup_brokers_formatos_completo.sql
```

---

## 📝 Estructura de los Datos

### Brokers (11)
```
1. OCEANIC CATERING LTD
2. MCTC MARINE LTD
3. CMA CGM
4. PROCURESHIP
5. GARRETS INTERNATIONAL LTD
6. BSM CATERING
7. MSC SHIPMANAGEMENT
8. ANGLO EASTERN
9. UMAR
10. BERNHARD SCHULTE
11. OPERATION3
```

### Formatos Configurados (6)

| ID | Broker | Header Row | Columnas | Archivo Ejemplo |
|----|--------|------------|----------|-----------------|
| 1 | OCEANIC CATERING LTD | 13 | 15 | OAS1210RO008910122025150143766.xlsx |
| 2 | MCTC MARINE LTD | 10 | 9 | QTN_LOU_233.xlsx |
| 3 | CMA CGM | 19 | 12 | 2679-2025R-0342-1293695-124805 (1).xlsx |
| 4 | PROCURESHIP | 14 | 14 | ATRA-ST-25-104_3_Valparaiso Ship Services SA (1).xlsx |
| 5 | GARRETS INTERNATIONAL LTD | 25 | 10 | quotation_RFQ0204875.xlsx |
| 7 | BSM CATERING | 19 | 17 | RFQ_EQ_SCF_CISA_0152.xlsm |

### Metadata Extraída (51 campos)

| Broker | Campos de Metadata | Secciones |
|--------|-------------------|-----------|
| BSM CATERING | 28 | Company Details, RFQ Information, Vendor Details |
| CMA CGM | 8 | Company Details, Vendor Details |
| OCEANIC CATERING LTD | 6 | Company Information, Request Information |
| MCTC MARINE LTD | 4 | Quotation Header |
| PROCURESHIP | 4 | Requisition Information |
| GARRETS INTERNATIONAL LTD | 1 | RFQ Information |

---

## 🔄 Regenerar Datos de Formatos

Si necesitas volver a generar los datos de `formato_columnas` y `broker_metadata`:

```bash
cd /Users/albertosanmartin/Documents/VSS-COTIZACIONES/cotizacion-organizer

mvn exec:java -Dexec.mainClass="cl.vsschile.FormatoSaver" \
  -Dexec.args="/Users/albertosanmartin/Documents/VSS-COTIZACIONES/brokers20-12-2025"
```

---

## 📁 Archivos Relacionados

- **`scripts_tablas_brokers.sql`** - Script de creación de tablas con estructura y datos iniciales
- **`backup_brokers_formatos_completo.sql`** - Backup completo con pg_dump (este archivo)
- **`ESTRUCTURA_BD.md`** - Documentación del esquema completo de base de datos

---

## ⚠️ Notas Importantes

1. **El backup incluye:**
   - ✅ Estructura completa de las 6 tablas
   - ✅ Todos los constraints (PK, FK, UNIQUE)
   - ✅ Índices de optimización
   - ✅ Sequences para autoincrementales
   - ✅ Todos los datos actuales

2. **El backup NO incluye:**
   - ❌ Otras tablas del sistema (cotizacion, producto, cliente, etc.)
   - ❌ Vistas (se deben recrear manualmente)
   - ❌ Funciones o procedimientos almacenados

3. **Compatibilidad:**
   - PostgreSQL 15.x o superior
   - Generado con pg_dump 15.15 (Homebrew)

---

## 🆘 Soporte

Para regenerar el backup:

```bash
pg_dump -U postgres -d sistema_cotizacion_2025 \
  -t brokers \
  -t broker_formatos \
  -t formato_columnas \
  -t broker_metadata \
  -t cotizaciones_archivos \
  -t archivo_colores \
  --clean --if-exists --create \
  -f backup_brokers_formatos_completo.sql
```

---

**Última actualización:** 6 de enero de 2026  
**Generado por:** Sistema de Cotizaciones VSS 2025
