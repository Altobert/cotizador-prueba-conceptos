-- ============================================================================
-- SCRIPTS DE CREACIÓN DE TABLAS - SISTEMA DE COTIZACIONES 2025
-- Módulo: Brokers y Formatos
-- ============================================================================

-- ============================================================================
-- 1. TABLA: brokers
-- Descripción: Catálogo de brokers que envían cotizaciones
-- ============================================================================
CREATE TABLE brokers (
    broker_id SERIAL PRIMARY KEY,
    broker_name VARCHAR(255) NOT NULL UNIQUE,
    descripcion TEXT,
    contacto VARCHAR(255),
    email VARCHAR(255),
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices
CREATE INDEX idx_broker_nombre ON brokers(broker_name);

-- Comentarios
COMMENT ON TABLE brokers IS 'Catálogo de brokers que envían cotizaciones';
COMMENT ON COLUMN brokers.broker_id IS 'ID único del broker';
COMMENT ON COLUMN brokers.broker_name IS 'Nombre del broker (único)';
COMMENT ON COLUMN brokers.activo IS 'Indica si el broker está activo';


-- ============================================================================
-- 2. TABLA: broker_formatos
-- Descripción: Formatos/plantillas de Excel por broker (permite versionado)
-- ============================================================================
CREATE TABLE broker_formatos (
    formato_id SERIAL PRIMARY KEY,
    broker_id INTEGER NOT NULL REFERENCES brokers(broker_id),
    version VARCHAR(20) NOT NULL DEFAULT '1.0',
    header_row INTEGER NOT NULL,
    descripcion TEXT,
    archivo_ejemplo VARCHAR(500),
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_broker_version UNIQUE (broker_id, version)
);

-- Índices
CREATE INDEX idx_formato_broker ON broker_formatos(broker_id);
CREATE INDEX idx_formato_activo ON broker_formatos(activo);

-- Comentarios
COMMENT ON TABLE broker_formatos IS 'Formatos/plantillas de Excel por broker con versionado';
COMMENT ON COLUMN broker_formatos.formato_id IS 'ID único del formato';
COMMENT ON COLUMN broker_formatos.header_row IS 'Fila donde están los encabezados (índice base 0)';
COMMENT ON COLUMN broker_formatos.version IS 'Versión del formato (ej: 1.0, 2.0)';
COMMENT ON COLUMN broker_formatos.archivo_ejemplo IS 'Nombre del archivo usado como ejemplo';


-- ============================================================================
-- 3. TABLA: formato_columnas
-- Descripción: Mapeo de columnas del formato a campos estándar
-- ============================================================================
CREATE TABLE formato_columnas (
    columna_id SERIAL PRIMARY KEY,
    formato_id INTEGER NOT NULL REFERENCES broker_formatos(formato_id) ON DELETE CASCADE,
    campo_estandar VARCHAR(100) NOT NULL,
    nombre_columna_original VARCHAR(255),
    indice_columna INTEGER NOT NULL,
    letra_columna VARCHAR(5),
    tipo_dato VARCHAR(50),
    requerido BOOLEAN DEFAULT false,
    descripcion TEXT,
    color_fondo VARCHAR(20),
    color_texto VARCHAR(20),
    es_negrita BOOLEAN DEFAULT false,
    es_cursiva BOOLEAN DEFAULT false,
    tiene_borde BOOLEAN DEFAULT false,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_formato_campo UNIQUE (formato_id, campo_estandar)
);

-- Índices
CREATE INDEX idx_columna_formato ON formato_columnas(formato_id);
CREATE INDEX idx_columna_campo ON formato_columnas(campo_estandar);

-- Comentarios
COMMENT ON TABLE formato_columnas IS 'Mapeo de columnas del formato a campos estándar';
COMMENT ON COLUMN formato_columnas.campo_estandar IS 'Nombre estándar del campo (ej: ITEM_NAME, QUANTITY)';
COMMENT ON COLUMN formato_columnas.nombre_columna_original IS 'Nombre original en el Excel del broker';
COMMENT ON COLUMN formato_columnas.indice_columna IS 'Índice de columna (base 0)';
COMMENT ON COLUMN formato_columnas.letra_columna IS 'Letra de columna (A, B, C, etc.)';


-- ============================================================================
-- 4. TABLA: broker_metadata
-- Descripción: Metadata de las cabeceras de los brokers
-- ============================================================================
CREATE TABLE broker_metadata (
    metadata_id SERIAL PRIMARY KEY,
    formato_id INTEGER NOT NULL REFERENCES broker_formatos(formato_id) ON DELETE CASCADE,
    seccion VARCHAR(100),
    campo_nombre VARCHAR(100),
    campo_valor TEXT,
    fila_origen INTEGER,
    columna_origen INTEGER,
    letra_columna VARCHAR(5),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices
CREATE INDEX idx_broker_metadata_formato ON broker_metadata(formato_id);
CREATE INDEX idx_broker_metadata_seccion ON broker_metadata(seccion);

-- Comentarios
COMMENT ON TABLE broker_metadata IS 'Metadata extraída de las cabeceras de los archivos de brokers';
COMMENT ON COLUMN broker_metadata.seccion IS 'Sección de la metadata (ej: Company Details, RFQ Information)';
COMMENT ON COLUMN broker_metadata.campo_nombre IS 'Nombre del campo (ej: Vendor Name, Email)';
COMMENT ON COLUMN broker_metadata.campo_valor IS 'Valor del campo';
COMMENT ON COLUMN broker_metadata.fila_origen IS 'Fila donde se encuentra el campo (base 0)';
COMMENT ON COLUMN broker_metadata.columna_origen IS 'Columna donde se encuentra el campo (base 0)';


-- ============================================================================
-- 5. TABLA: cotizaciones_archivos
-- Descripción: Registro de cada archivo/planilla de cotización procesado
-- ============================================================================
CREATE TABLE cotizaciones_archivos (
    archivo_id SERIAL PRIMARY KEY,
    broker_id INTEGER NOT NULL REFERENCES brokers(broker_id),
    formato_id INTEGER REFERENCES broker_formatos(formato_id),
    nombre_archivo VARCHAR(500) NOT NULL,
    ruta_archivo TEXT,
    vessel_name VARCHAR(255),
    imo_number VARCHAR(50),
    numero_cotizacion VARCHAR(100),
    fecha_archivo TIMESTAMP,
    fecha_procesado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_broker_archivo UNIQUE (broker_id, nombre_archivo)
);

-- Índices
CREATE INDEX idx_archivo_broker ON cotizaciones_archivos(broker_id);
CREATE INDEX idx_archivo_formato ON cotizaciones_archivos(formato_id);
CREATE INDEX idx_archivo_vessel ON cotizaciones_archivos(vessel_name);
CREATE INDEX idx_archivo_imo ON cotizaciones_archivos(imo_number);

-- Comentarios
COMMENT ON TABLE cotizaciones_archivos IS 'Registro de cada archivo Excel de cotización procesado';
COMMENT ON COLUMN cotizaciones_archivos.archivo_id IS 'ID único del archivo';
COMMENT ON COLUMN cotizaciones_archivos.vessel_name IS 'Nombre del vessel/barco';
COMMENT ON COLUMN cotizaciones_archivos.imo_number IS 'Número IMO del vessel';
COMMENT ON COLUMN cotizaciones_archivos.numero_cotizacion IS 'Número de cotización/RFQ';


-- ============================================================================
-- 6. TABLA: archivo_colores
-- Descripción: Colores y estilos específicos de cada archivo de cotización
-- ============================================================================
CREATE TABLE archivo_colores (
    color_id SERIAL PRIMARY KEY,
    archivo_id INTEGER NOT NULL REFERENCES cotizaciones_archivos(archivo_id) ON DELETE CASCADE,
    campo_estandar VARCHAR(100),
    nombre_columna_original VARCHAR(255),
    indice_columna INTEGER,
    letra_columna VARCHAR(5),
    color_fondo VARCHAR(20),
    color_texto VARCHAR(20),
    es_negrita BOOLEAN DEFAULT false,
    es_cursiva BOOLEAN DEFAULT false,
    tiene_borde BOOLEAN DEFAULT false,
    fecha_deteccion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_archivo_campo UNIQUE (archivo_id, campo_estandar)
);

-- Índices
CREATE INDEX idx_colores_archivo ON archivo_colores(archivo_id);
CREATE INDEX idx_colores_campo ON archivo_colores(campo_estandar);

-- Comentarios
COMMENT ON TABLE archivo_colores IS 'Colores y estilos específicos de cada archivo individual';
COMMENT ON COLUMN archivo_colores.campo_estandar IS 'Campo estándar mapeado';
COMMENT ON COLUMN archivo_colores.color_fondo IS 'Color de fondo en formato hex (ej: #D9E1F2)';
COMMENT ON COLUMN archivo_colores.color_texto IS 'Color de texto en formato hex';


-- ============================================================================
-- VISTAS
-- ============================================================================

-- Vista: v_formatos_activos
-- Descripción: Resumen de formatos activos con conteo de columnas
CREATE OR REPLACE VIEW v_formatos_activos AS
SELECT 
    bf.formato_id,
    bf.broker_id,
    b.broker_name,
    bf.version,
    bf.header_row,
    bf.descripcion AS formato_descripcion,
    COUNT(fc.columna_id) AS total_columnas
FROM broker_formatos bf
JOIN brokers b ON bf.broker_id = b.broker_id
LEFT JOIN formato_columnas fc ON bf.formato_id = fc.formato_id
WHERE bf.activo = true
GROUP BY bf.formato_id, bf.broker_id, b.broker_name, bf.version, bf.header_row, bf.descripcion
ORDER BY b.broker_name;

COMMENT ON VIEW v_formatos_activos IS 'Resumen de formatos activos con conteo de columnas';


-- Vista: v_columnas_detalladas
-- Descripción: Detalle de columnas con sus colores para cada formato
CREATE OR REPLACE VIEW v_columnas_detalladas AS
SELECT 
    fc.columna_id,
    fc.formato_id,
    b.broker_name,
    bf.version,
    fc.campo_estandar,
    fc.nombre_columna_original,
    fc.indice_columna,
    fc.letra_columna,
    fc.tipo_dato,
    fc.requerido,
    fc.color_fondo,
    fc.color_texto,
    fc.es_negrita,
    fc.es_cursiva,
    fc.tiene_borde
FROM formato_columnas fc
JOIN broker_formatos bf ON fc.formato_id = bf.formato_id
JOIN brokers b ON bf.broker_id = b.broker_id
ORDER BY b.broker_name, fc.indice_columna;

COMMENT ON VIEW v_columnas_detalladas IS 'Detalle de columnas con estilos para cada formato';


-- Vista: v_broker_metadata
-- Descripción: Metadata de brokers con información del formato
CREATE OR REPLACE VIEW v_broker_metadata AS
SELECT 
    bm.metadata_id,
    b.broker_name,
    bf.formato_id,
    bf.version,
    bm.seccion,
    bm.campo_nombre,
    bm.campo_valor,
    bm.fila_origen,
    bm.letra_columna
FROM broker_metadata bm
JOIN broker_formatos bf ON bm.formato_id = bf.formato_id
JOIN brokers b ON bf.broker_id = b.broker_id
ORDER BY b.broker_name, bm.seccion, bm.fila_origen;

COMMENT ON VIEW v_broker_metadata IS 'Metadata de cabeceras de brokers con información del formato';


-- Vista: v_archivos_cotizaciones
-- Descripción: Lista de archivos procesados con estadísticas
CREATE OR REPLACE VIEW v_archivos_cotizaciones AS
SELECT 
    ca.archivo_id,
    b.broker_name,
    ca.nombre_archivo,
    ca.vessel_name,
    ca.imo_number,
    ca.numero_cotizacion,
    ca.fecha_archivo,
    ca.fecha_procesado,
    COUNT(ac.color_id) AS total_columnas_con_color
FROM cotizaciones_archivos ca
JOIN brokers b ON ca.broker_id = b.broker_id
LEFT JOIN archivo_colores ac ON ca.archivo_id = ac.archivo_id
GROUP BY ca.archivo_id, b.broker_name, ca.nombre_archivo, ca.vessel_name, 
         ca.imo_number, ca.numero_cotizacion, ca.fecha_archivo, ca.fecha_procesado
ORDER BY ca.fecha_procesado DESC;

COMMENT ON VIEW v_archivos_cotizaciones IS 'Lista de archivos procesados con estadísticas de colores';


-- ============================================================================
-- DATOS ACTUALES DE LAS TABLAS
-- Generado: 2025-12-28
-- ============================================================================

-- ============================================================================
-- DATOS: brokers (11 registros)
-- ============================================================================
INSERT INTO brokers (broker_id, broker_name, activo) VALUES 
    (1, 'OCEANIC CATERING LTD', true),
    (2, 'MCTC MARINE LTD', true),
    (3, 'CMA CGM', true),
    (4, 'PROCURESHIP', true),
    (5, 'GARRETS INTERNATIONAL LTD', true),
    (6, 'BSM CATERING', true),
    (7, 'MSC SHIPMANAGEMENT', true),
    (8, 'ANGLO EASTERN', true),
    (9, 'UMAR', true),
    (10, 'BERNHARD SCHULTE', true),
    (11, 'OPERATION3', true)
ON CONFLICT (broker_name) DO NOTHING;

-- Ajustar secuencia
SELECT setval('brokers_broker_id_seq', (SELECT MAX(broker_id) FROM brokers));


-- ============================================================================
-- DATOS: broker_formatos (6 registros)
-- ============================================================================
INSERT INTO broker_formatos (formato_id, broker_id, version, header_row, archivo_ejemplo, activo) VALUES 
    (1, 1, '1.0', 12, 'OAS1210RO008910122025150143766.xlsx', true),
    (2, 2, '1.0', 9, 'QTN_LOU_233.xlsx', true),
    (3, 3, '1.0', 18, '2679-2025R-0342-1293695-124805 (1).xlsx', true),
    (4, 4, '1.0', 13, 'ATRA-ST-25-104_3_Valparaiso Ship Services SA (1).xlsx', true),
    (5, 5, '1.0', 24, 'quotation_RFQ0204875.xlsx', true),
    (7, 6, '1.0', 18, 'RFQ_EQ_SCF_CISA_0152.xlsm', true)
ON CONFLICT (broker_id, version) DO UPDATE 
    SET header_row = EXCLUDED.header_row,
        archivo_ejemplo = EXCLUDED.archivo_ejemplo,
        activo = EXCLUDED.activo;

-- Ajustar secuencia
SELECT setval('broker_formatos_formato_id_seq', (SELECT MAX(formato_id) FROM broker_formatos));


-- ============================================================================
-- NOTA SOBRE DATOS DE formato_columnas y broker_metadata
-- ============================================================================
-- Los datos de formato_columnas y broker_metadata son extensos:
-- 
-- formato_columnas: 77 registros totales
--   - Formato 1 (OCEANIC): 15 columnas
--   - Formato 2 (MCTC): 9 columnas  
--   - Formato 3 (CMA CGM): 12 columnas
--   - Formato 4 (PROCURESHIP): 14 columnas
--   - Formato 5 (GARRETS): 10 columnas
--   - Formato 7 (BSM CATERING): 17 columnas
--
-- broker_metadata: 51 registros totales
--   - Formato 1 (OCEANIC): 6 campos de metadata
--   - Formato 2 (MCTC): 4 campos de metadata
--   - Formato 3 (CMA CGM): 8 campos de metadata
--   - Formato 4 (PROCURESHIP): 4 campos de metadata
--   - Formato 5 (GARRETS): 1 campo de metadata
--   - Formato 7 (BSM CATERING): 28 campos de metadata
--
-- Estos datos se generan automáticamente ejecutando:
--   mvn exec:java -Dexec.mainClass="cl.vsschile.FormatoSaver" \
--     -Dexec.args="<ruta-al-directorio-BROKERS>"
--
-- Para exportar los datos actuales, ejecutar:
--   pg_dump -U postgres -d sistema_cotizacion_2025 \
--     -t formato_columnas -t broker_metadata --data-only > datos_formatos.sql
--


-- ============================================================================
-- FIN DE SCRIPTS
-- ============================================================================
