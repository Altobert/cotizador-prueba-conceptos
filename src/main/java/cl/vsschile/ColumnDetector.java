package cl.vsschile;

import org.apache.poi.ss.usermodel.*;
import java.util.*;

/**
 * Detecta y mapea las columnas de cotizaciones según el formato del broker
 */
public class ColumnDetector {
    
    /**
     * Estructura para almacenar información de columnas detectadas
     */
    public static class ColumnMapping {
        public String brokerName;
        public int headerRow = -1;
        public Map<String, Integer> columns = new HashMap<String, Integer>();
        public List<String> columnNames = new ArrayList<String>();
        public Map<Integer, CellStyleInfo> columnStyles = new HashMap<Integer, CellStyleInfo>();
        
        public boolean isValid() {
            return headerRow >= 0 && !columns.isEmpty();
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Broker: ").append(brokerName).append("\n");
            sb.append("Header Row: ").append(headerRow + 1).append("\n");
            sb.append("Columnas detectadas:\n");
            for (Map.Entry<String, Integer> entry : columns.entrySet()) {
                sb.append("  - ").append(entry.getKey())
                  .append(" -> Columna ").append(entry.getValue() + 1).append("\n");
            }
            return sb.toString();
        }
    }
    
    /**
     * Detecta las columnas del formato de cotización
     */
    public static ColumnMapping detectColumns(Sheet sheet, String brokerName) {
        ColumnMapping mapping = new ColumnMapping();
        mapping.brokerName = brokerName;
        
        if (brokerName.contains("MCTC")) {
            detectMCTCColumns(sheet, mapping);
        } else if (brokerName.contains("OCEANIC")) {
            detectOceanicColumns(sheet, mapping);
        } else if (brokerName.contains("CMA")) {
            detectCMAColumns(sheet, mapping);
        } else if (brokerName.contains("GARRETS")) {
            detectGarretsColumns(sheet, mapping);
        } else if (brokerName.contains("PROCURESHIP")) {
            detectProcureshipColumns(sheet, mapping);
        } else if (brokerName.contains("BSM")) {
            detectBSMColumns(sheet, mapping);
        } else {
            detectGenericColumns(sheet, mapping);
        }
        
        return mapping;
    }
    
    /**
     * MCTC MARINE LTD
     * Header en fila 10: AA, MCTC'S REF NO, FOOD CATEGORIES, ITEM, ITEM DESCRIPTION,
     *                    UNIT OF MEASURE, QUANTITY ORDER, PRICE, SUPPLIER COMMENTS, 
     *                    VESSEL COMMENTS, TOTAL
     */
    private static void detectMCTCColumns(Sheet sheet, ColumnMapping mapping) {
        Row row = sheet.getRow(9); // Fila 10 (índice 9)
        if (row != null) {
            mapping.headerRow = 9;
            
            for (int i = 0; i < 15; i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    String value = getCellValueAsString(cell);
                    if (value != null) {
                        String normalized = value.toUpperCase().trim();
                        
                        // Capturar estilo de la celda
                        CellStyleInfo styleInfo = CellStyleInfo.fromCell(cell);
                        mapping.columnStyles.put(i, styleInfo);
                        
                        // Mapear columnas conocidas
                        if (normalized.contains("REF NO") || normalized.contains("MCTC")) {
                            mapping.columns.put("ITEM_CODE", i);
                        } else if (normalized.equals("FOOD CATEGORIES") || normalized.contains("CATEGORIES")) {
                            mapping.columns.put("CATEGORY", i);
                        } else if (normalized.equals("ITEM") && !normalized.contains("DESCRIPTION")) {
                            mapping.columns.put("ITEM_NAME", i);
                        } else if (normalized.contains("ITEM DESCRIPTION") || normalized.equals("ITEM DESCRIPTION")) {
                            mapping.columns.put("DESCRIPTION", i);
                        } else if (normalized.contains("UNIT OF MEASURE") || normalized.equals("UNIT OF MEASURE")) {
                            mapping.columns.put("UOM", i);
                        } else if (normalized.contains("QUANTITY ORDER") || normalized.contains("QUANTITY")) {
                            mapping.columns.put("QUANTITY", i);
                        } else if (normalized.equals("PRICE")) {
                            mapping.columns.put("UNIT_PRICE", i);
                        } else if (normalized.contains("SUPPLIER COMMENTS")) {
                            mapping.columns.put("SUPPLIER_COMMENTS", i);
                        } else if (normalized.equals("TOTAL")) {
                            mapping.columns.put("TOTAL", i);
                        }
                        
                        mapping.columnNames.add(value);
                    }
                }
            }
        }
    }
    
    /**
     * OCEANIC CATERING LTD
     * Header en fila 13: Category, Item - Description, OCL Code, VSC Code, 
     *                    Expiry Date, Requested Quantity, OCL Uom, Supplier Code,
     *                    Case, Case Size, Package, Package Size, Metric, Brand, Unit Cost
     */
    private static void detectOceanicColumns(Sheet sheet, ColumnMapping mapping) {
        Row row = sheet.getRow(12); // Fila 13 (índice 12)
        if (row != null) {
            mapping.headerRow = 12;
            
            for (int i = 0; i < 20; i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    String value = getCellValueAsString(cell);
                    if (value != null && !value.trim().isEmpty()) {
                        String normalized = value.toUpperCase().trim();
                        
                        if (normalized.equals("CATEGORY")) {
                            mapping.columns.put("CATEGORY", i);
                        } else if (normalized.contains("ITEM") && normalized.contains("DESCRIPTION")) {
                            mapping.columns.put("ITEM_NAME", i);
                        } else if (normalized.contains("OCL CODE")) {
                            mapping.columns.put("OCL_CODE", i);
                        } else if (normalized.contains("VSC CODE")) {
                            mapping.columns.put("VSC_CODE", i);
                        } else if (normalized.contains("EXPIRY DATE")) {
                            mapping.columns.put("EXPIRY_DATE", i);
                        } else if (normalized.contains("REQUESTED QUANTITY")) {
                            mapping.columns.put("QUANTITY", i);
                        } else if (normalized.contains("OCL UOM")) {
                            mapping.columns.put("UOM", i);
                        } else if (normalized.contains("SUPPLIER CODE")) {
                            mapping.columns.put("SUPPLIER_CODE", i);
                        } else if (normalized.equals("CASE")) {
                            mapping.columns.put("CASE", i);
                        } else if (normalized.equals("CASE SIZE")) {
                            mapping.columns.put("CASE_SIZE", i);
                        } else if (normalized.equals("PACKAGE")) {
                            mapping.columns.put("PACKAGE", i);
                        } else if (normalized.equals("PACKAGE SIZE")) {
                            mapping.columns.put("PACKAGE_SIZE", i);
                        } else if (normalized.equals("METRIC")) {
                            mapping.columns.put("METRIC", i);
                        } else if (normalized.equals("BRAND")) {
                            mapping.columns.put("BRAND", i);
                        } else if (normalized.contains("UNIT COST")) {
                            mapping.columns.put("UNIT_PRICE", i);
                        }
                        
                        mapping.columnNames.add(value);
                    }
                }
            }
        }
    }
    
    /**
     * CMA CGM
     * Header en fila 19: No, Item Code, Description, Brand, Weight, Unit, 
     *                    Package, Quantity, Unit Price, Discount %, VAT %, Total Price
     */
    private static void detectCMAColumns(Sheet sheet, ColumnMapping mapping) {
        Row row = sheet.getRow(18); // Fila 19 (índice 18)
        if (row != null) {
            mapping.headerRow = 18;
            
            for (int i = 0; i < 20; i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    String value = getCellValueAsString(cell);
                    if (value != null && !value.trim().isEmpty()) {
                        String normalized = value.toUpperCase().trim();
                        
                        if (normalized.equals("NO")) {
                            mapping.columns.put("LINE_NO", i);
                        } else if (normalized.contains("ITEM CODE")) {
                            mapping.columns.put("ITEM_CODE", i);
                        } else if (normalized.equals("DESCRIPTION")) {
                            mapping.columns.put("ITEM_NAME", i);
                        } else if (normalized.equals("BRAND")) {
                            mapping.columns.put("BRAND", i);
                        } else if (normalized.equals("WEIGHT")) {
                            mapping.columns.put("WEIGHT", i);
                        } else if (normalized.equals("UNIT")) {
                            mapping.columns.put("UOM", i);
                        } else if (normalized.equals("PACKAGE")) {
                            mapping.columns.put("PACKAGE", i);
                        } else if (normalized.equals("QUANTITY")) {
                            mapping.columns.put("QUANTITY", i);
                        } else if (normalized.contains("UNIT PRICE")) {
                            mapping.columns.put("UNIT_PRICE", i);
                        } else if (normalized.contains("DISCOUNT")) {
                            mapping.columns.put("DISCOUNT", i);
                        } else if (normalized.contains("VAT")) {
                            mapping.columns.put("VAT", i);
                        } else if (normalized.contains("TOTAL PRICE")) {
                            mapping.columns.put("TOTAL", i);
                        }
                        
                        mapping.columnNames.add(value);
                    }
                }
            }
        }
    }
    
    /**
     * GARRETS INTERNATIONAL LTD
     * Header en fila 25: No., Part#, Vessel, Description, Quality, Unit, 
     *                    Quantity, Unit Price, Disc.%, Del. Days
     */
    private static void detectGarretsColumns(Sheet sheet, ColumnMapping mapping) {
        Row row = sheet.getRow(24); // Fila 25 (índice 24)
        if (row != null) {
            mapping.headerRow = 24;
            
            for (int i = 0; i < 20; i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    String value = getCellValueAsString(cell);
                    if (value != null && !value.trim().isEmpty()) {
                        String normalized = value.toUpperCase().trim();
                        
                        if (normalized.equals("NO.") || normalized.equals("NO")) {
                            mapping.columns.put("LINE_NO", i);
                        } else if (normalized.contains("PART")) {
                            mapping.columns.put("ITEM_CODE", i);
                        } else if (normalized.equals("VESSEL")) {
                            mapping.columns.put("VESSEL", i);
                        } else if (normalized.equals("DESCRIPTION")) {
                            mapping.columns.put("ITEM_NAME", i);
                        } else if (normalized.equals("QUALITY")) {
                            mapping.columns.put("QUALITY", i);
                        } else if (normalized.equals("UNIT")) {
                            mapping.columns.put("UOM", i);
                        } else if (normalized.equals("QUANTITY")) {
                            mapping.columns.put("QUANTITY", i);
                        } else if (normalized.contains("UNIT PRICE")) {
                            mapping.columns.put("UNIT_PRICE", i);
                        } else if (normalized.contains("DISC")) {
                            mapping.columns.put("DISCOUNT", i);
                        } else if (normalized.contains("DEL") && normalized.contains("DAYS")) {
                            mapping.columns.put("DELIVERY_DAYS", i);
                        }
                        
                        mapping.columnNames.add(value);
                    }
                }
            }
        }
    }
    
    /**
     * PROCURESHIP
     * Header en fila 14: No., Description, Item Office Notes, Vessel Notes, 
     *                    Item Code / Part No., Reference No., Drawing No., 
     *                    Quantity Requested, UoM, Quantity Offered, UoM, 
     *                    Unit Cost, Disc. %, Line Cost
     */
    private static void detectProcureshipColumns(Sheet sheet, ColumnMapping mapping) {
        Row row = sheet.getRow(13); // Fila 14 (índice 13)
        if (row != null) {
            mapping.headerRow = 13;
            
            for (int i = 0; i < 20; i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    String value = getCellValueAsString(cell);
                    if (value != null && !value.trim().isEmpty()) {
                        String normalized = value.toUpperCase().trim();
                        
                        if (normalized.equals("NO.") || normalized.equals("NO")) {
                            mapping.columns.put("LINE_NO", i);
                        } else if (normalized.equals("DESCRIPTION")) {
                            mapping.columns.put("ITEM_NAME", i);
                        } else if (normalized.contains("ITEM OFFICE NOTES")) {
                            mapping.columns.put("OFFICE_NOTES", i);
                        } else if (normalized.contains("VESSEL NOTES")) {
                            mapping.columns.put("VESSEL_NOTES", i);
                        } else if (normalized.contains("ITEM CODE") || normalized.contains("PART NO")) {
                            mapping.columns.put("ITEM_CODE", i);
                        } else if (normalized.contains("REFERENCE NO")) {
                            mapping.columns.put("REFERENCE_NO", i);
                        } else if (normalized.contains("DRAWING NO")) {
                            mapping.columns.put("DRAWING_NO", i);
                        } else if (normalized.contains("QUANTITY REQUESTED")) {
                            mapping.columns.put("QUANTITY_REQUESTED", i);
                        } else if (normalized.equals("UOM") && !mapping.columns.containsKey("UOM")) {
                            mapping.columns.put("UOM", i);
                        } else if (normalized.contains("QUANTITY OFFERED")) {
                            mapping.columns.put("QUANTITY_OFFERED", i);
                        } else if (normalized.equals("UOM") && mapping.columns.containsKey("UOM")) {
                            mapping.columns.put("UOM_OFFERED", i);
                        } else if (normalized.contains("UNIT COST")) {
                            mapping.columns.put("UNIT_PRICE", i);
                        } else if (normalized.contains("DISC")) {
                            mapping.columns.put("DISCOUNT", i);
                        } else if (normalized.contains("LINE COST")) {
                            mapping.columns.put("TOTAL", i);
                        }
                        
                        mapping.columnNames.add(value);
                    }
                }
            }
        }
    }
    
    /**
     * BSM CATERING - formato genérico
     */
    private static void detectBSMColumns(Sheet sheet, ColumnMapping mapping) {
        detectGenericColumns(sheet, mapping);
    }
    
    /**
     * Detección genérica de columnas
     * Busca la fila con más headers típicos de cotizaciones
     */
    private static void detectGenericColumns(Sheet sheet, ColumnMapping mapping) {
        int maxScore = 0;
        int bestRow = -1;
        
        // Palabras clave para identificar headers
        String[] keywords = {
            "DESCRIPTION", "ITEM", "QUANTITY", "PRICE", "UNIT", "TOTAL",
            "QTY", "UOM", "AMOUNT", "CODE", "PART"
        };
        
        // Buscar en las primeras 30 filas
        for (int rowIdx = 0; rowIdx < 30; rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row == null) continue;
            
            int score = 0;
            int nonEmptyCount = 0;
            
            for (int i = 0; i < 20; i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    String value = getCellValueAsString(cell);
                    if (value != null && !value.trim().isEmpty()) {
                        nonEmptyCount++;
                        String normalized = value.toUpperCase().trim();
                        
                        for (String keyword : keywords) {
                            if (normalized.contains(keyword)) {
                                score += 2;
                                break;
                            }
                        }
                    }
                }
            }
            
            // La fila debe tener al menos 4 celdas no vacías
            if (nonEmptyCount >= 4 && score > maxScore) {
                maxScore = score;
                bestRow = rowIdx;
            }
        }
        
        if (bestRow >= 0) {
            mapping.headerRow = bestRow;
            Row row = sheet.getRow(bestRow);
            
            for (int i = 0; i < 20; i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    String value = getCellValueAsString(cell);
                    if (value != null && !value.trim().isEmpty()) {
                        String normalized = value.toUpperCase().trim();
                        
                        // Mapeo genérico
                        if (normalized.contains("ITEM") && normalized.contains("CODE")) {
                            mapping.columns.put("ITEM_CODE", i);
                        } else if (normalized.contains("DESCRIPTION") || 
                                   (normalized.contains("ITEM") && !normalized.contains("CODE"))) {
                            mapping.columns.put("ITEM_NAME", i);
                        } else if (normalized.contains("QUANTITY") || normalized.equals("QTY")) {
                            mapping.columns.put("QUANTITY", i);
                        } else if (normalized.contains("UNIT") && normalized.contains("PRICE")) {
                            mapping.columns.put("UNIT_PRICE", i);
                        } else if (normalized.equals("PRICE")) {
                            mapping.columns.put("UNIT_PRICE", i);
                        } else if (normalized.equals("UOM") || normalized.equals("UNIT")) {
                            mapping.columns.put("UOM", i);
                        } else if (normalized.contains("TOTAL") || normalized.contains("AMOUNT")) {
                            mapping.columns.put("TOTAL", i);
                        } else if (normalized.contains("BRAND")) {
                            mapping.columns.put("BRAND", i);
                        } else if (normalized.contains("CATEGORY")) {
                            mapping.columns.put("CATEGORY", i);
                        }
                        
                        mapping.columnNames.add(value);
                    }
                }
            }
        }
    }
    
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_FORMULA:
                // Intentar obtener valor calculado
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            default:
                return null;
        }
    }
}
