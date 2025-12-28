package cl.vsschile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Genera un Excel específico para un broker con su formato y archivos
 */
public class BrokerExcelGenerator {
    
    private Connection conn;
    
    public BrokerExcelGenerator(String dbHost, int dbPort, String dbName, String dbUser, String dbPassword) 
            throws SQLException {
        String dbUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
        this.conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java BrokerExcelGenerator <nombre-broker> <archivo-salida.xlsx> [db-password]");
            System.out.println("\nEjemplo:");
            System.out.println("  mvn exec:java -Dexec.mainClass=\"cl.vsschile.BrokerExcelGenerator\" \\");
            System.out.println("    -Dexec.args=\"'MCTC MARINE LTD' mctc_reporte.xlsx\"");
            System.out.println("\nBrokers disponibles:");
            
            // Mostrar brokers disponibles
            try {
                BrokerExcelGenerator gen = new BrokerExcelGenerator(
                    "localhost", 5432, "sistema_cotizacion_2025", "postgres", ""
                );
                gen.listBrokers();
                gen.close();
            } catch (Exception e) {
                // Ignorar errores al listar
            }
            return;
        }
        
        String brokerName = args[0];
        String outputFile = args[1];
        String dbPassword = args.length > 2 ? args[2] : "";
        
        try {
            BrokerExcelGenerator generator = new BrokerExcelGenerator(
                "localhost", 5432, "sistema_cotizacion_2025", "postgres", dbPassword
            );
            
            System.out.println("==============================================");
            System.out.println("  GENERADOR EXCEL POR BROKER");
            System.out.println("==============================================\n");
            System.out.println("Broker: " + brokerName);
            System.out.println("Archivo: " + outputFile);
            System.out.println();
            
            generator.generateBrokerReport(brokerName, outputFile);
            
            System.out.println("\n✓ Excel generado exitosamente!");
            
            generator.close();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void listBrokers() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT broker_name FROM brokers ORDER BY broker_name"
        );
        
        while (rs.next()) {
            System.out.println("  - " + rs.getString("broker_name"));
        }
        
        rs.close();
        stmt.close();
    }
    
    public void generateBrokerReport(String brokerName, String outputFile) throws Exception {
        // Verificar que el broker existe
        if (!brokerExists(brokerName)) {
            throw new Exception("Broker no encontrado: " + brokerName);
        }
        
        XSSFWorkbook workbook = new XSSFWorkbook();
        Map<String, CellStyle> styles = createStyles(workbook);
        
        // 1. Hoja de información general del broker
        createInfoSheet(workbook, styles, brokerName);
        
        // 2. Hoja de formato/plantilla
        createFormatSheet(workbook, styles, brokerName);
        
        // 3. Hoja de archivos procesados
        createFilesListSheet(workbook, styles, brokerName);
        
        // 4. Una hoja por cada archivo con sus colores específicos
        createFileDetailSheets(workbook, styles, brokerName);
        
        // Guardar
        FileOutputStream fileOut = new FileOutputStream(outputFile);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        
        System.out.println("✓ Hojas creadas:");
        System.out.println("  - Información General");
        System.out.println("  - Formato Estándar");
        System.out.println("  - Lista de Archivos");
        System.out.println("  - Detalle de cada archivo");
    }
    
    private boolean brokerExists(String brokerName) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT COUNT(*) FROM brokers WHERE broker_name = ?"
        );
        ps.setString(1, brokerName);
        ResultSet rs = ps.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        ps.close();
        return count > 0;
    }
    
    private Map<String, CellStyle> createStyles(XSSFWorkbook workbook) {
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        
        // Header
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        styles.put("header", headerStyle);
        
        // Title
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFont(titleFont);
        styles.put("title", titleStyle);
        
        // Subtitle
        CellStyle subtitleStyle = workbook.createCellStyle();
        Font subtitleFont = workbook.createFont();
        subtitleFont.setBold(true);
        subtitleFont.setFontHeightInPoints((short) 12);
        subtitleStyle.setFont(subtitleFont);
        styles.put("subtitle", subtitleStyle);
        
        // Normal
        CellStyle normalStyle = workbook.createCellStyle();
        normalStyle.setBorderBottom(BorderStyle.THIN);
        normalStyle.setBorderTop(BorderStyle.THIN);
        normalStyle.setBorderLeft(BorderStyle.THIN);
        normalStyle.setBorderRight(BorderStyle.THIN);
        styles.put("normal", normalStyle);
        
        // Label (negrita para etiquetas)
        CellStyle labelStyle = workbook.createCellStyle();
        Font labelFont = workbook.createFont();
        labelFont.setBold(true);
        labelStyle.setFont(labelFont);
        styles.put("label", labelStyle);
        
        return styles;
    }
    
    private void createInfoSheet(XSSFWorkbook workbook, Map<String, CellStyle> styles, String brokerName) 
            throws SQLException {
        Sheet sheet = workbook.createSheet("Información General");
        
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(brokerName);
        titleCell.setCellStyle(styles.get("title"));
        rowNum++;
        
        // Obtener estadísticas
        PreparedStatement ps = conn.prepareStatement(
            "SELECT COUNT(*) as total_archivos, " +
            "SUM(total_columnas) as total_columnas, " +
            "SUM(columnas_con_color_fondo) as columnas_con_color " +
            "FROM v_archivos_cotizaciones WHERE broker_name = ?"
        );
        ps.setString(1, brokerName);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            Row row1 = sheet.createRow(rowNum++);
            row1.createCell(0).setCellValue("Total de Archivos Procesados:");
            row1.getCell(0).setCellStyle(styles.get("label"));
            row1.createCell(1).setCellValue(rs.getInt("total_archivos"));
            
            Row row2 = sheet.createRow(rowNum++);
            row2.createCell(0).setCellValue("Total de Columnas Detectadas:");
            row2.getCell(0).setCellStyle(styles.get("label"));
            row2.createCell(1).setCellValue(rs.getInt("total_columnas"));
            
            Row row3 = sheet.createRow(rowNum++);
            row3.createCell(0).setCellValue("Columnas con Color:");
            row3.getCell(0).setCellStyle(styles.get("label"));
            row3.createCell(1).setCellValue(rs.getInt("columnas_con_color"));
        }
        
        rs.close();
        ps.close();
        
        rowNum += 2;
        
        // Información del formato
        Row subtitleRow = sheet.createRow(rowNum++);
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue("FORMATO ESTÁNDAR DETECTADO");
        subtitleCell.setCellStyle(styles.get("subtitle"));
        rowNum++;
        
        ps = conn.prepareStatement(
            "SELECT header_row, version FROM v_formatos_activos WHERE broker_name = ?"
        );
        ps.setString(1, brokerName);
        rs = ps.executeQuery();
        
        if (rs.next()) {
            Row row1 = sheet.createRow(rowNum++);
            row1.createCell(0).setCellValue("Fila de Headers:");
            row1.getCell(0).setCellStyle(styles.get("label"));
            row1.createCell(1).setCellValue(rs.getInt("header_row") + 1);
            
            Row row2 = sheet.createRow(rowNum++);
            row2.createCell(0).setCellValue("Versión:");
            row2.getCell(0).setCellStyle(styles.get("label"));
            row2.createCell(1).setCellValue(rs.getString("version"));
        }
        
        rs.close();
        ps.close();
        
        // Auto-size
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
    
    private void createFormatSheet(XSSFWorkbook workbook, Map<String, CellStyle> styles, String brokerName) 
            throws SQLException {
        Sheet sheet = workbook.createSheet("Formato Estándar");
        
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("FORMATO/PLANTILLA: " + brokerName);
        titleRow.getCell(0).setCellStyle(styles.get("title"));
        rowNum++;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Campo Estándar", "Columna", "Nombre Original", 
                           "Color Fondo", "Color Texto", "Negrita", "Cursiva", "Borde"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // Datos
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM v_columnas_detalladas WHERE broker_name = ? ORDER BY indice_columna"
        );
        ps.setString(1, brokerName);
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(rs.getString("campo_estandar"));
            row.createCell(1).setCellValue(rs.getString("letra_columna"));
            row.createCell(2).setCellValue(rs.getString("nombre_columna_original"));
            row.createCell(3).setCellValue(rs.getString("color_fondo"));
            row.createCell(4).setCellValue(rs.getString("color_texto"));
            row.createCell(5).setCellValue(rs.getBoolean("es_negrita") ? "✓" : "");
            row.createCell(6).setCellValue(rs.getBoolean("es_cursiva") ? "✓" : "");
            row.createCell(7).setCellValue(rs.getBoolean("tiene_borde") ? "✓" : "");
            
            for (int i = 0; i < 8; i++) {
                row.getCell(i).setCellStyle(styles.get("normal"));
            }
        }
        
        rs.close();
        ps.close();
        
        // Auto-size
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createFilesListSheet(XSSFWorkbook workbook, Map<String, CellStyle> styles, String brokerName) 
            throws SQLException {
        Sheet sheet = workbook.createSheet("Lista de Archivos");
        
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("ARCHIVOS PROCESADOS: " + brokerName);
        titleRow.getCell(0).setCellStyle(styles.get("title"));
        rowNum++;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"#", "Nombre Archivo", "Vessel", "IMO", "Total Columnas", "Con Color"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // Datos
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM v_archivos_cotizaciones WHERE broker_name = ? ORDER BY nombre_archivo"
        );
        ps.setString(1, brokerName);
        ResultSet rs = ps.executeQuery();
        
        int fileNum = 1;
        while (rs.next()) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(fileNum++);
            row.createCell(1).setCellValue(rs.getString("nombre_archivo"));
            row.createCell(2).setCellValue(rs.getString("vessel_name"));
            row.createCell(3).setCellValue(rs.getString("imo_number"));
            row.createCell(4).setCellValue(rs.getInt("total_columnas"));
            row.createCell(5).setCellValue(rs.getInt("columnas_con_color_fondo"));
            
            for (int i = 0; i < 6; i++) {
                row.getCell(i).setCellStyle(styles.get("normal"));
            }
        }
        
        rs.close();
        ps.close();
        
        // Auto-size
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createFileDetailSheets(XSSFWorkbook workbook, Map<String, CellStyle> styles, String brokerName) 
            throws SQLException {
        // Obtener lista de archivos
        PreparedStatement ps = conn.prepareStatement(
            "SELECT nombre_archivo FROM cotizaciones_archivos ca " +
            "INNER JOIN brokers b ON ca.broker_id = b.broker_id " +
            "WHERE b.broker_name = ? ORDER BY nombre_archivo LIMIT 10"  // Límite de 10 para no sobrecargar
        );
        ps.setString(1, brokerName);
        ResultSet rs = ps.executeQuery();
        
        List<String> files = new ArrayList<String>();
        while (rs.next()) {
            files.add(rs.getString("nombre_archivo"));
        }
        rs.close();
        ps.close();
        
        // Crear hoja para cada archivo (máximo 10)
        int fileCount = 0;
        for (String fileName : files) {
            if (fileCount >= 10) break;  // Límite de hojas
            createFileDetailSheet(workbook, styles, brokerName, fileName, fileCount + 1);
            fileCount++;
        }
    }
    
    private void createFileDetailSheet(XSSFWorkbook workbook, Map<String, CellStyle> styles, 
                                      String brokerName, String fileName, int fileNum) throws SQLException {
        // Nombre de hoja válido
        String sheetName = "Archivo " + fileNum;
        Sheet sheet = workbook.createSheet(sheetName);
        
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("ARCHIVO: " + fileName);
        titleRow.getCell(0).setCellStyle(styles.get("subtitle"));
        rowNum++;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Campo", "Columna", "Nombre", "Color Fondo", "Color Texto", "Negrita"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // Datos
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM v_colores_por_archivo " +
            "WHERE broker_name = ? AND nombre_archivo = ? " +
            "ORDER BY letra_columna"
        );
        ps.setString(1, brokerName);
        ps.setString(2, fileName);
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(rs.getString("campo_estandar"));
            row.createCell(1).setCellValue(rs.getString("letra_columna"));
            row.createCell(2).setCellValue(rs.getString("nombre_columna_original"));
            row.createCell(3).setCellValue(rs.getString("color_fondo"));
            row.createCell(4).setCellValue(rs.getString("color_texto"));
            row.createCell(5).setCellValue(rs.getBoolean("es_negrita") ? "✓" : "");
            
            for (int i = 0; i < 6; i++) {
                row.getCell(i).setCellStyle(styles.get("normal"));
            }
        }
        
        rs.close();
        ps.close();
        
        // Auto-size
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            // Ignorar
        }
    }
}
