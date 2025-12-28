-- Extension del schema para almacenar colores por cada archivo de cotización
-- Base de datos: sistema_cotizacion_2025

-- Tabla para registrar cada archivo/planilla de cotización
CREATE TABLE IF NOT EXISTS cotizaciones_archivos (
    archivo_id SERIAL PRIMARY KEY,
    broker_id INTEGER NOT NULL REFERENCES brokers(broker_id),
    formato_id INTEGER REFERENCES broker_formatos(formato_id),
    nombre_archivo VARCHAR(500) NOT NULL,
    ruta_archivo TEXT,
    vessel_name VARCHAR(255),
    imo_number VARCHAR(50),
    numero_cotizacion VARCHAR(255),
    fecha_archivo DATE,
    fecha_procesado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_broker_archivo UNIQUE(broker_id, nombre_archivo)
);

-- Tabla para almacenar colores específicos de cada archivo
CREATE TABLE IF NOT EXISTS archivo_colores (
    color_id SERIAL PRIMARY KEY,
    archivo_id INTEGER NOT NULL REFERENCES cotizaciones_archivos(archivo_id) ON DELETE CASCADE,
    campo_estandar VARCHAR(100) NOT NULL,
    nombre_columna_original VARCHAR(255),
    indice_columna INTEGER NOT NULL,
    letra_columna VARCHAR(10),
    -- Colores y estilos específicos de este archivo
    color_fondo VARCHAR(20),
    color_texto VARCHAR(20),
    es_negrita BOOLEAN DEFAULT FALSE,
    es_cursiva BOOLEAN DEFAULT FALSE,
    tiene_borde BOOLEAN DEFAULT FALSE,
    -- Metadata adicional
    fecha_deteccion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_archivo_campo UNIQUE(archivo_id, campo_estandar)
);

-- Índices para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_archivo_broker ON cotizaciones_archivos(broker_id);
CREATE INDEX IF NOT EXISTS idx_archivo_vessel ON cotizaciones_archivos(vessel_name);
CREATE INDEX IF NOT EXISTS idx_archivo_imo ON cotizaciones_archivos(imo_number);
CREATE INDEX IF NOT EXISTS idx_colores_archivo ON archivo_colores(archivo_id);
CREATE INDEX IF NOT EXISTS idx_colores_campo ON archivo_colores(campo_estandar);

-- Vista de archivos con información completa
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
    COUNT(ac.color_id) as total_columnas,
    COUNT(ac.color_fondo) FILTER (WHERE ac.color_fondo IS NOT NULL) as columnas_con_color_fondo,
    COUNT(ac.color_id) FILTER (WHERE ac.es_negrita = TRUE) as columnas_negrita
FROM cotizaciones_archivos ca
INNER JOIN brokers b ON ca.broker_id = b.broker_id
LEFT JOIN archivo_colores ac ON ca.archivo_id = ac.archivo_id
GROUP BY ca.archivo_id, b.broker_name, ca.nombre_archivo, 
         ca.vessel_name, ca.imo_number, ca.numero_cotizacion,
         ca.fecha_archivo, ca.fecha_procesado;

-- Vista de colores por archivo con detalles
CREATE OR REPLACE VIEW v_colores_por_archivo AS
SELECT 
    b.broker_name,
    ca.nombre_archivo,
    ca.vessel_name,
    ac.campo_estandar,
    ac.nombre_columna_original,
    ac.letra_columna,
    ac.color_fondo,
    ac.color_texto,
    ac.es_negrita,
    ac.es_cursiva,
    ac.tiene_borde,
    ac.fecha_deteccion
FROM archivo_colores ac
INNER JOIN cotizaciones_archivos ca ON ac.archivo_id = ca.archivo_id
INNER JOIN brokers b ON ca.broker_id = b.broker_id
ORDER BY b.broker_name, ca.nombre_archivo, ac.indice_columna;

-- Vista para comparar colores entre archivos del mismo broker
CREATE OR REPLACE VIEW v_comparacion_colores_broker AS
SELECT 
    b.broker_name,
    ac.campo_estandar,
    ac.letra_columna,
    COUNT(DISTINCT ac.color_fondo) as variaciones_fondo,
    COUNT(DISTINCT ac.color_texto) as variaciones_texto,
    STRING_AGG(DISTINCT ac.color_fondo, ', ' ORDER BY ac.color_fondo) as colores_fondo_encontrados,
    COUNT(ca.archivo_id) as total_archivos
FROM archivo_colores ac
INNER JOIN cotizaciones_archivos ca ON ac.archivo_id = ca.archivo_id
INNER JOIN brokers b ON ca.broker_id = b.broker_id
GROUP BY b.broker_name, ac.campo_estandar, ac.letra_columna
HAVING COUNT(DISTINCT ac.color_fondo) > 1  -- Solo mostrar donde hay variaciones
ORDER BY b.broker_name, ac.campo_estandar;

-- Comentarios
COMMENT ON TABLE cotizaciones_archivos IS 'Registro de cada archivo/planilla de cotización procesado';
COMMENT ON TABLE archivo_colores IS 'Colores y estilos específicos de cada archivo de cotización';
COMMENT ON VIEW v_comparacion_colores_broker IS 'Muestra variaciones de color entre archivos del mismo broker';
