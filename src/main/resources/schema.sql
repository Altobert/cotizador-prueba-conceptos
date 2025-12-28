-- Schema para almacenar formatos de cotizaciones por broker
-- Base de datos: sistema_cotizacion_2025
-- Propósito: Almacenar únicamente la estructura/formato de las cotizaciones de cada broker

-- Tabla de Brokers
CREATE TABLE IF NOT EXISTS brokers (
    broker_id SERIAL PRIMARY KEY,
    broker_name VARCHAR(255) NOT NULL UNIQUE,
    descripcion TEXT,
    contacto VARCHAR(255),
    email VARCHAR(255),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Formatos de Cotización por Broker
CREATE TABLE IF NOT EXISTS broker_formatos (
    formato_id SERIAL PRIMARY KEY,
    broker_id INTEGER NOT NULL REFERENCES brokers(broker_id),
    version VARCHAR(50) DEFAULT '1.0',
    header_row INTEGER NOT NULL,
    descripcion TEXT,
    archivo_ejemplo VARCHAR(500),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_broker_version UNIQUE(broker_id, version)
);

-- Tabla de Columnas del Formato
CREATE TABLE IF NOT EXISTS formato_columnas (
    columna_id SERIAL PRIMARY KEY,
    formato_id INTEGER NOT NULL REFERENCES broker_formatos(formato_id) ON DELETE CASCADE,
    campo_estandar VARCHAR(100) NOT NULL,
    nombre_columna_original VARCHAR(255),
    indice_columna INTEGER NOT NULL,
    letra_columna VARCHAR(10),
    tipo_dato VARCHAR(50),
    requerido BOOLEAN DEFAULT FALSE,
    descripcion TEXT,
    -- Información de estilo/color
    color_fondo VARCHAR(20),
    color_texto VARCHAR(20),
    es_negrita BOOLEAN DEFAULT FALSE,
    es_cursiva BOOLEAN DEFAULT FALSE,
    tiene_borde BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_formato_campo UNIQUE(formato_id, campo_estandar)
);

-- Índices para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_broker_nombre ON brokers(broker_name);
CREATE INDEX IF NOT EXISTS idx_formato_broker ON broker_formatos(broker_id);
CREATE INDEX IF NOT EXISTS idx_columna_formato ON formato_columnas(formato_id);
CREATE INDEX IF NOT EXISTS idx_columna_campo ON formato_columnas(campo_estandar);

-- Vista de formatos activos con información del broker
CREATE OR REPLACE VIEW v_formatos_activos AS
SELECT 
    bf.formato_id,
    b.broker_id,
    b.broker_name,
    b.descripcion as broker_descripcion,
    bf.version,
    bf.header_row,
    bf.descripcion as formato_descripcion,
    bf.archivo_ejemplo,
    COUNT(fc.columna_id) as total_columnas,
    bf.fecha_actualizacion
FROM broker_formatos bf
INNER JOIN brokers b ON bf.broker_id = b.broker_id
LEFT JOIN formato_columnas fc ON bf.formato_id = fc.formato_id
WHERE bf.activo = TRUE AND b.activo = TRUE
GROUP BY bf.formato_id, b.broker_id, b.broker_name, b.descripcion,
         bf.version, bf.header_row, bf.descripcion, bf.archivo_ejemplo, bf.fecha_actualizacion;

-- Vista de columnas con información del broker y formato
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

-- Comentarios en las tablas
COMMENT ON TABLE brokers IS 'Catálogo de brokers que envían cotizaciones';
COMMENT ON TABLE broker_formatos IS 'Formatos de Excel por broker, permite versionado';
COMMENT ON TABLE formato_columnas IS 'Mapeo de columnas del formato a campos estándar';

COMMENT ON COLUMN formato_columnas.campo_estandar IS 'Nombre del campo estándar: ITEM_NAME, QUANTITY, UNIT_PRICE, etc.';
COMMENT ON COLUMN formato_columnas.indice_columna IS 'Índice de columna en Excel (0-based)';
COMMENT ON COLUMN formato_columnas.letra_columna IS 'Letra de la columna en Excel (A, B, C, etc.)';
