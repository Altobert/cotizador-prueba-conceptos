# Visor de Cotizaciones JavaFX

Aplicación JavaFX para visualizar cotizaciones Excel con el formato original del broker (colores, estilos) usando información almacenada en PostgreSQL.

## Características

✅ **Selección de Broker**: Dropdown con todos los brokers disponibles en la BD  
✅ **Carga de Archivos**: Selección de archivos Excel (.xlsx, .xls)  
✅ **Formato Automático**: Lee colores y estilos desde la BD  
✅ **Visualización con Colores**: Aplica colores de fondo (#D9E1F2, etc.) y estilos (negrita, bordes)  
✅ **Tabla Dinámica**: Columnas ajustadas según el broker seleccionado  

## Requisitos

- Java 17
- Maven 3.x
- PostgreSQL con base de datos `sistema_cotizacion_2025`
- Brokers y formatos ya registrados en la BD

## Ejecución

### Opción 1: Script de inicio (recomendado)
```bash
./run-viewer.sh
```

### Opción 2: Maven directamente
```bash
mvn compile
mvn exec:java -Dexec.mainClass="cl.vsschile.CotizacionViewerApp"
```

## Uso de la Aplicación

1. **Seleccionar Broker**: Elige un broker del dropdown (ej: "MCTC MARINE LTD")
2. **Cargar Archivo**: Click en "Seleccionar Excel" y elige una cotización
3. **Visualizar**: Click en "Visualizar con Formato"
4. La tabla mostrará la cotización con los colores originales del broker

## Formato de la Tabla

- **Headers**: Con colores de fondo y texto del broker
- **Columnas**: Dinámicas según el formato detectado
- **Datos**: Lee desde la fila después del header (automático)
- **Estilos**: Negrita, bordes, colores aplicados

## Ejemplo

Para MCTC MARINE LTD:
- 9 columnas: UNIT, DESCRIPTION, REMARKS, PART_NUMBER, SPECIFICATION, QUANTITY, UOM, UNIT_PRICE, TOTAL
- Color de fondo: #D9E1F2 (azul claro)
- Texto negro (#000000) en negrita
- Bordes en todas las celdas

## Base de Datos

La aplicación lee de:
- `v_formatos_activos`: Lista de brokers con su formato
- `v_columnas_detalladas`: Columnas con colores y estilos

Conexión:
- Host: localhost:5432
- Database: sistema_cotizacion_2025
- User: postgres
- Password: (vacío - modificar en código si necesario)

## Estructura de Código

```
src/main/java/cl/vsschile/
├── CotizacionViewerApp.java  - Aplicación principal JavaFX
└── RowData.java              - Modelo de datos para filas dinámicas
```

## Solución de Problemas

### Error de conexión a BD
Verifica que PostgreSQL esté corriendo:
```bash
psql -U postgres -d sistema_cotizacion_2025
```

### JavaFX no encontrado
El proyecto usa JavaFX 17 como dependencia Maven. Maven debe descargar automáticamente las librerías.

### No se muestran brokers
Ejecuta primero los scripts de procesamiento para registrar formatos:
```bash
mvn exec:java -Dexec.mainClass="cl.vsschile.FormatoSaver"
```

## Próximas Mejoras

- [ ] Exportar tabla a Excel con formato
- [ ] Búsqueda/filtrado en tabla
- [ ] Comparación de cotizaciones
- [ ] Edición inline de datos
- [ ] Guardar cambios en BD
