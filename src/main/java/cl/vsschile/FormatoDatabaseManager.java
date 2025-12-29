package cl.vsschile;

import java.sql.*;
import java.util.*;

/**
 * Gestor de base de datos para almacenar formatos de cotizaciones por broker
 */
public class FormatoDatabaseManager {
    
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    
    public FormatoDatabaseManager(String host, int port, String database, String user, String password) {
        this.dbUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        this.dbUser = user;
        this.dbPassword = password;
    }
    
    public FormatoDatabaseManager() {
        // Valores por defecto
        this("localhost", 5432, "sistema_cotizacion_2025", "postgres", "");
    }
    
    /**
     * Obtiene una conexión a la base de datos
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
    
    /**
     * Prueba la conexión a la base de datos
     */
    public boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error conectando a la base de datos: " + e.getMessage());
            return false;
        } finally {
            closeConnection(conn);
        }
    }
    
    /**
     * Guarda o actualiza un broker en la base de datos
     */
    public int saveBroker(String brokerName, String descripcion) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Verificar si ya existe
            ps = conn.prepareStatement(
                "SELECT broker_id FROM brokers WHERE broker_name = ?"
            );
            ps.setString(1, brokerName);
            rs = ps.getResultSet();
            
            if (rs.next()) {
                return rs.getInt("broker_id");
            }
            
            // Insertar nuevo broker
            ps = conn.prepareStatement(
                "INSERT INTO brokers (broker_name, descripcion) VALUES (?, ?) RETURNING broker_id"
            );
            ps.setString(1, brokerName);
            ps.setString(2, descripcion);
            
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            throw new SQLException("No se pudo crear el broker");
            
        } finally {
            closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Guarda un formato de broker detectado
     */
    public int saveFormato(ColumnDetector.ColumnMapping mapping, String archivoEjemplo) 
            throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            // 1. Obtener o crear broker
            int brokerId = getBrokerIdByName(conn, mapping.brokerName);
            if (brokerId == -1) {
                brokerId = insertBroker(conn, mapping.brokerName);
            }
            
            // 2. Verificar si ya existe este formato
            int formatoId = getFormatoId(conn, brokerId, "1.0");
            if (formatoId != -1) {
                // Actualizar formato existente
                updateFormato(conn, formatoId, mapping.headerRow, archivoEjemplo);
                // Eliminar columnas antiguas
                deleteFormatoColumnas(conn, formatoId);
            } else {
                // Crear nuevo formato
                formatoId = insertFormato(conn, brokerId, "1.0", mapping.headerRow, archivoEjemplo);
            }
            
            // 3. Insertar columnas
            insertColumnas(conn, formatoId, mapping);
            
            conn.commit();
            return formatoId;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Ignorar
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
            closeResources(conn, ps, rs);
        }
    }
    
    private int getBrokerIdByName(Connection conn, String brokerName) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            ps = conn.prepareStatement("SELECT broker_id FROM brokers WHERE broker_name = ?");
            ps.setString(1, brokerName);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("broker_id");
            }
            return -1;
        } finally {
            closeStatement(ps);
            closeResultSet(rs);
        }
    }
    
    private int insertBroker(Connection conn, String brokerName) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            ps = conn.prepareStatement(
                "INSERT INTO brokers (broker_name) VALUES (?) RETURNING broker_id"
            );
            ps.setString(1, brokerName);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("No se pudo insertar el broker");
        } finally {
            closeStatement(ps);
            closeResultSet(rs);
        }
    }
    
    private int getFormatoId(Connection conn, int brokerId, String version) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            ps = conn.prepareStatement(
                "SELECT formato_id FROM broker_formatos WHERE broker_id = ? AND version = ?"
            );
            ps.setInt(1, brokerId);
            ps.setString(2, version);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("formato_id");
            }
            return -1;
        } finally {
            closeStatement(ps);
            closeResultSet(rs);
        }
    }
    
    private int insertFormato(Connection conn, int brokerId, String version, 
                             int headerRow, String archivoEjemplo) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            ps = conn.prepareStatement(
                "INSERT INTO broker_formatos (broker_id, version, header_row, archivo_ejemplo) " +
                "VALUES (?, ?, ?, ?) RETURNING formato_id"
            );
            ps.setInt(1, brokerId);
            ps.setString(2, version);
            ps.setInt(3, headerRow);
            ps.setString(4, archivoEjemplo);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("No se pudo insertar el formato");
        } finally {
            closeStatement(ps);
            closeResultSet(rs);
        }
    }
    
    private void updateFormato(Connection conn, int formatoId, int headerRow, 
                              String archivoEjemplo) throws SQLException {
        PreparedStatement ps = null;
        
        try {
            ps = conn.prepareStatement(
                "UPDATE broker_formatos SET header_row = ?, archivo_ejemplo = ?, " +
                "fecha_actualizacion = CURRENT_TIMESTAMP WHERE formato_id = ?"
            );
            ps.setInt(1, headerRow);
            ps.setString(2, archivoEjemplo);
            ps.setInt(3, formatoId);
            ps.executeUpdate();
        } finally {
            closeStatement(ps);
        }
    }
    
    private void deleteFormatoColumnas(Connection conn, int formatoId) throws SQLException {
        PreparedStatement ps = null;
        
        try {
            ps = conn.prepareStatement("DELETE FROM formato_columnas WHERE formato_id = ?");
            ps.setInt(1, formatoId);
            ps.executeUpdate();
        } finally {
            closeStatement(ps);
        }
    }
    
    private void insertColumnas(Connection conn, int formatoId, 
                               ColumnDetector.ColumnMapping mapping) throws SQLException {
        PreparedStatement ps = null;
        
        try {
            ps = conn.prepareStatement(
                "INSERT INTO formato_columnas " +
                "(formato_id, campo_estandar, nombre_columna_original, indice_columna, letra_columna, " +
                "color_fondo, color_texto, es_negrita, es_cursiva, tiene_borde) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            
            for (Map.Entry<String, Integer> entry : mapping.columns.entrySet()) {
                String campoEstandar = entry.getKey();
                int indiceColumna = entry.getValue();
                String nombreOriginal = indiceColumna < mapping.columnNames.size() ? 
                    mapping.columnNames.get(indiceColumna) : null;
                String letraColumna = getColumnLetter(indiceColumna);
                
                // Obtener información de estilo
                CellStyleInfo styleInfo = mapping.columnStyles.get(indiceColumna);
                
                ps.setInt(1, formatoId);
                ps.setString(2, campoEstandar);
                ps.setString(3, nombreOriginal);
                ps.setInt(4, indiceColumna);
                ps.setString(5, letraColumna);
                
                // Guardar colores y estilos
                if (styleInfo != null) {
                    ps.setString(6, styleInfo.backgroundColor);
                    ps.setString(7, styleInfo.foregroundColor);
                    ps.setBoolean(8, styleInfo.isBold);
                    ps.setBoolean(9, styleInfo.isItalic);
                    ps.setBoolean(10, styleInfo.hasBorder);
                } else {
                    ps.setString(6, null);
                    ps.setString(7, null);
                    ps.setBoolean(8, false);
                    ps.setBoolean(9, false);
                    ps.setBoolean(10, false);
                }
                
                ps.addBatch();
            }
            
            ps.executeBatch();
        } finally {
            closeStatement(ps);
        }
    }
    
    /**
     * Obtiene todos los formatos activos
     */
    public List<FormatoInfo> getFormatosActivos() throws SQLException {
        List<FormatoInfo> formatos = new ArrayList<FormatoInfo>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM v_formatos_activos ORDER BY broker_name");
            
            while (rs.next()) {
                FormatoInfo info = new FormatoInfo();
                info.formatoId = rs.getInt("formato_id");
                info.brokerId = rs.getInt("broker_id");
                info.brokerName = rs.getString("broker_name");
                info.version = rs.getString("version");
                info.headerRow = rs.getInt("header_row");
                info.descripcion = rs.getString("formato_descripcion");
                info.totalColumnas = rs.getInt("total_columnas");
                formatos.add(info);
            }
            
            return formatos;
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Obtiene las columnas de un formato específico
     */
    public List<ColumnaInfo> getColumnasByFormato(int formatoId) throws SQLException {
        List<ColumnaInfo> columnas = new ArrayList<ColumnaInfo>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            ps = conn.prepareStatement(
                "SELECT * FROM formato_columnas WHERE formato_id = ? ORDER BY indice_columna"
            );
            ps.setInt(1, formatoId);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                ColumnaInfo info = new ColumnaInfo();
                info.columnaId = rs.getInt("columna_id");
                info.formatoId = rs.getInt("formato_id");
                info.campoEstandar = rs.getString("campo_estandar");
                info.nombreOriginal = rs.getString("nombre_columna_original");
                info.indiceColumna = rs.getInt("indice_columna");
                info.letraColumna = rs.getString("letra_columna");
                info.tipoDato = rs.getString("tipo_dato");
                info.requerido = rs.getBoolean("requerido");
                info.colorFondo = rs.getString("color_fondo");
                info.colorTexto = rs.getString("color_texto");
                info.esNegrita = rs.getBoolean("es_negrita");
                info.esCursiva = rs.getBoolean("es_cursiva");
                info.tieneBorde = rs.getBoolean("tiene_borde");
                columnas.add(info);
            }
            
            return columnas;
        } finally {
            closeResources(conn, ps, rs);
        }
    }
    
    private String getColumnLetter(int columnIndex) {
        StringBuilder columnName = new StringBuilder();
        while (columnIndex >= 0) {
            columnName.insert(0, (char) ('A' + (columnIndex % 26)));
            columnIndex = (columnIndex / 26) - 1;
        }
        return columnName.toString();
    }
    
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Ignorar
            }
        }
    }
    
    private void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                // Ignorar
            }
        }
    }
    
    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // Ignorar
            }
        }
    }
    
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }
    
    /**
     * Guarda metadata de un broker
     */
    public void saveMetadata(int formatoId, List<BrokerMetadataExtractor.MetadataField> metadata) 
            throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            // Eliminar metadata anterior de este formato
            ps = conn.prepareStatement("DELETE FROM broker_metadata WHERE formato_id = ?");
            ps.setInt(1, formatoId);
            ps.executeUpdate();
            ps.close();
            
            // Insertar nueva metadata
            ps = conn.prepareStatement(
                "INSERT INTO broker_metadata (formato_id, seccion, campo_nombre, campo_valor, " +
                "fila_origen, columna_origen, letra_columna) VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            
            for (BrokerMetadataExtractor.MetadataField field : metadata) {
                ps.setInt(1, formatoId);
                ps.setString(2, field.seccion);
                ps.setString(3, field.campoNombre);
                ps.setString(4, field.campoValor);
                ps.setInt(5, field.filaOrigen);
                ps.setInt(6, field.columnaOrigen);
                ps.setString(7, field.letraColumna);
                ps.addBatch();
            }
            
            ps.executeBatch();
            conn.commit();
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Ignorar
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
            closeStatement(ps);
            closeConnection(conn);
        }
    }
    
    /**
     * Obtiene la metadata de un formato
     */
    public List<MetadataInfo> getMetadataByFormato(int formatoId) throws SQLException {
        List<MetadataInfo> metadata = new ArrayList<MetadataInfo>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            ps = conn.prepareStatement(
                "SELECT * FROM broker_metadata WHERE formato_id = ? " +
                "ORDER BY seccion, fila_origen, columna_origen"
            );
            ps.setInt(1, formatoId);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                MetadataInfo info = new MetadataInfo();
                info.metadataId = rs.getInt("metadata_id");
                info.formatoId = rs.getInt("formato_id");
                info.seccion = rs.getString("seccion");
                info.campoNombre = rs.getString("campo_nombre");
                info.campoValor = rs.getString("campo_valor");
                info.filaOrigen = rs.getInt("fila_origen");
                info.columnaOrigen = rs.getInt("columna_origen");
                info.letraColumna = rs.getString("letra_columna");
                metadata.add(info);
            }
            
            return metadata;
        } finally {
            closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Clase para información de formato
     */
    public static class FormatoInfo {
        public int formatoId;
        public int brokerId;
        public String brokerName;
        public String version;
        public int headerRow;
        public String descripcion;
        public int totalColumnas;
        
        public String toString() {
            return String.format("%s (v%s) - Header: fila %d, Columnas: %d",
                brokerName, version, headerRow + 1, totalColumnas);
        }
    }
    
    /**
     * Clase para información de columna
     */
    public static class ColumnaInfo {
        public int columnaId;
        public int formatoId;
        public String campoEstandar;
        public String nombreOriginal;
        public int indiceColumna;
        public String letraColumna;
        public String tipoDato;
        public boolean requerido;
        public String colorFondo;
        public String colorTexto;
        public boolean esNegrita;
        public boolean esCursiva;
        public boolean tieneBorde;
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-20s -> %s (%d) \"%s\"",
                campoEstandar, letraColumna, indiceColumna + 1, nombreOriginal));
            
            if (colorFondo != null || colorTexto != null || esNegrita || esCursiva) {
                sb.append(" [");
                if (colorFondo != null) sb.append("BG:").append(colorFondo).append(" ");
                if (colorTexto != null) sb.append("FG:").append(colorTexto).append(" ");
                if (esNegrita) sb.append("Bold ");
                if (esCursiva) sb.append("Italic ");
                if (tieneBorde) sb.append("Border");
                sb.append("]");
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Clase para información de metadata
     */
    public static class MetadataInfo {
        public int metadataId;
        public int formatoId;
        public String seccion;
        public String campoNombre;
        public String campoValor;
        public int filaOrigen;
        public int columnaOrigen;
        public String letraColumna;
        
        public String toString() {
            return String.format("[%s] %s = %s (Fila: %d, Col: %s)",
                seccion, campoNombre, campoValor, filaOrigen + 1, letraColumna);
        }
    }
}
