package cl.vsschile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Registra el formato completo de BSM CATERING en la base de datos
 * Incluye columnas y metadata basado en el análisis del archivo Excel original
 */
public class BSMFormatRegistrar {
    
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/sistema_cotizacion_2025";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "";
    
    private static final int BROKER_ID = 6; // BSM CATERING
    private static final int FORMATO_ID = 7; // Formato existente
    
    public static void main(String[] args) {
        BSMFormatRegistrar registrar = new BSMFormatRegistrar();
        
        try {
            registrar.registrarFormatoCompleto();
            System.out.println("✅ Formato BSM CATERING registrado exitosamente");
        } catch (SQLException e) {
            System.err.println("❌ Error al registrar formato: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void registrarFormatoCompleto() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);
            
            try {
                // 1. Verificar/actualizar formato
                actualizarFormato(conn);
                
                // 2. Limpiar columnas existentes
                limpiarColumnas(conn);
                
                // 3. Registrar columnas
                registrarColumnas(conn);
                
                // 4. Limpiar metadata existente
                limpiarMetadata(conn);
                
                // 5. Registrar metadata
                registrarMetadata(conn);
                
                conn.commit();
                
                System.out.println("\n=== RESUMEN ===");
                System.out.println("Broker ID: " + BROKER_ID);
                System.out.println("Formato ID: " + FORMATO_ID);
                System.out.println("Header Row: 19 (0-based: 18)");
                
                // Contar registros
                int numColumnas = contarRegistros(conn, "formato_columnas", FORMATO_ID);
                int numMetadata = contarRegistros(conn, "broker_metadata", FORMATO_ID);
                
                System.out.println("Columnas registradas: " + numColumnas);
                System.out.println("Metadata registrada: " + numMetadata);
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
    
    private void actualizarFormato(Connection conn) throws SQLException {
        String sql = "UPDATE broker_formatos SET " +
                    "header_row = 18, " +
                    "descripcion = 'Formato BSM CATERING - Request for Quote', " +
                    "archivo_ejemplo = 'RFQ_EQ_SCF_BAA_0031.xlsm', " +
                    "fecha_actualizacion = CURRENT_TIMESTAMP " +
                    "WHERE formato_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, FORMATO_ID);
            stmt.executeUpdate();
            System.out.println("✓ Formato actualizado");
        }
    }
    
    private void limpiarColumnas(Connection conn) throws SQLException {
        String sql = "DELETE FROM formato_columnas WHERE formato_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, FORMATO_ID);
            int deleted = stmt.executeUpdate();
            System.out.println("✓ Columnas anteriores eliminadas: " + deleted);
        }
    }
    
    private void limpiarMetadata(Connection conn) throws SQLException {
        String sql = "DELETE FROM broker_metadata WHERE formato_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, FORMATO_ID);
            int deleted = stmt.executeUpdate();
            System.out.println("✓ Metadata anterior eliminada: " + deleted);
        }
    }
    
    private void registrarColumnas(Connection conn) throws SQLException {
        String sql = "INSERT INTO formato_columnas " +
                    "(formato_id, campo_estandar, nombre_columna_original, indice_columna, letra_columna, " +
                    "color_fondo, color_texto, requerido, tipo_dato) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        List<ColumnaInfo> columnas = obtenerColumnasInfo();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (ColumnaInfo col : columnas) {
                stmt.setInt(1, FORMATO_ID);
                stmt.setString(2, col.campoEstandar);
                stmt.setString(3, col.nombreColumna);
                stmt.setInt(4, col.indice);
                stmt.setString(5, col.letra);
                stmt.setString(6, col.colorFondo);
                stmt.setString(7, col.colorTexto);
                stmt.setBoolean(8, col.esObligatorio);
                stmt.setString(9, col.esNumerico ? "NUMBER" : "TEXT");
                stmt.executeUpdate();
            }
            System.out.println("✓ Columnas registradas: " + columnas.size());
        }
    }
    
    private void registrarMetadata(Connection conn) throws SQLException {
        String sql = "INSERT INTO broker_metadata " +
                    "(formato_id, seccion, campo_nombre, campo_valor, fila_origen, letra_columna) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        List<MetadataInfo> metadataList = obtenerMetadataInfo();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (MetadataInfo meta : metadataList) {
                stmt.setInt(1, FORMATO_ID);
                stmt.setString(2, meta.seccion);
                stmt.setString(3, meta.campoNombre);
                stmt.setString(4, meta.campoValor);
                stmt.setInt(5, meta.filaExcel);
                stmt.setString(6, meta.columnaExcel);
                stmt.executeUpdate();
            }
            System.out.println("✓ Metadata registrada: " + metadataList.size());
        }
    }
    
    private List<ColumnaInfo> obtenerColumnasInfo() {
        List<ColumnaInfo> columnas = new ArrayList<>();
        
        // Header color: #9DBEC3 (background), #1F497D (text)
        String headerBg = "#9DBEC3";
        String headerTxt = "#1F497D";
        
        // Fila 19 (0-based: 18) - Columnas del header
        columnas.add(new ColumnaInfo("ITEM_NUMBER", "No", 0, "A", headerBg, headerTxt, false, true, false));
        columnas.add(new ColumnaInfo("PRODUCT_CODE", "Product Code", 2, "C", headerBg, headerTxt, false, false, false));
        columnas.add(new ColumnaInfo("DESCRIPTION", "Description", 3, "D", headerBg, headerTxt, true, false, false));
        columnas.add(new ColumnaInfo("PART_NUMBER", "Part Number", 4, "E", headerBg, headerTxt, false, false, false));
        columnas.add(new ColumnaInfo("BRAND", "Brand", 5, "F", headerBg, headerTxt, false, false, false));
        columnas.add(new ColumnaInfo("WEIGHT", "Weight", 6, "G", headerBg, headerTxt, false, true, false));
        columnas.add(new ColumnaInfo("UOM", "Unit", 7, "H", headerBg, headerTxt, false, false, false));
        columnas.add(new ColumnaInfo("PACKAGE", "Package", 8, "I", headerBg, headerTxt, false, false, false));
        columnas.add(new ColumnaInfo("CONTRACT_PRICE", "Contract Price", 9, "J", headerBg, headerTxt, false, true, false));
        columnas.add(new ColumnaInfo("QUANTITY", "Quantity", 10, "K", headerBg, headerTxt, true, true, false));
        columnas.add(new ColumnaInfo("UNIT_PRICE", "Unit Price ( USD )", 11, "L", headerBg, headerTxt, false, true, false));
        // Nota: L y M están mezcladas para "Unit Price ( USD )"
        columnas.add(new ColumnaInfo("DISCOUNT", "Discount %", 13, "N", headerBg, headerTxt, false, true, false));
        columnas.add(new ColumnaInfo("VAT", "VAT %", 14, "O", headerBg, headerTxt, false, true, false));
        columnas.add(new ColumnaInfo("TOTAL", "Total Price", 15, "P", headerBg, headerTxt, false, true, false));
        columnas.add(new ColumnaInfo("MD", "MD", 16, "Q", headerBg, headerTxt, false, false, false));
        columnas.add(new ColumnaInfo("SDOC", "sDoC", 17, "R", headerBg, headerTxt, false, false, false));
        columnas.add(new ColumnaInfo("VENDOR_REMARKS", "Vendor Remarks", 18, "S", headerBg, headerTxt, false, false, false));
        // Nota: S y T están mezcladas para "Vendor Remarks"
        columnas.add(new ColumnaInfo("OFFICE_REMARKS", "Office Remarks", 20, "U", headerBg, headerTxt, false, false, false));
        // Nota: U y V están mezcladas para "Office Remarks"
        
        // Columna B (índice 1) tiene datos pero no header visible
        columnas.add(new ColumnaInfo("INTERNAL_CODE", "Internal Code", 1, "B", headerBg, headerTxt, false, true, false));
        
        return columnas;
    }
    
    private List<MetadataInfo> obtenerMetadataInfo() {
        List<MetadataInfo> metadata = new ArrayList<>();
        int orden = 1;
        
        // === COMPANY DETAILS (Filas 3-11, Columnas A-L) ===
        metadata.add(new MetadataInfo("Company Details", "Section Title", "Vessel \\ Company Details", 
                                      3, "A", "TEXT", true, orden++));
        
        metadata.add(new MetadataInfo("Company Details", "Company Name Line 1", "BERNHARD SCHULTE SHIPMANAGEMENT (HONG KONG) LIMITED PARTNERS", 
                                      4, "A", "TEXT", true, orden++));
        
        metadata.add(new MetadataInfo("Company Details", "Company Name Line 2", "BSM - CATERING SERVICES,", 
                                      5, "A", "TEXT", true, orden++));
        
        metadata.add(new MetadataInfo("Company Details", "Company Name Line 3", "BERNHARD SCHULTE SHIPMANAGEMENT (L) LTD.,", 
                                      6, "A", "TEXT", true, orden++));
        
        metadata.add(new MetadataInfo("Company Details", "Care Of", "C\\O Bernhard Schulte Shipmanagement (India) Pvt. Limited", 
                                      7, "A", "TEXT", false, orden++));
        
        metadata.add(new MetadataInfo("Company Details", "Address", "401 Olympia, Hiranandani Gardens, Powai, Mumbai 400 076, India", 
                                      8, "A", "TEXT", false, orden++));
        
        metadata.add(new MetadataInfo("Company Details", "Contact", "Tel: +91-22-40017300        Fax: +91-22-40017555", 
                                      9, "A", "TEXT", false, orden++));
        
        metadata.add(new MetadataInfo("Company Details", "Email", "seachef@seachef.com", 
                                      10, "C", "EMAIL", false, orden++));
        
        metadata.add(new MetadataInfo("Company Details", "Website", "www.seachef.com /  www.bs-shipmanagement.com", 
                                      11, "C", "TEXT", false, orden++));
        
        // === RFQ INFORMATION (Filas 4-17, Columnas M-V) ===
        metadata.add(new MetadataInfo("RFQ Information", "Vessel", "Barism Alice", 
                                      4, "O", "TEXT", true, orden++));
        
        metadata.add(new MetadataInfo("RFQ Information", "RFQ Number", "EQ/SCF/BAA/0031", 
                                      5, "O", "TEXT", true, orden++));
        
        metadata.add(new MetadataInfo("RFQ Information", "RFQ Date", "2025-12-11", 
                                      6, "O", "DATE", true, orden++));
        
        metadata.add(new MetadataInfo("RFQ Information", "Submit Quote Before", "2025-12-11", 
                                      8, "O", "DATE", true, orden++));
        
        metadata.add(new MetadataInfo("RFQ Information", "Port of Delivery", "Port of Delivery", 
                                      9, "L", "TEXT", false, orden++));
        
        metadata.add(new MetadataInfo("RFQ Information", "Vessel ETA", "2025-12-12 12:00", 
                                      10, "O", "DATETIME", false, orden++));
        
        metadata.add(new MetadataInfo("RFQ Information", "Payment Terms", "Credit", 
                                      11, "O", "TEXT", false, orden++));
        
        metadata.add(new MetadataInfo("RFQ Information", "Payment Days", "9", 
                                      11, "S", "NUMBER", false, orden++));
        
        metadata.add(new MetadataInfo("RFQ Information", "Vendor Reference", "Vendor Reference ", 
                                      12, "M", "TEXT", false, orden++));
        
        metadata.add(new MetadataInfo("RFQ Information", "Delivery Term", "Free On Board", 
                                      13, "O", "TEXT", false, orden++));
        
        metadata.add(new MetadataInfo("RFQ Information", "Currency", "USD", 
                                      14, "O", "TEXT", true, orden++));
        
        metadata.add(new MetadataInfo("RFQ Information", "Discount Percentage", "0", 
                                      15, "O", "NUMBER", false, orden++));
        
        metadata.add(new MetadataInfo("RFQ Information", "VAT Percentage", "0", 
                                      16, "O", "NUMBER", false, orden++));
        
        metadata.add(new MetadataInfo("RFQ Information", "Place (City)", "Place (CITY)", 
                                      17, "M", "TEXT", false, orden++));
        
        // === VENDOR DETAILS (Filas 12-17, Columnas A-L) ===
        metadata.add(new MetadataInfo("Vendor Details", "Section Title", "Vendor Details", 
                                      12, "A", "TEXT", true, orden++));
        
        metadata.add(new MetadataInfo("Vendor Details", "Vendor Name", "Valparaiso Ship Services S.A.", 
                                      13, "D", "TEXT", true, orden++));
        
        metadata.add(new MetadataInfo("Vendor Details", "Vendor Address", "Calle Tercera N°820 , Placilla, Valparaiso", 
                                      14, "D", "TEXT", false, orden++));
        
        metadata.add(new MetadataInfo("Vendor Details", "Vendor City", "City", 
                                      15, "A", "TEXT", false, orden++));
        
        metadata.add(new MetadataInfo("Vendor Details", "Vendor Phone", "56 32 2298972", 
                                      16, "D", "TEXT", false, orden++));
        
        metadata.add(new MetadataInfo("Vendor Details", "Vendor Email", "vss@vsschile.cl", 
                                      17, "D", "EMAIL", false, orden++));
        
        return metadata;
    }
    
    private int contarRegistros(Connection conn, String tabla, int formatoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tabla + " WHERE formato_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, formatoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    // Clases auxiliares
    static class ColumnaInfo {
        String campoEstandar;
        String nombreColumna;
        int indice;
        String letra;
        String colorFondo;
        String colorTexto;
        boolean esObligatorio;
        boolean esNumerico;
        boolean esFecha;
        
        ColumnaInfo(String campoEstandar, String nombreColumna, int indice, String letra,
                   String colorFondo, String colorTexto, boolean esObligatorio, 
                   boolean esNumerico, boolean esFecha) {
            this.campoEstandar = campoEstandar;
            this.nombreColumna = nombreColumna;
            this.indice = indice;
            this.letra = letra;
            this.colorFondo = colorFondo;
            this.colorTexto = colorTexto;
            this.esObligatorio = esObligatorio;
            this.esNumerico = esNumerico;
            this.esFecha = esFecha;
        }
    }
    
    static class MetadataInfo {
        String seccion;
        String campoNombre;
        String campoValor;
        int filaExcel;
        String columnaExcel;
        String tipoDato;
        boolean esObligatorio;
        int ordenVisualizacion;
        
        MetadataInfo(String seccion, String campoNombre, String campoValor, 
                    int filaExcel, String columnaExcel, String tipoDato,
                    boolean esObligatorio, int ordenVisualizacion) {
            this.seccion = seccion;
            this.campoNombre = campoNombre;
            this.campoValor = campoValor;
            this.filaExcel = filaExcel;
            this.columnaExcel = columnaExcel;
            this.tipoDato = tipoDato;
            this.esObligatorio = esObligatorio;
            this.ordenVisualizacion = ordenVisualizacion;
        }
    }
}
