package cl.vsschile;

import org.apache.poi.ss.usermodel.*;
import java.util.*;

/**
 * Extrae metadata específica de las cabeceras de cada broker
 */
public class BrokerMetadataExtractor {
    
    /**
     * Clase para almacenar un campo de metadata
     */
    public static class MetadataField {
        public String seccion;
        public String campoNombre;
        public String campoValor;
        public int filaOrigen;
        public int columnaOrigen;
        public String letraColumna;
        
        public MetadataField(String seccion, String campoNombre, String campoValor, 
                           int fila, int columna) {
            this.seccion = seccion;
            this.campoNombre = campoNombre;
            this.campoValor = campoValor;
            this.filaOrigen = fila;
            this.columnaOrigen = columna;
            this.letraColumna = getColumnLetter(columna);
        }
        
        private String getColumnLetter(int columnIndex) {
            StringBuilder columnName = new StringBuilder();
            while (columnIndex >= 0) {
                columnName.insert(0, (char) ('A' + (columnIndex % 26)));
                columnIndex = (columnIndex / 26) - 1;
            }
            return columnName.toString();
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s = %s (Fila: %d, Col: %s)", 
                seccion, campoNombre, campoValor, filaOrigen + 1, letraColumna);
        }
    }
    
    /**
     * Extrae metadata según el broker
     */
    public static List<MetadataField> extractMetadata(Sheet sheet, String brokerName) {
        if (brokerName.contains("BSM")) {
            return extractBSMMetadata(sheet);
        } else if (brokerName.contains("MCTC")) {
            return extractMCTCMetadata(sheet);
        } else if (brokerName.contains("OCEANIC")) {
            return extractOceanicMetadata(sheet);
        } else if (brokerName.contains("CMA")) {
            return extractCMAMetadata(sheet);
        } else if (brokerName.contains("GARRETS")) {
            return extractGarretsMetadata(sheet);
        } else if (brokerName.contains("PROCURESHIP")) {
            return extractProcureshipMetadata(sheet);
        } else {
            return new ArrayList<MetadataField>();
        }
    }
    
    /**
     * BSM CATERING
     * Metadata en filas 1-17
     */
    private static List<MetadataField> extractBSMMetadata(Sheet sheet) {
        List<MetadataField> metadata = new ArrayList<MetadataField>();
        
        // Sección: Company Details (filas 3-11)
        addField(metadata, sheet, "Company Details", "Company Name Line 1", 3, 0);
        addField(metadata, sheet, "Company Details", "Company Name Line 2", 4, 0);
        addField(metadata, sheet, "Company Details", "Company Name Line 3", 5, 0);
        addField(metadata, sheet, "Company Details", "Company Name Line 4", 6, 0);
        addField(metadata, sheet, "Company Details", "Company Address", 7, 0);
        addField(metadata, sheet, "Company Details", "Company Contact", 8, 0);
        addField(metadata, sheet, "Company Details", "Company Email Label", 9, 0);
        addField(metadata, sheet, "Company Details", "Company Email", 9, 2);
        addField(metadata, sheet, "Company Details", "Company Web Label", 10, 0);
        addField(metadata, sheet, "Company Details", "Company Web", 10, 2);
        
        // Sección: RFQ Information (filas 3-11, columnas M-O)
        addField(metadata, sheet, "RFQ Information", "Vessel", 3, 14);
        addField(metadata, sheet, "RFQ Information", "RFQ Number", 4, 14);
        addField(metadata, sheet, "RFQ Information", "RFQ Date", 5, 14);
        addField(metadata, sheet, "RFQ Information", "Submit Quote Before", 7, 14);
        addField(metadata, sheet, "RFQ Information", "Port of Delivery", 8, 11);
        addField(metadata, sheet, "RFQ Information", "Vessel ETA", 9, 14);
        addField(metadata, sheet, "RFQ Information", "Payment Terms", 10, 14);
        addField(metadata, sheet, "RFQ Information", "Payment Days", 10, 18);
        addField(metadata, sheet, "RFQ Information", "Vendor Reference", 11, 12);
        addField(metadata, sheet, "RFQ Information", "Delivery Term", 12, 14);
        addField(metadata, sheet, "RFQ Information", "Currency", 13, 14);
        addField(metadata, sheet, "RFQ Information", "Discount Percentage", 14, 12);
        addField(metadata, sheet, "RFQ Information", "VAT Percentage", 15, 12);
        addField(metadata, sheet, "RFQ Information", "Place City", 16, 12);
        
        // Sección: Vendor Details (filas 12-17)
        addField(metadata, sheet, "Vendor Details", "Vendor Name", 12, 3);
        addField(metadata, sheet, "Vendor Details", "Vendor Address", 13, 3);
        addField(metadata, sheet, "Vendor Details", "Vendor City", 14, 3);
        addField(metadata, sheet, "Vendor Details", "Vendor Phone", 15, 3);
        addField(metadata, sheet, "Vendor Details", "Vendor Email", 16, 3);
        
        return metadata;
    }
    
    /**
     * MCTC MARINE LTD
     * Metadata en filas 1-8
     */
    private static List<MetadataField> extractMCTCMetadata(Sheet sheet) {
        List<MetadataField> metadata = new ArrayList<MetadataField>();
        
        // Sección: Quotation Header
        addField(metadata, sheet, "Quotation Header", "Document Title", 0, 0);
        addField(metadata, sheet, "Quotation Header", "Vessel Name", 1, 4);
        addField(metadata, sheet, "Quotation Header", "IMO Number", 2, 4);
        addField(metadata, sheet, "Quotation Header", "Port of Delivery", 3, 4);
        addField(metadata, sheet, "Quotation Header", "Delivery Date", 4, 4);
        addField(metadata, sheet, "Quotation Header", "Supplier", 7, 1);
        addField(metadata, sheet, "Quotation Header", "Quotation Number", 7, 3);
        addField(metadata, sheet, "Quotation Header", "Date", 7, 6);
        
        return metadata;
    }
    
    /**
     * OCEANIC CATERING LTD
     * Metadata en filas 1-12
     */
    private static List<MetadataField> extractOceanicMetadata(Sheet sheet) {
        List<MetadataField> metadata = new ArrayList<MetadataField>();
        
        // Sección: Company Information
        addField(metadata, sheet, "Company Information", "Company Name", 0, 0);
        
        // Sección: Request Information
        addField(metadata, sheet, "Request Information", "Quotation Request Number", 4, 2);
        addField(metadata, sheet, "Request Information", "Quotation Request Date", 5, 2);
        addField(metadata, sheet, "Request Information", "Est. Delivery Date", 6, 2);
        addField(metadata, sheet, "Request Information", "Loading Port", 7, 2);
        addField(metadata, sheet, "Request Information", "Vessel", 8, 2);
        
        return metadata;
    }
    
    /**
     * CMA CGM
     * Metadata en filas 1-18
     */
    private static List<MetadataField> extractCMAMetadata(Sheet sheet) {
        List<MetadataField> metadata = new ArrayList<MetadataField>();
        
        // Sección: Company Details
        addField(metadata, sheet, "Company Details", "RFQ Label", 0, 1);
        addField(metadata, sheet, "Company Details", "Company Name", 3, 0);
        addField(metadata, sheet, "Company Details", "Company Info", 4, 0);
        
        // Sección: Vendor Details
        addField(metadata, sheet, "Vendor Details", "Vendor Name", 12, 3);
        addField(metadata, sheet, "Vendor Details", "Vendor Address", 13, 3);
        addField(metadata, sheet, "Vendor Details", "Vendor City", 14, 3);
        addField(metadata, sheet, "Vendor Details", "Vendor Phone", 15, 3);
        addField(metadata, sheet, "Vendor Details", "Vendor Email", 16, 3);
        
        return metadata;
    }
    
    /**
     * GARRETS INTERNATIONAL LTD
     * Metadata en filas 1-24
     */
    private static List<MetadataField> extractGarretsMetadata(Sheet sheet) {
        List<MetadataField> metadata = new ArrayList<MetadataField>();
        
        // Sección: RFQ Information
        addField(metadata, sheet, "RFQ Information", "Document Title", 0, 0);
        addField(metadata, sheet, "RFQ Information", "RFQ Number", 2, 1);
        addField(metadata, sheet, "RFQ Information", "Vessel Name", 3, 1);
        addField(metadata, sheet, "RFQ Information", "Port", 4, 1);
        addField(metadata, sheet, "RFQ Information", "ETA Date", 5, 1);
        addField(metadata, sheet, "RFQ Information", "Request Date", 6, 1);
        addField(metadata, sheet, "RFQ Information", "Due Date", 7, 1);
        
        // Sección: Supplier Information
        addField(metadata, sheet, "Supplier Information", "Supplier Name", 10, 1);
        addField(metadata, sheet, "Supplier Information", "Address", 11, 1);
        addField(metadata, sheet, "Supplier Information", "Contact Person", 12, 1);
        addField(metadata, sheet, "Supplier Information", "Email", 13, 1);
        addField(metadata, sheet, "Supplier Information", "Phone", 14, 1);
        addField(metadata, sheet, "Supplier Information", "Currency", 16, 1);
        addField(metadata, sheet, "Supplier Information", "Payment Terms", 17, 1);
        
        return metadata;
    }
    
    /**
     * PROCURESHIP
     * Metadata en filas 1-13
     */
    private static List<MetadataField> extractProcureshipMetadata(Sheet sheet) {
        List<MetadataField> metadata = new ArrayList<MetadataField>();
        
        // Sección: Requisition Information
        addField(metadata, sheet, "Requisition Information", "Document Title", 0, 0);
        addField(metadata, sheet, "Requisition Information", "Vessel Name", 2, 1);
        addField(metadata, sheet, "Requisition Information", "IMO Number", 3, 1);
        addField(metadata, sheet, "Requisition Information", "Requisition Number", 4, 1);
        addField(metadata, sheet, "Requisition Information", "Port", 5, 1);
        addField(metadata, sheet, "Requisition Information", "ETA Date", 6, 1);
        addField(metadata, sheet, "Requisition Information", "Request Date", 7, 1);
        
        // Sección: Supplier Information
        addField(metadata, sheet, "Supplier Information", "Supplier Name", 9, 1);
        addField(metadata, sheet, "Supplier Information", "Contact", 10, 1);
        addField(metadata, sheet, "Supplier Information", "Email", 11, 1);
        addField(metadata, sheet, "Supplier Information", "Phone", 12, 1);
        
        return metadata;
    }
    
    /**
     * Método auxiliar para agregar un campo
     */
    private static void addField(List<MetadataField> metadata, Sheet sheet, 
                                 String seccion, String campoNombre, 
                                 int fila, int columna) {
        String valor = getCellValueAsString(sheet, fila, columna);
        if (valor != null && !valor.trim().isEmpty()) {
            metadata.add(new MetadataField(seccion, campoNombre, valor, fila, columna));
        }
    }
    
    /**
     * Obtiene el valor de una celda como String
     */
    private static String getCellValueAsString(Sheet sheet, int rowIdx, int colIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) return null;
        
        Cell cell = row.getCell(colIdx);
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
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return null;
                    }
                }
            default:
                return null;
        }
    }
}
