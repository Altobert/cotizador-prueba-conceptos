package cl.vsschile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

/**
 * Analiza todas las columnas de un archivo Excel de Garrets
 */
public class GarretsColumnAnalyzer {
    
    public static void main(String[] args) throws Exception {
        String filePath = "/Users/albertosanmartin/Documents/VSS-COTIZACIONES/brokers28-12-2025/GARRETS INTERNATIONAL LTD/quotation_RFQ0204875.xlsx";
        
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);
        
        // La fila de headers de Garrets está en la fila 25 (índice 24)
        int headerRowIndex = 24;
        Row headerRow = sheet.getRow(headerRowIndex);
        
        System.out.println("==============================================");
        System.out.println("  ANÁLISIS DE COLUMNAS - GARRETS");
        System.out.println("==============================================\n");
        System.out.println("Archivo: " + filePath);
        System.out.println("Fila de headers: " + (headerRowIndex + 1) + "\n");
        
        if (headerRow != null) {
            int lastColumn = headerRow.getLastCellNum();
            System.out.println("Total de columnas en el archivo: " + lastColumn + "\n");
            
            System.out.println("Columnas encontradas:");
            System.out.println("─────────────────────────────────────────────────");
            
            for (int i = 0; i < lastColumn; i++) {
                Cell cell = headerRow.getCell(i);
                String columnLetter = getColumnLetter(i);
                String value = getCellValueAsString(cell);
                
                if (value != null && !value.trim().isEmpty()) {
                    System.out.println(String.format("  %2s (índice %2d): \"%s\"", 
                        columnLetter, i, value));
                } else {
                    System.out.println(String.format("  %2s (índice %2d): [VACÍA]", 
                        columnLetter, i));
                }
            }
            
            // Mostrar una fila de datos como ejemplo
            System.out.println("\n\nEjemplo de datos (primera fila no vacía):");
            System.out.println("─────────────────────────────────────────────────");
            
            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow == null) continue;
                
                boolean hasData = false;
                for (int j = 0; j < lastColumn; j++) {
                    Cell cell = dataRow.getCell(j);
                    String value = getCellValueAsString(cell);
                    if (value != null && !value.trim().isEmpty()) {
                        hasData = true;
                        break;
                    }
                }
                
                if (hasData) {
                    System.out.println("Fila " + (i + 1) + ":");
                    for (int j = 0; j < lastColumn; j++) {
                        Cell cell = dataRow.getCell(j);
                        String value = getCellValueAsString(cell);
                        String columnLetter = getColumnLetter(j);
                        
                        if (value != null && !value.trim().isEmpty()) {
                            System.out.println(String.format("  %2s: %s", columnLetter, value));
                        }
                    }
                    break;
                }
            }
        }
        
        workbook.close();
        fis.close();
    }
    
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue().trim();
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return "[Formula]";
                }
            case Cell.CELL_TYPE_BLANK:
                return null;
            default:
                return null;
        }
    }
    
    private static String getColumnLetter(int columnIndex) {
        StringBuilder columnName = new StringBuilder();
        while (columnIndex >= 0) {
            columnName.insert(0, (char) ('A' + (columnIndex % 26)));
            columnIndex = (columnIndex / 26) - 1;
        }
        return columnName.toString();
    }
}
