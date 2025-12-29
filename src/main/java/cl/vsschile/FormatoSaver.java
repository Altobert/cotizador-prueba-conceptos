package cl.vsschile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Herramienta para detectar y guardar formatos de brokers en la base de datos
 */
public class FormatoSaver {
    
    private FormatoDatabaseManager dbManager;
    
    public FormatoSaver(String dbHost, int dbPort, String dbName, String dbUser, String dbPassword) {
        this.dbManager = new FormatoDatabaseManager(dbHost, dbPort, dbName, dbUser, dbPassword);
    }
    
    public FormatoSaver() {
        this.dbManager = new FormatoDatabaseManager();
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java FormatoSaver <directorio-brokers> [db-password]");
            System.out.println("\nEjemplo:");
            System.out.println("  mvn exec:java -Dexec.mainClass=\"cl.vsschile.FormatoSaver\" \\");
            System.out.println("    -Dexec.args=\"/path/to/BROKERS\"");
            System.out.println("\nBase de datos: localhost:5432/sistema_cotizacion_2025");
            return;
        }
        
        String brokersPath = args[0];
        String dbPassword = args.length > 1 ? args[1] : "";
        
        FormatoSaver saver = new FormatoSaver("localhost", 5432, "sistema_cotizacion_2025", "postgres", dbPassword);
        
        // Probar conexión
        System.out.println("==============================================");
        System.out.println("  GUARDAR FORMATOS DE BROKERS EN BD");
        System.out.println("==============================================\n");
        
        System.out.print("Probando conexión a base de datos... ");
        if (!saver.dbManager.testConnection()) {
            System.out.println("✗ FALLO");
            System.err.println("\nNo se pudo conectar a la base de datos.");
            System.err.println("Verificar:");
            System.err.println("  - PostgreSQL está corriendo");
            System.err.println("  - Base de datos 'sistema_cotizacion_2025' existe");
            System.err.println("  - Usuario y contraseña correctos");
            return;
        }
        System.out.println("✓ CONECTADO\n");
        
        File brokersDir = new File(brokersPath);
        if (!brokersDir.exists() || !brokersDir.isDirectory()) {
            System.err.println("Error: El directorio no existe: " + brokersPath);
            return;
        }
        
        saver.processBrokersDirectory(brokersDir);
        
        // Mostrar resumen
        System.out.println("\n==============================================");
        System.out.println("  RESUMEN DE FORMATOS GUARDADOS");
        System.out.println("==============================================\n");
        saver.printFormatosResumen();
    }
    
    public void processBrokersDirectory(File brokersDir) {
        File[] brokerDirs = brokersDir.listFiles();
        if (brokerDirs == null) {
            System.err.println("Error: No se pueden leer los subdirectorios");
            return;
        }
        
        int saved = 0;
        int failed = 0;
        
        for (File brokerDir : brokerDirs) {
            if (brokerDir.isDirectory()) {
                try {
                    if (processBroker(brokerDir)) {
                        saved++;
                    }
                } catch (Exception e) {
                    System.err.println("  ✗ Error: " + e.getMessage());
                    failed++;
                }
            }
        }
        
        System.out.println("\nResultado:");
        System.out.println("  Formatos guardados: " + saved);
        System.out.println("  Errores: " + failed);
    }
    
    private boolean processBroker(File brokerDir) throws Exception {
        String brokerName = brokerDir.getName();
        
        File[] files = brokerDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lower = name.toLowerCase();
                return lower.endsWith(".xlsx") || lower.endsWith(".xls") || lower.endsWith(".xlsm");
            }
        });
        
        if (files == null || files.length == 0) {
            return false;
        }
        
        // Tomar el primer archivo como ejemplo
        File sampleFile = files[0];
        
        System.out.println("Procesando: " + brokerName);
        System.out.println("  Archivo ejemplo: " + sampleFile.getName());
        
        // Detectar columnas
        ColumnDetector.ColumnMapping mapping = detectColumnsFromFile(sampleFile, brokerName);
        
        if (!mapping.isValid()) {
            System.out.println("  ✗ No se pudieron detectar columnas");
            return false;
        }
        
        System.out.println("  ✓ Columnas detectadas: " + mapping.columns.size());
        System.out.println("  ✓ Header en fila: " + (mapping.headerRow + 1));
        
        // Extraer metadata
        List<BrokerMetadataExtractor.MetadataField> metadata = extractMetadataFromFile(sampleFile, brokerName);
        System.out.println("  ✓ Campos de metadata detectados: " + metadata.size());
        
        // Guardar en base de datos
        try {
            int formatoId = dbManager.saveFormato(mapping, sampleFile.getName());
            System.out.println("  ✓ Guardado en BD (formato_id: " + formatoId + ")");
            
            // Guardar metadata
            if (!metadata.isEmpty()) {
                dbManager.saveMetadata(formatoId, metadata);
                System.out.println("  ✓ Metadata guardada en BD");
            }
            
            return true;
        } catch (SQLException e) {
            System.err.println("  ✗ Error guardando en BD: " + e.getMessage());
            throw e;
        }
    }
    
    private ColumnDetector.ColumnMapping detectColumnsFromFile(File file, String brokerName) 
            throws Exception {
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = null;
        
        try {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".xlsx") || fileName.endsWith(".xlsm")) {
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
    
    private List<BrokerMetadataExtractor.MetadataField> extractMetadataFromFile(File file, String brokerName) 
            throws Exception {
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = null;
        
        try {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".xlsx") || fileName.endsWith(".xlsm")) {
                workbook = new XSSFWorkbook(fis);
            } else {
                workbook = new HSSFWorkbook(fis);
            }
            
            Sheet sheet = workbook.getSheetAt(0);
            return BrokerMetadataExtractor.extractMetadata(sheet, brokerName);
            
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            fis.close();
        }
    }
    
    public void printFormatosResumen() {
        try {
            List<FormatoDatabaseManager.FormatoInfo> formatos = dbManager.getFormatosActivos();
            
            if (formatos.isEmpty()) {
                System.out.println("No hay formatos guardados.");
                return;
            }
            
            for (FormatoDatabaseManager.FormatoInfo formato : formatos) {
                System.out.println("┌─────────────────────────────────────────────");
                System.out.println("│ " + formato.brokerName);
                System.out.println("├─────────────────────────────────────────────");
                System.out.println("│ ID: " + formato.formatoId);
                System.out.println("│ Versión: " + formato.version);
                System.out.println("│ Header Row: " + (formato.headerRow + 1));
                System.out.println("│ Total Columnas: " + formato.totalColumnas);
                
                // Obtener columnas
                List<FormatoDatabaseManager.ColumnaInfo> columnas = dbManager.getColumnasByFormato(formato.formatoId);
                if (!columnas.isEmpty()) {
                    System.out.println("│");
                    System.out.println("│ Columnas:");
                    for (FormatoDatabaseManager.ColumnaInfo col : columnas) {
                        System.out.println("│   " + col.toString());
                    }
                }
                System.out.println("└─────────────────────────────────────────────\n");
            }
            
        } catch (SQLException e) {
            System.err.println("Error consultando formatos: " + e.getMessage());
        }
    }
}
