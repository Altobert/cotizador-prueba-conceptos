package cl.vsschile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Analizador de bordes, estilos e imágenes del archivo BSM CATERING original
 * Equivalente a analizar_bordes_bsm.py
 */
public class BSMBorderAnalyzer {
    
    public static void main(String[] args) {
        String archivoPath = "/Users/albertosanmartin/Documents/VSS-COTIZACIONES/brokers28-12-2025/BSM CATERING/RFQ_EQ_SCF_BAA_0031.xlsm";
        
        try (FileInputStream fis = new FileInputStream(archivoPath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            System.out.println("Archivo: " + archivoPath);
            System.out.println("Hoja activa: " + sheet.getSheetName());
            System.out.println("=".repeat(100));
            
            // Analizar bordes en las primeras 18 filas
            System.out.println("\n### BORDES Y LÍNEAS EN METADATA ###\n");
            
            for (int rowIdx = 0; rowIdx < 18; rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;
                
                System.out.println("\n--- FILA " + (rowIdx + 1) + " ---");
                
                // Revisar todas las columnas hasta V (22)
                for (int colIdx = 0; colIdx < 22; colIdx++) {
                    Cell cell = row.getCell(colIdx);
                    if (cell == null) continue;
                    
                    CellStyle style = cell.getCellStyle();
                    if (style == null) continue;
                    
                    // Revisar bordes
                    StringBuilder borderInfo = new StringBuilder();
                    
                    if (style.getBorderBottomEnum() != BorderStyle.NONE) {
                        borderInfo.append("Bottom: ").append(style.getBorderBottomEnum()).append(" ");
                    }
                    if (style.getBorderTopEnum() != BorderStyle.NONE) {
                        borderInfo.append("Top: ").append(style.getBorderTopEnum()).append(" ");
                    }
                    if (style.getBorderLeftEnum() != BorderStyle.NONE) {
                        borderInfo.append("Left: ").append(style.getBorderLeftEnum()).append(" ");
                    }
                    if (style.getBorderRightEnum() != BorderStyle.NONE) {
                        borderInfo.append("Right: ").append(style.getBorderRightEnum()).append(" ");
                    }
                    
                    if (borderInfo.length() > 0) {
                        String cellRef = getCellReference(rowIdx, colIdx);
                        System.out.println("  " + cellRef + ": " + borderInfo.toString().trim());
                    }
                }
            }
            
            // Buscar imágenes
            System.out.println("\n\n### IMÁGENES EN EL WORKSHEET ###\n");
            
            if (workbook instanceof XSSFWorkbook) {
                XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
                XSSFDrawing drawing = xssfWorkbook.getSheetAt(0).createDrawingPatriarch();
                
                if (drawing != null) {
                    List<XSSFShape> shapes = drawing.getShapes();
                    int imgCount = 0;
                    
                    for (XSSFShape shape : shapes) {
                        if (shape instanceof XSSFPicture) {
                            imgCount++;
                            XSSFPicture picture = (XSSFPicture) shape;
                            
                            System.out.println("Imagen " + imgCount + ":");
                            
                            // Obtener datos de la imagen
                            byte[] imageData = picture.getPictureData().getData();
                            
                            System.out.println("  Tamaño: " + imageData.length + " bytes");
                            System.out.println("  Tipo: " + picture.getPictureData().suggestFileExtension());
                            
                            // Guardar la imagen
                            String outputPath = "/Users/albertosanmartin/Documents/VSS-COTIZACIONES/cotizacion-organizer/bsm_logo.jpeg";
                            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                                fos.write(imageData);
                                System.out.println("  Guardada en: " + outputPath);
                            }
                        }
                    }
                    
                    if (imgCount == 0) {
                        System.out.println("No se encontraron imágenes en el worksheet");
                    }
                } else {
                    System.out.println("No se encontraron imágenes en el worksheet");
                }
            }
            
            // Analizar fills en filas específicas
            System.out.println("\n\n### FILLS Y BACKGROUNDS DETALLADOS ###\n");
            
            int[] filasAnalizar = {3, 12, 18, 19};
            for (int rowNum : filasAnalizar) {
                Row row = sheet.getRow(rowNum - 1); // 0-based
                if (row == null) continue;
                
                System.out.println("\n--- FILA " + rowNum + " ---");
                
                for (int colIdx = 0; colIdx < 22; colIdx++) {
                    Cell cell = row.getCell(colIdx);
                    if (cell == null) continue;
                    
                    CellStyle style = cell.getCellStyle();
                    if (style == null) continue;
                    
                    FillPatternType fillPattern = style.getFillPatternEnum();
                    if (fillPattern != null && fillPattern != FillPatternType.NO_FILL) {
                        String cellRef = getCellReference(rowNum - 1, colIdx);
                        
                        // Obtener color de fondo
                        short bgColor = style.getFillForegroundColor();
                        
                        System.out.println("  " + cellRef + ": Fill=" + bgColor + 
                                         ", Type=" + fillPattern);
                    }
                }
            }
            
            System.out.println("\n✅ Análisis completado");
            
        } catch (IOException e) {
            System.err.println("Error al analizar el archivo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Convierte índices de fila/columna a referencia de celda (ej: A1, B2)
     */
    private static String getCellReference(int rowIdx, int colIdx) {
        StringBuilder ref = new StringBuilder();
        
        // Convertir columna a letra(s)
        int col = colIdx;
        while (col >= 0) {
            ref.insert(0, (char) ('A' + (col % 26)));
            col = (col / 26) - 1;
        }
        
        // Agregar número de fila (1-based)
        ref.append(rowIdx + 1);
        
        return ref.toString();
    }
}
