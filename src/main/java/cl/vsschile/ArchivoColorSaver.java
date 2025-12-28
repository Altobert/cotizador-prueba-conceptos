package cl.vsschile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Procesa TODOS los archivos de cotización y guarda sus colores individualmente
 */
public class ArchivoColorSaver {
    
    private Connection conn;
    
    public ArchivoColorSaver(String dbHost, int dbPort, String dbName, String dbUser, String dbPassword) 
            throws SQLException {
        String dbUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
        this.conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java ArchivoColorSaver <directorio-brokers> [db-password]");
            System.out.println("\nProcesa TODOS los archivos y guarda colores individuales");
            return;
        }
        
        String brokersPath = args[0];
        String dbPassword = args.length > 1 ? args[1] : "";
        
        try {
            ArchivoColorSaver saver = new ArchivoColorSaver(
                "localhost", 5432, "sistema_cotizacion_2025", "postgres", dbPassword
            );
            
            System.out.println("==============================================");
            System.out.println("  PROCESAR ARCHIVOS CON COLORES");
            System.out.println("==============================================\n");
            
            File brokersDir = new File(brokersPath);
            if (!brokersDir.exists()) {
                System.err.println("Error: El directorio no existe");
                return;
            }
            
            saver.processAllBrokers(brokersDir);
            saver.printSummary();
            
            saver.close();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void processAllBrokers(File brokersDir) throws Exception {
        File[] brokerDirs = brokersDir.listFiles();
        if (brokerDirs == null) return;
        
        int totalFiles = 0;
        int processed = 0;
        
        for (File brokerDir : brokerDirs) {
            if (brokerDir.isDirectory()) {
                String brokerName = brokerDir.getName();
                System.out.println("\n▶ Procesando broker: " + brokerName);
                
                int brokerId = getOrCreateBroker(brokerName);
                
                File[] files = brokerDir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        String lower = name.toLowerCase();
                        return lower.endsWith(".xlsx") || lower.endsWith(".xls");
                    }
                });
                
                if (files != null) {
                    totalFiles += files.length;
                    System.out.println("  Archivos encontrados: " + files.length);
                    
                    for (File file : files) {
                        try {
                            processFile(file, brokerId, brokerName);
                            processed++;
                            System.out.print(".");
                            if (processed % 50 == 0) System.out.println();
                        } catch (Exception e) {
                            System.err.println("\n  Error en " + file.getName() + ": " + e.getMessage());
                        }
                    }
                    System.out.println();
                }
            }
        }
        
        System.out.println("\n✓ Procesados: " + processed + " de " + totalFiles + " archivos");
    }
    
    private void processFile(File file, int brokerId, String brokerName) throws Exception {
        // Detectar columnas y colores
        ColumnDetector.ColumnMapping mapping = detectColumnsFromFile(file, brokerName);
        
        if (!mapping.isValid()) {
            return;
        }
        
        // Extraer información del vessel/IMO si existe
        QuotationInfo quotInfo = extractQuotationInfo(file, brokerName);
        
        // Guardar archivo en BD
        int archivoId = saveArchivo(brokerId, file.getName(), quotInfo);
        
        // Guardar colores del archivo
        saveColores(archivoId, mapping);
    }
    
    private ColumnDetector.ColumnMapping detectColumnsFromFile(File file, String brokerName) 
            throws Exception {
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = null;
        
        try {
            if (file.getName().toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else {
                workbook = new HSSFWorkbook(fis);
            }
            
            Sheet sheet = workbook.getSheetAt(0);
            return ColumnDetector.detectColumns(sheet, brokerName);
            
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            fis.close();
        }
    }
    
    private QuotationInfo extractQuotationInfo(File file, String brokerName) throws Exception {
        // Aquí podrías reutilizar la lógica de CotizacionOrganizer
        // Por ahora solo retornamos info básica
        QuotationInfo info = new QuotationInfo();
        info.nombreArchivo = file.getName();
        return info;
    }
    
    private int getOrCreateBroker(String brokerName) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            // Intentar obtener
            ps = conn.prepareStatement("SELECT broker_id FROM brokers WHERE broker_name = ?");
            ps.setString(1, brokerName);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("broker_id");
            }
            
            // Crear si no existe
            ps = conn.prepareStatement(
                "INSERT INTO brokers (broker_name) VALUES (?) RETURNING broker_id"
            );
            ps.setString(1, brokerName);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            throw new SQLException("No se pudo crear broker");
            
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
    }
    
    private int saveArchivo(int brokerId, String nombreArchivo, QuotationInfo info) 
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            // Verificar si ya existe
            ps = conn.prepareStatement(
                "SELECT archivo_id FROM cotizaciones_archivos " +
                "WHERE broker_id = ? AND nombre_archivo = ?"
            );
            ps.setInt(1, brokerId);
            ps.setString(2, nombreArchivo);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                int archivoId = rs.getInt("archivo_id");
                // Eliminar colores antiguos
                deleteColores(archivoId);
                return archivoId;
            }
            
            // Crear nuevo
            ps = conn.prepareStatement(
                "INSERT INTO cotizaciones_archivos " +
                "(broker_id, nombre_archivo, vessel_name, imo_number) " +
                "VALUES (?, ?, ?, ?) RETURNING archivo_id"
            );
            ps.setInt(1, brokerId);
            ps.setString(2, nombreArchivo);
            ps.setString(3, info.vesselName);
            ps.setString(4, info.imoNumber);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            throw new SQLException("No se pudo crear archivo");
            
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
    }
    
    private void deleteColores(int archivoId) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("DELETE FROM archivo_colores WHERE archivo_id = ?");
            ps.setInt(1, archivoId);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
        }
    }
    
    private void saveColores(int archivoId, ColumnDetector.ColumnMapping mapping) 
            throws SQLException {
        PreparedStatement ps = null;
        
        try {
            ps = conn.prepareStatement(
                "INSERT INTO archivo_colores " +
                "(archivo_id, campo_estandar, nombre_columna_original, indice_columna, " +
                "letra_columna, color_fondo, color_texto, es_negrita, es_cursiva, tiene_borde) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            
            for (Map.Entry<String, Integer> entry : mapping.columns.entrySet()) {
                String campoEstandar = entry.getKey();
                int indiceColumna = entry.getValue();
                String nombreOriginal = indiceColumna < mapping.columnNames.size() ? 
                    mapping.columnNames.get(indiceColumna) : null;
                String letraColumna = getColumnLetter(indiceColumna);
                CellStyleInfo styleInfo = mapping.columnStyles.get(indiceColumna);
                
                ps.setInt(1, archivoId);
                ps.setString(2, campoEstandar);
                ps.setString(3, nombreOriginal);
                ps.setInt(4, indiceColumna);
                ps.setString(5, letraColumna);
                
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
            if (ps != null) ps.close();
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
    
    private void printSummary() throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            
            System.out.println("\n==============================================");
            System.out.println("  RESUMEN");
            System.out.println("==============================================\n");
            
            rs = stmt.executeQuery(
                "SELECT broker_name, COUNT(*) as total_archivos, " +
                "SUM(columnas_con_color_fondo) as archivos_con_colores " +
                "FROM v_archivos_cotizaciones " +
                "GROUP BY broker_name ORDER BY broker_name"
            );
            
            while (rs.next()) {
                System.out.println(String.format("%-30s: %3d archivos (%d con colores)",
                    rs.getString("broker_name"),
                    rs.getInt("total_archivos"),
                    rs.getInt("archivos_con_colores")));
            }
            
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }
    
    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            // Ignorar
        }
    }
    
    private static class QuotationInfo {
        String nombreArchivo;
        String vesselName;
        String imoNumber;
    }
}
