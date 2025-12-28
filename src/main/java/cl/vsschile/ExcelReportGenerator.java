package cl.vsschile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Genera reportes Excel con información de formatos y colores por broker
 */
public class ExcelReportGenerator {
    
    private Connection conn;
    
    public ExcelReportGenerator(String dbHost, int dbPort, String dbName, String dbUser, String dbPassword) 
            throws SQLException {
        String dbUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
        this.conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java ExcelReportGenerator <archivo-salida.xlsx> [db-password]");
            System.out.println("\nEjemplo:");
            System.out.println("  mvn exec:java -Dexec.mainClass=\"cl.vsschile.ExcelReportGenerator\" \\");
            System.out.println("    -Dexec.args=\"reporte_brokers.xlsx\"");
            return;
        }
        
        String outputFile = args[0];
        String dbPassword = args.length > 1 ? args[1] : "";
        
        try {
            ExcelReportGenerator generator = new ExcelReportGenerator(
                "localhost", 5432, "sistema_cotizacion_2025", "postgres", dbPassword
            );
            
            System.out.println("==============================================");
            System.out.println("  GENERADOR DE REPORTE EXCEL");
            System.out.println("==============================================\n");
            
            generator.generateReport(outputFile);
            
            System.out.println("\n✓ Reporte generado: " + outputFile);
            
            generator.close();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void generateReport(String outputFile) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        // Crear estilos
        Map<String, CellStyle> styles = createStyles(workbook);
        
        // 1. Hoja de resumen
        createSummarySheet(workbook, styles);
        
        // 2. Hoja de formatos por broker
        createFormatsSheet(workbook, styles);
        
        // 3. Hoja de archivos procesados
        createFilesSheet(workbook, styles);
        
        // 4. Una hoja por cada broker con detalles
        createBrokerSheets(workbook, styles);
        
        // Guardar archivo
        FileOutputStream fileOut = new FileOutputStream(outputFile);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }
    
    private Map<String, CellStyle> createStyles(XSSFWorkbook workbook) {
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        
        // Estilo para headers
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        styles.put("header", headerStyle);
        
        // Estilo para título
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        styles.put("title", titleStyle);
        
        // Estilo normal
        CellStyle normalStyle = workbook.createCellStyle();
        normalStyle.setBorderBottom(BorderStyle.THIN);
        normalStyle.setBorderTop(BorderStyle.THIN);
        normalStyle.setBorderLeft(BorderStyle.THIN);
        normalStyle.setBorderRight(BorderStyle.THIN);
        styles.put("normal", normalStyle);
        
        // Estilo con color de fondo
        CellStyle coloredStyle = workbook.createCellStyle();
        coloredStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        coloredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        coloredStyle.setBorderBottom(BorderStyle.THIN);
        coloredStyle.setBorderTop(BorderStyle.THIN);
        coloredStyle.setBorderLeft(BorderStyle.THIN);
        coloredStyle.setBorderRight(BorderStyle.THIN);
        styles.put("colored", coloredStyle);
        
        return styles;
    }
    
    private void createSummarySheet(XSSFWorkbook workbook, Map<String, CellStyle> styles) 
            throws SQLException {
        Sheet sheet = workbook.createSheet("Resumen");
        
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RESUMEN DE BROKERS Y FORMATOS");
        titleCell.setCellStyle(styles.get("title"));
        rowNum++;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Broker", "Total Archivos", "Columnas con Color", "Columnas Negrita"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // Datos
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT broker_name, COUNT(*) as total_archivos, " +
            "SUM(columnas_con_color_fondo) as con_colores, " +
            "SUM(columnas_negrita) as negrita " +
            "FROM v_archivos_cotizaciones " +
            "GROUP BY broker_name ORDER BY broker_name"
        );
        
        while (rs.next()) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(rs.getString("broker_name"));
            cell0.setCellStyle(styles.get("normal"));
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(rs.getInt("total_archivos"));
            cell1.setCellStyle(styles.get("normal"));
            
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(rs.getInt("con_colores"));
            cell2.setCellStyle(styles.get("normal"));
            
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(rs.getInt("negrita"));
            cell3.setCellStyle(styles.get("normal"));
        }
        
        rs.close();
        stmt.close();
        
        // Auto-size columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createFormatsSheet(XSSFWorkbook workbook, Map<String, CellStyle> styles) 
            throws SQLException {
        Sheet sheet = workbook.createSheet("Formatos");
        
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("FORMATOS/PLANTILLAS POR BROKER");
        titleCell.setCellStyle(styles.get("title"));
        rowNum++;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Broker", "Campo Estándar", "Columna", "Nombre Original", 
                           "Color Fondo", "Color Texto", "Negrita", "Cursiva", "Borde"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // Datos
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT * FROM v_columnas_detalladas ORDER BY broker_name, indice_columna"
        );
        
        String currentBroker = "";
        while (rs.next()) {
            String broker = rs.getString("broker_name");
            
            // Fila separadora entre brokers
            if (!broker.equals(currentBroker)) {
                if (!currentBroker.isEmpty()) {
                    rowNum++; // Línea en blanco
                }
                currentBroker = broker;
            }
            
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(broker);
            row.createCell(1).setCellValue(rs.getString("campo_estandar"));
            row.createCell(2).setCellValue(rs.getString("letra_columna"));
            row.createCell(3).setCellValue(rs.getString("nombre_columna_original"));
            row.createCell(4).setCellValue(rs.getString("color_fondo"));
            row.createCell(5).setCellValue(rs.getString("color_texto"));
            row.createCell(6).setCellValue(rs.getBoolean("es_negrita") ? "✓" : "");
            row.createCell(7).setCellValue(rs.getBoolean("es_cursiva") ? "✓" : "");
            row.createCell(8).setCellValue(rs.getBoolean("tiene_borde") ? "✓" : "");
            
            // Aplicar estilo
            for (int i = 0; i < 9; i++) {
                row.getCell(i).setCellStyle(styles.get("normal"));
            }
        }
        
        rs.close();
        stmt.close();
        
        // Auto-size
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createFilesSheet(XSSFWorkbook workbook, Map<String, CellStyle> styles) 
            throws SQLException {
        Sheet sheet = workbook.createSheet("Archivos Procesados");
        
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("ARCHIVOS PROCESADOS");
        titleCell.setCellStyle(styles.get("title"));
        rowNum++;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Broker", "Nombre Archivo", "Vessel", "IMO", "Total Columnas", "Con Color"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // Datos
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT * FROM v_archivos_cotizaciones ORDER BY broker_name, nombre_archivo"
        );
        
        while (rs.next()) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(rs.getString("broker_name"));
            row.createCell(1).setCellValue(rs.getString("nombre_archivo"));
            row.createCell(2).setCellValue(rs.getString("vessel_name"));
            row.createCell(3).setCellValue(rs.getString("imo_number"));
            row.createCell(4).setCellValue(rs.getInt("total_columnas"));
            row.createCell(5).setCellValue(rs.getInt("columnas_con_color_fondo"));
            
            // Aplicar estilo
            for (int i = 0; i < 6; i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    cell.setCellStyle(styles.get("normal"));
                }
            }
        }
        
        rs.close();
        stmt.close();
        
        // Auto-size
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createBrokerSheets(XSSFWorkbook workbook, Map<String, CellStyle> styles) 
            throws SQLException {
        // Obtener lista de brokers
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT DISTINCT broker_name FROM brokers ORDER BY broker_name"
        );
        
        List<String> brokers = new ArrayList<String>();
        while (rs.next()) {
            brokers.add(rs.getString("broker_name"));
        }
        rs.close();
        stmt.close();
        
        // Crear una hoja por broker
        for (String broker : brokers) {
            createBrokerDetailSheet(workbook, styles, broker);
        }
    }
    
    private void createBrokerDetailSheet(XSSFWorkbook workbook, Map<String, CellStyle> styles, 
                                        String brokerName) throws SQLException {
        // Nombre de hoja válido (máximo 31 caracteres)
        String sheetName = brokerName.length() > 31 ? 
            brokerName.substring(0, 31) : brokerName;
        Sheet sheet = workbook.createSheet(sheetName);
        
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DETALLE: " + brokerName);
        titleCell.setCellStyle(styles.get("title"));
        rowNum++;
        
        // Sección 1: Formato/Plantilla
        Row section1Row = sheet.createRow(rowNum++);
        Cell section1Cell = section1Row.createCell(0);
        section1Cell.setCellValue("FORMATO ESTÁNDAR");
        section1Cell.setCellStyle(styles.get("header"));
        
        // Headers formato
        Row formatHeaderRow = sheet.createRow(rowNum++);
        String[] formatHeaders = {"Campo", "Columna", "Nombre", "Color Fondo", "Negrita"};
        for (int i = 0; i < formatHeaders.length; i++) {
            Cell cell = formatHeaderRow.createCell(i);
            cell.setCellValue(formatHeaders[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // Datos formato
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
            row.createCell(4).setCellValue(rs.getBoolean("es_negrita") ? "✓" : "");
            
            for (int i = 0; i < 5; i++) {
                row.getCell(i).setCellStyle(styles.get("normal"));
            }
        }
        rs.close();
        ps.close();
        
        rowNum += 2; // Espacio
        
        // Sección 2: Archivos
        Row section2Row = sheet.createRow(rowNum++);
        Cell section2Cell = section2Row.createCell(0);
        section2Cell.setCellValue("ARCHIVOS PROCESADOS");
        section2Cell.setCellStyle(styles.get("header"));
        
        // Headers archivos
        Row filesHeaderRow = sheet.createRow(rowNum++);
        String[] filesHeaders = {"Archivo", "Total Columnas", "Con Color"};
        for (int i = 0; i < filesHeaders.length; i++) {
            Cell cell = filesHeaderRow.createCell(i);
            cell.setCellValue(filesHeaders[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // Datos archivos
        ps = conn.prepareStatement(
            "SELECT * FROM v_archivos_cotizaciones WHERE broker_name = ? ORDER BY nombre_archivo"
        );
        ps.setString(1, brokerName);
        rs = ps.executeQuery();
        
        while (rs.next()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rs.getString("nombre_archivo"));
            row.createCell(1).setCellValue(rs.getInt("total_columnas"));
            row.createCell(2).setCellValue(rs.getInt("columnas_con_color_fondo"));
            
            for (int i = 0; i < 3; i++) {
                row.getCell(i).setCellStyle(styles.get("normal"));
            }
        }
        rs.close();
        ps.close();
        
        // Auto-size
        for (int i = 0; i < 5; i++) {
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
