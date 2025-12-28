package cl.vsschile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Recrea plantillas de cotización con el formato original del broker
 * usando la información almacenada en la base de datos
 */
public class TemplateRecreator {
    
    private Connection conn;
    
    public TemplateRecreator(String dbHost, int dbPort, String dbName, String dbUser, String dbPassword) 
            throws SQLException {
        String dbUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
        this.conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java TemplateRecreator <nombre-broker> <archivo-salida.xlsx> [db-password]");
            System.out.println("\nGenera una plantilla Excel con el formato original del broker");
            System.out.println("\nEjemplo:");
            System.out.println("  mvn exec:java -Dexec.mainClass=\"cl.vsschile.TemplateRecreator\" \\");
            System.out.println("    -Dexec.args=\"'MCTC MARINE LTD' plantilla_mctc.xlsx\"");
            return;
        }
        
        String brokerName = args[0];
        String outputFile = args[1];
        String dbPassword = args.length > 2 ? args[2] : "";
        
        try {
            TemplateRecreator recreator = new TemplateRecreator(
                "localhost", 5432, "sistema_cotizacion_2025", "postgres", dbPassword
            );
            
            System.out.println("==============================================");
            System.out.println("  RECREAR PLANTILLA DE COTIZACIÓN");
            System.out.println("==============================================\n");
            System.out.println("Broker: " + brokerName);
            System.out.println("Archivo: " + outputFile);
            System.out.println();
            
            recreator.recreateTemplate(brokerName, outputFile);
            
            System.out.println("\n✓ Plantilla generada exitosamente!");
            System.out.println("✓ Formato aplicado con colores originales");
            
            recreator.close();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void recreateTemplate(String brokerName, String outputFile) throws Exception {
        // Obtener información del formato
        FormatInfo format = getFormatInfo(brokerName);
        if (format == null) {
            throw new Exception("No se encontró formato para: " + brokerName);
        }
        
        // Obtener columnas
        List<ColumnInfo> columns = getColumns(brokerName);
        if (columns.isEmpty()) {
            throw new Exception("No se encontraron columnas para: " + brokerName);
        }
        
        System.out.println("✓ Formato detectado:");
        System.out.println("  - Header en fila: " + (format.headerRow + 1));
        System.out.println("  - Columnas: " + columns.size());
        
        // Crear Excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Cotización");
        
        // Crear estilos basados en colores detectados
        Map<String, XSSFCellStyle> styles = createStylesFromDB(workbook, columns);
        
        // Crear filas de encabezado si las hay
        createHeaderRows(sheet, format.headerRow);
        
        // Crear fila de headers con formato
        createHeaderRow(sheet, format.headerRow, columns, styles);
        
        // Crear algunas filas de ejemplo
        createSampleRows(sheet, format.headerRow + 1, columns, styles, 10);
        
        // Auto-size columnas
        for (int i = 0; i < columns.size(); i++) {
            sheet.autoSizeColumn(columns.get(i).indiceColumna);
        }
        
        // Guardar
        FileOutputStream fileOut = new FileOutputStream(outputFile);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        
        System.out.println("\n✓ Colores aplicados:");
        for (ColumnInfo col : columns) {
            if (col.colorFondo != null) {
                System.out.println("  - Columna " + col.letraColumna + ": " + col.colorFondo);
            }
        }
    }
    
    private FormatInfo getFormatInfo(String brokerName) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM v_formatos_activos WHERE broker_name = ?"
        );
        ps.setString(1, brokerName);
        ResultSet rs = ps.executeQuery();
        
        FormatInfo info = null;
        if (rs.next()) {
            info = new FormatInfo();
            info.formatoId = rs.getInt("formato_id");
            info.brokerName = rs.getString("broker_name");
            info.headerRow = rs.getInt("header_row");
            info.version = rs.getString("version");
        }
        
        rs.close();
        ps.close();
        return info;
    }
    
    private List<ColumnInfo> getColumns(String brokerName) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
        
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM v_columnas_detalladas WHERE broker_name = ? ORDER BY indice_columna"
        );
        ps.setString(1, brokerName);
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            ColumnInfo col = new ColumnInfo();
            col.campoEstandar = rs.getString("campo_estandar");
            col.nombreOriginal = rs.getString("nombre_columna_original");
            col.indiceColumna = rs.getInt("indice_columna");
            col.letraColumna = rs.getString("letra_columna");
            col.colorFondo = rs.getString("color_fondo");
            col.colorTexto = rs.getString("color_texto");
            col.esNegrita = rs.getBoolean("es_negrita");
            col.esCursiva = rs.getBoolean("es_cursiva");
            col.tieneBorde = rs.getBoolean("tiene_borde");
            columns.add(col);
        }
        
        rs.close();
        ps.close();
        return columns;
    }
    
    private Map<String, XSSFCellStyle> createStylesFromDB(XSSFWorkbook workbook, List<ColumnInfo> columns) {
        Map<String, XSSFCellStyle> styles = new HashMap<String, XSSFCellStyle>();
        
        // Crear un estilo por cada combinación única de colores
        for (ColumnInfo col : columns) {
            String key = col.campoEstandar;
            
            XSSFCellStyle style = workbook.createCellStyle();
            
            // Aplicar color de fondo
            if (col.colorFondo != null && !col.colorFondo.isEmpty()) {
                XSSFColor bgColor = hexToXSSFColor(col.colorFondo);
                if (bgColor != null) {
                    style.setFillForegroundColor(bgColor);
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                }
            }
            
            // Aplicar fuente
            XSSFFont font = workbook.createFont();
            if (col.esNegrita) {
                font.setBold(true);
            }
            if (col.esCursiva) {
                font.setItalic(true);
            }
            if (col.colorTexto != null && !col.colorTexto.isEmpty()) {
                XSSFColor textColor = hexToXSSFColor(col.colorTexto);
                if (textColor != null) {
                    font.setColor(textColor);
                }
            }
            style.setFont(font);
            
            // Aplicar bordes
            if (col.tieneBorde) {
                style.setBorderBottom(BorderStyle.THIN);
                style.setBorderTop(BorderStyle.THIN);
                style.setBorderLeft(BorderStyle.THIN);
                style.setBorderRight(BorderStyle.THIN);
            }
            
            styles.put(key, style);
        }
        
        // Estilo normal para datos
        XSSFCellStyle normalStyle = workbook.createCellStyle();
        normalStyle.setBorderBottom(BorderStyle.THIN);
        normalStyle.setBorderTop(BorderStyle.THIN);
        normalStyle.setBorderLeft(BorderStyle.THIN);
        normalStyle.setBorderRight(BorderStyle.THIN);
        styles.put("normal", normalStyle);
        
        return styles;
    }
    
    private XSSFColor hexToXSSFColor(String hexColor) {
        if (hexColor == null || hexColor.isEmpty()) {
            return null;
        }
        
        try {
            // Remover # si existe
            hexColor = hexColor.replace("#", "");
            
            // Convertir hex a RGB
            int r = Integer.parseInt(hexColor.substring(0, 2), 16);
            int g = Integer.parseInt(hexColor.substring(2, 4), 16);
            int b = Integer.parseInt(hexColor.substring(4, 6), 16);
            
            byte[] rgb = new byte[]{(byte) r, (byte) g, (byte) b};
            return new XSSFColor(rgb, null);
            
        } catch (Exception e) {
            System.err.println("Error convirtiendo color: " + hexColor);
            return null;
        }
    }
    
    private void createHeaderRows(XSSFSheet sheet, int headerRow) {
        // Crear filas antes del header si las hay
        for (int i = 0; i < headerRow; i++) {
            sheet.createRow(i);
        }
    }
    
    private void createHeaderRow(XSSFSheet sheet, int headerRowNum, 
                                 List<ColumnInfo> columns, Map<String, XSSFCellStyle> styles) {
        Row headerRow = sheet.createRow(headerRowNum);
        
        for (ColumnInfo col : columns) {
            Cell cell = headerRow.createCell(col.indiceColumna);
            cell.setCellValue(col.nombreOriginal);
            
            // Aplicar estilo del campo
            XSSFCellStyle style = styles.get(col.campoEstandar);
            if (style != null) {
                cell.setCellStyle(style);
            }
        }
    }
    
    private void createSampleRows(XSSFSheet sheet, int startRow, 
                                  List<ColumnInfo> columns, Map<String, XSSFCellStyle> styles, 
                                  int numRows) {
        XSSFCellStyle normalStyle = styles.get("normal");
        
        for (int i = 0; i < numRows; i++) {
            Row row = sheet.createRow(startRow + i);
            
            for (ColumnInfo col : columns) {
                Cell cell = row.createCell(col.indiceColumna);
                
                // Agregar datos de ejemplo según el campo
                if (col.campoEstandar.equals("ITEM_CODE")) {
                    cell.setCellValue("ITEM-" + (i + 1));
                } else if (col.campoEstandar.equals("ITEM_NAME")) {
                    cell.setCellValue("Producto de ejemplo " + (i + 1));
                } else if (col.campoEstandar.equals("QUANTITY")) {
                    cell.setCellValue(10 + i);
                } else if (col.campoEstandar.equals("UNIT_PRICE")) {
                    cell.setCellValue(100.0 + (i * 10));
                } else if (col.campoEstandar.equals("TOTAL")) {
                    cell.setCellValue((10 + i) * (100.0 + (i * 10)));
                } else if (col.campoEstandar.equals("UOM")) {
                    cell.setCellValue("KG");
                } else if (col.campoEstandar.equals("CATEGORY")) {
                    cell.setCellValue("Categoría " + ((i % 3) + 1));
                } else {
                    cell.setCellValue("");
                }
                
                if (normalStyle != null) {
                    cell.setCellStyle(normalStyle);
                }
            }
        }
    }
    
    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            // Ignorar
        }
    }
    
    private static class FormatInfo {
        int formatoId;
        String brokerName;
        int headerRow;
        String version;
    }
    
    private static class ColumnInfo {
        String campoEstandar;
        String nombreOriginal;
        int indiceColumna;
        String letraColumna;
        String colorFondo;
        String colorTexto;
        boolean esNegrita;
        boolean esCursiva;
        boolean tieneBorde;
    }
}
