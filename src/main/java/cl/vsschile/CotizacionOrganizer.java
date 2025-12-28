package cl.vsschile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.util.*;

/**
 * Organiza cotizaciones de diferentes brokers por cliente/vessel
 */
public class CotizacionOrganizer {
    
    private static final String OUTPUT_DIR = "cotizaciones-por-cliente";
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java CotizacionOrganizer <directorio-brokers>");
            return;
        }
        
        String brokersDir = args[0];
        CotizacionOrganizer organizer = new CotizacionOrganizer();
        organizer.processBrokersDirectory(brokersDir);
    }
    
    public void processBrokersDirectory(String brokersPath) {
        File brokersDir = new File(brokersPath);
        
        if (!brokersDir.exists() || !brokersDir.isDirectory()) {
            System.err.println("Error: El directorio no existe: " + brokersPath);
            return;
        }
        
        // Crear directorio de salida
        File outputDir = new File(brokersDir.getParent(), OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        System.out.println("Procesando cotizaciones...\n");
        
        // Procesar cada broker
        File[] brokerDirs = brokersDir.listFiles();
        if (brokerDirs == null) return;
        
        for (File brokerDir : brokerDirs) {
            if (brokerDir.isDirectory()) {
                processBroker(brokerDir, outputDir);
            }
        }
        
        System.out.println("\nProceso completado. Archivos organizados en: " + outputDir.getAbsolutePath());
    }
    
    private void processBroker(File brokerDir, File outputDir) {
        String brokerName = brokerDir.getName();
        System.out.println("Procesando broker: " + brokerName);
        
        File[] files = brokerDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lower = name.toLowerCase();
                return lower.endsWith(".xlsx") || lower.endsWith(".xls");
            }
        });
        
        if (files == null || files.length == 0) {
            System.out.println("  - No hay archivos Excel");
            return;
        }
        
        for (File file : files) {
            try {
                processQuotation(file, brokerName, outputDir);
            } catch (Exception e) {
                System.err.println("  Error procesando " + file.getName() + ": " + e.getMessage());
            }
        }
    }
    
    private void processQuotation(File file, String brokerName, File outputDir) throws Exception {
        QuotationInfo info = extractQuotationInfo(file, brokerName);
        
        if (info.vesselName != null || info.imoNumber != null) {
            copyToClientDirectory(file, info, outputDir);
            System.out.println("  ✓ " + file.getName() + " -> Cliente: " + info.getClientKey());
        } else {
            System.out.println("  ⚠ " + file.getName() + " (no se encontró información de cliente)");
        }
    }
    
    private QuotationInfo extractQuotationInfo(File file, String brokerName) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = null;
        
        try {
            if (file.getName().toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else {
                workbook = new HSSFWorkbook(fis);
            }
            
            Sheet sheet = workbook.getSheetAt(0);
            QuotationInfo info = new QuotationInfo();
            info.broker = brokerName;
            info.originalFileName = file.getName();
            
            // Detectar formato y extraer información
            if (brokerName.contains("MCTC")) {
                extractMCTCInfo(sheet, info);
            } else if (brokerName.contains("OCEANIC")) {
                extractOceanicInfo(sheet, info);
            } else if (brokerName.contains("CMA")) {
                extractCMAInfo(sheet, info);
            } else if (brokerName.contains("GARRETS")) {
                extractGarretsInfo(sheet, info);
            } else if (brokerName.contains("PROCURESHIP")) {
                extractProcureshipInfo(sheet, info);
            } else {
                // Formato genérico
                extractGenericInfo(sheet, info);
            }
            
            return info;
            
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            fis.close();
        }
    }
    
    private void extractMCTCInfo(Sheet sheet, QuotationInfo info) {
        // MCTC: VESSEL'S NAME en fila 2, IMO NUMBER en fila 3
        Row row2 = sheet.getRow(1);
        if (row2 != null) {
            Cell cell = row2.getCell(4);
            if (cell != null) {
                info.vesselName = getCellValueAsString(cell);
            }
        }
        
        Row row3 = sheet.getRow(2);
        if (row3 != null) {
            Cell cell = row3.getCell(4);
            if (cell != null) {
                info.imoNumber = getCellValueAsString(cell);
            }
        }
        
        // Número de cotización en fila 8
        Row row8 = sheet.getRow(7);
        if (row8 != null) {
            Cell cell = row8.getCell(3);
            if (cell != null) {
                info.quotationNumber = getCellValueAsString(cell);
            }
        }
    }
    
    private void extractOceanicInfo(Sheet sheet, QuotationInfo info) {
        // Buscar "Vessel" en las primeras 15 filas
        for (int i = 0; i < 15; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            for (Cell cell : row) {
                String value = getCellValueAsString(cell);
                if (value != null && value.equalsIgnoreCase("Vessel")) {
                    Cell nextCell = row.getCell(cell.getColumnIndex() + 1);
                    if (nextCell != null) {
                        info.vesselName = getCellValueAsString(nextCell);
                    }
                } else if (value != null && value.contains("Quotation Request #")) {
                    Cell nextCell = row.getCell(cell.getColumnIndex() + 1);
                    if (nextCell != null) {
                        info.quotationNumber = getCellValueAsString(nextCell);
                    }
                }
            }
        }
    }
    
    private void extractCMAInfo(Sheet sheet, QuotationInfo info) {
        // CMA: buscar "Vessel" en las primeras filas
        for (int i = 0; i < 10; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            for (Cell cell : row) {
                String value = getCellValueAsString(cell);
                if (value != null) {
                    if (value.trim().equalsIgnoreCase("Vessel")) {
                        Cell nextCell = row.getCell(cell.getColumnIndex() + 1);
                        if (nextCell != null) {
                            info.vesselName = getCellValueAsString(nextCell);
                        }
                    }
                }
            }
        }
    }
    
    private void extractGarretsInfo(Sheet sheet, QuotationInfo info) {
        // RFQ number en fila 7
        Row row7 = sheet.getRow(6);
        if (row7 != null) {
            Cell cell = row7.getCell(8);
            if (cell != null) {
                info.quotationNumber = getCellValueAsString(cell);
            }
        }
        
        // Buscar columna "Vessel" en headers
        for (int i = 20; i < 30; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            for (Cell cell : row) {
                String value = getCellValueAsString(cell);
                if (value != null && value.equalsIgnoreCase("Vessel")) {
                    // Encontrar primera fila con datos
                    for (int j = i + 1; j < i + 10; j++) {
                        Row dataRow = sheet.getRow(j);
                        if (dataRow != null) {
                            Cell vesselCell = dataRow.getCell(cell.getColumnIndex());
                            if (vesselCell != null) {
                                String vessel = getCellValueAsString(vesselCell);
                                if (vessel != null && !vessel.trim().isEmpty()) {
                                    info.vesselName = vessel;
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void extractProcureshipInfo(Sheet sheet, QuotationInfo info) {
        // Procureship: RFQ en fila 5, Vessel e IMO en columnas de la derecha
        Row row5 = sheet.getRow(4);
        if (row5 != null) {
            for (Cell cell : row5) {
                String value = getCellValueAsString(cell);
                if (value != null && value.contains("Requisition No.")) {
                    Cell nextCell = row5.getCell(cell.getColumnIndex() + 1);
                    if (nextCell != null) {
                        info.quotationNumber = getCellValueAsString(nextCell);
                    }
                }
            }
        }
        
        // Buscar Vessel e IMO en filas 6-7
        for (int i = 5; i < 8; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            for (Cell cell : row) {
                String value = getCellValueAsString(cell);
                if (value != null) {
                    if (value.trim().equals("Vessel:")) {
                        Cell nextCell = row.getCell(cell.getColumnIndex() + 1);
                        if (nextCell != null) {
                            info.vesselName = getCellValueAsString(nextCell);
                        }
                    } else if (value.trim().equals("IMO:")) {
                        Cell nextCell = row.getCell(cell.getColumnIndex() + 1);
                        if (nextCell != null) {
                            info.imoNumber = getCellValueAsString(nextCell);
                        }
                    }
                }
            }
        }
    }
    
    private void extractGenericInfo(Sheet sheet, QuotationInfo info) {
        // Búsqueda genérica de campos comunes
        for (int i = 0; i < 30; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            for (Cell cell : row) {
                String value = getCellValueAsString(cell);
                if (value == null) continue;
                
                String lower = value.toLowerCase().trim();
                
                if ((lower.equals("vessel") || lower.equals("vessel name") || 
                     lower.equals("vessel's name") || lower.contains("buque")) && 
                    info.vesselName == null) {
                    Cell nextCell = row.getCell(cell.getColumnIndex() + 1);
                    if (nextCell != null) {
                        info.vesselName = getCellValueAsString(nextCell);
                    }
                } else if ((lower.equals("imo") || lower.equals("imo number") || 
                           lower.contains("imo no")) && info.imoNumber == null) {
                    Cell nextCell = row.getCell(cell.getColumnIndex() + 1);
                    if (nextCell != null) {
                        info.imoNumber = getCellValueAsString(nextCell);
                    }
                }
            }
        }
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue().trim();
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    private void copyToClientDirectory(File sourceFile, QuotationInfo info, File outputDir) throws IOException {
        String clientDirName = info.getClientKey();
        File clientDir = new File(outputDir, clientDirName);
        
        if (!clientDir.exists()) {
            clientDir.mkdirs();
        }
        
        // Crear nombre de archivo con broker
        String newFileName = info.broker + "_" + sourceFile.getName();
        File destFile = new File(clientDir, newFileName);
        
        // Copiar archivo
        copyFile(sourceFile, destFile);
        
        // Crear archivo de metadata
        createMetadataFile(clientDir, info);
    }
    
    private void copyFile(File source, File dest) throws IOException {
        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(dest);
        
        byte[] buffer = new byte[8192];
        int length;
        
        while ((length = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        
        fis.close();
        fos.close();
    }
    
    private void createMetadataFile(File clientDir, QuotationInfo info) throws IOException {
        File metadataFile = new File(clientDir, "metadata.txt");
        FileWriter writer = new FileWriter(metadataFile, true);
        
        writer.write("=====================================\n");
        writer.write("Archivo: " + info.originalFileName + "\n");
        writer.write("Broker: " + info.broker + "\n");
        if (info.vesselName != null) {
            writer.write("Vessel: " + info.vesselName + "\n");
        }
        if (info.imoNumber != null) {
            writer.write("IMO: " + info.imoNumber + "\n");
        }
        if (info.quotationNumber != null) {
            writer.write("Cotización: " + info.quotationNumber + "\n");
        }
        writer.write("Fecha procesado: " + new Date() + "\n");
        writer.write("\n");
        
        writer.close();
    }
    
    /**
     * Clase para almacenar información de cotización
     */
    private static class QuotationInfo {
        String broker;
        String vesselName;
        String imoNumber;
        String quotationNumber;
        String originalFileName;
        
        String getClientKey() {
            if (imoNumber != null && !imoNumber.trim().isEmpty()) {
                return "IMO_" + imoNumber.trim().replaceAll("[^0-9]", "");
            } else if (vesselName != null && !vesselName.trim().isEmpty()) {
                return vesselName.trim()
                    .replaceAll("[^a-zA-Z0-9\\s]", "")
                    .replaceAll("\\s+", "_")
                    .toUpperCase();
            } else if (quotationNumber != null && !quotationNumber.trim().isEmpty()) {
                return "QUOTE_" + quotationNumber.trim()
                    .replaceAll("[^a-zA-Z0-9]", "_");
            } else {
                return "UNKNOWN_" + System.currentTimeMillis();
            }
        }
    }
}
