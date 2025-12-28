package cl.vsschile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;

/**
 * Utilidad para probar el detector de columnas en archivos de brokers
 */
public class ColumnDetectorTest {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java ColumnDetectorTest <directorio-brokers>");
            System.out.println("\nEjemplo:");
            System.out.println("  mvn exec:java -Dexec.mainClass=\"cl.vsschile.ColumnDetectorTest\" \\");
            System.out.println("    -Dexec.args=\"/path/to/BROKERS\"");
            return;
        }
        
        String brokersPath = args[0];
        File brokersDir = new File(brokersPath);
        
        if (!brokersDir.exists() || !brokersDir.isDirectory()) {
            System.err.println("Error: El directorio no existe: " + brokersPath);
            return;
        }
        
        System.out.println("==============================================");
        System.out.println("  DETECTOR DE COLUMNAS - COTIZACIONES");
        System.out.println("==============================================\n");
        
        File[] brokerDirs = brokersDir.listFiles();
        if (brokerDirs == null) {
            System.err.println("Error: No se pueden leer los subdirectorios");
            return;
        }
        
        for (File brokerDir : brokerDirs) {
            if (brokerDir.isDirectory()) {
                processBrokerDirectory(brokerDir);
            }
        }
    }
    
    private static void processBrokerDirectory(File brokerDir) {
        String brokerName = brokerDir.getName();
        
        File[] files = brokerDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lower = name.toLowerCase();
                return lower.endsWith(".xlsx") || lower.endsWith(".xls");
            }
        });
        
        if (files == null || files.length == 0) {
            return;
        }
        
        // Tomar el primer archivo como muestra
        File sampleFile = files[0];
        
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("  BROKER: " + brokerName);
        System.out.println("  Archivo: " + sampleFile.getName());
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        try {
            ColumnDetector.ColumnMapping mapping = detectColumnsFromFile(sampleFile, brokerName);
            
            if (mapping.isValid()) {
                System.out.println("\n✓ Columnas detectadas exitosamente");
                System.out.println("  Fila de headers: " + (mapping.headerRow + 1));
                System.out.println("  Total columnas mapeadas: " + mapping.columns.size());
                System.out.println("\nMapeo de columnas:");
                
                // Ordenar por índice de columna
                java.util.List<java.util.Map.Entry<String, Integer>> sortedEntries = 
                    new java.util.ArrayList<java.util.Map.Entry<String, Integer>>(mapping.columns.entrySet());
                java.util.Collections.sort(sortedEntries, new java.util.Comparator<java.util.Map.Entry<String, Integer>>() {
                    public int compare(java.util.Map.Entry<String, Integer> a, java.util.Map.Entry<String, Integer> b) {
                        return a.getValue().compareTo(b.getValue());
                    }
                });
                
                for (java.util.Map.Entry<String, Integer> entry : sortedEntries) {
                    int colIdx = entry.getValue();
                    String fieldName = entry.getKey();
                    String columnLetter = getColumnLetter(colIdx);
                    String headerText = colIdx < mapping.columnNames.size() ? 
                        mapping.columnNames.get(colIdx) : "N/A";
                    
                    System.out.println(String.format("  %-20s -> Columna %s (%-3s) \"%s\"", 
                        fieldName, columnLetter, colIdx + 1, headerText));
                }
                
                // Mostrar datos de ejemplo
                showSampleData(sampleFile, mapping);
                
            } else {
                System.out.println("\n✗ No se pudieron detectar columnas");
            }
            
        } catch (Exception e) {
            System.err.println("\n✗ Error al procesar: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n");
    }
    
    private static ColumnDetector.ColumnMapping detectColumnsFromFile(File file, String brokerName) 
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
    
    private static void showSampleData(File file, ColumnDetector.ColumnMapping mapping) 
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
            System.out.println("\nDatos de ejemplo (primeras 3 filas):");
            System.out.println("─────────────────────────────────────");
            
            // Mostrar hasta 3 filas de datos
            int dataRowsShown = 0;
            for (int i = mapping.headerRow + 1; i < sheet.getLastRowNum() && dataRowsShown < 3; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                // Verificar si la fila tiene datos
                boolean hasData = false;
                Integer itemCol = mapping.columns.get("ITEM_NAME");
                if (itemCol != null) {
                    Cell cell = row.getCell(itemCol);
                    if (cell != null && getCellValueAsString(cell) != null) {
                        hasData = true;
                    }
                }
                
                if (!hasData) continue;
                
                System.out.println("\nFila " + (i + 1) + ":");
                
                // Mostrar campos principales
                String[] mainFields = {"ITEM_NAME", "ITEM_CODE", "QUANTITY", "UOM", "UNIT_PRICE", "TOTAL"};
                for (String field : mainFields) {
                    Integer colIdx = mapping.columns.get(field);
                    if (colIdx != null) {
                        Cell cell = row.getCell(colIdx);
                        String value = getCellValueAsString(cell);
                        if (value != null && !value.trim().isEmpty()) {
                            System.out.println("  " + field + ": " + value);
                        }
                    }
                }
                
                dataRowsShown++;
            }
            
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            fis.close();
        }
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
                    return "[Formula: " + cell.getCellFormula() + "]";
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
