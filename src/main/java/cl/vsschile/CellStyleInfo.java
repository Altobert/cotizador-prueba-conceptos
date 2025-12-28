package cl.vsschile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

/**
 * Información de estilo y color de una celda Excel
 */
public class CellStyleInfo {
    
    public String backgroundColor;
    public String foregroundColor;
    public boolean isBold;
    public boolean isItalic;
    public boolean hasBorder;
    
    /**
     * Extrae información de estilo de una celda
     */
    public static CellStyleInfo fromCell(Cell cell) {
        CellStyleInfo info = new CellStyleInfo();
        
        if (cell == null) {
            return info;
        }
        
        CellStyle style = cell.getCellStyle();
        if (style == null) {
            return info;
        }
        
        // Extraer color de fondo
        info.backgroundColor = extractBackgroundColor(style, cell.getSheet().getWorkbook());
        
        // Extraer color de texto (foreground)
        info.foregroundColor = extractForegroundColor(style, cell.getSheet().getWorkbook());
        
        // Extraer información de fuente
        Workbook wb = cell.getSheet().getWorkbook();
        Font font = wb.getFontAt(style.getFontIndex());
        if (font != null) {
            info.isBold = font.getBold();
            info.isItalic = font.getItalic();
        }
        
        // Verificar bordes (en POI 3.17 los bordes son short)
        // 0 = sin borde, cualquier otro valor = con borde
        info.hasBorder = style.getBorderTop() != 0 ||
                        style.getBorderBottom() != 0 ||
                        style.getBorderLeft() != 0 ||
                        style.getBorderRight() != 0;
        
        return info;
    }
    
    private static String extractBackgroundColor(CellStyle style, Workbook wb) {
        try {
            // Para XLSX (XSSFWorkbook)
            if (style instanceof org.apache.poi.xssf.usermodel.XSSFCellStyle) {
                org.apache.poi.xssf.usermodel.XSSFCellStyle xssfStyle = 
                    (org.apache.poi.xssf.usermodel.XSSFCellStyle) style;
                
                XSSFColor color = xssfStyle.getFillForegroundXSSFColor();
                if (color != null) {
                    byte[] rgb = color.getRGB();
                    if (rgb != null && rgb.length >= 3) {
                        return String.format("#%02X%02X%02X", 
                            rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF);
                    }
                }
            }
            
            // Para XLS (HSSFWorkbook) - colores indexados
            short colorIndex = style.getFillForegroundColor();
            if (colorIndex != IndexedColors.AUTOMATIC.getIndex()) {
                return getColorNameFromIndex(colorIndex);
            }
            
        } catch (Exception e) {
            // Ignorar errores de extracción de color
        }
        
        return null;
    }
    
    private static String extractForegroundColor(CellStyle style, Workbook wb) {
        try {
            Font font = wb.getFontAt(style.getFontIndex());
            
            // Para XLSX
            if (font instanceof XSSFFont) {
                XSSFFont xssfFont = (XSSFFont) font;
                XSSFColor color = xssfFont.getXSSFColor();
                if (color != null) {
                    byte[] rgb = color.getRGB();
                    if (rgb != null && rgb.length >= 3) {
                        return String.format("#%02X%02X%02X", 
                            rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF);
                    }
                }
            }
            
            // Para XLS - colores indexados
            short colorIndex = font.getColor();
            if (colorIndex != IndexedColors.AUTOMATIC.getIndex()) {
                return getColorNameFromIndex(colorIndex);
            }
            
        } catch (Exception e) {
            // Ignorar errores
        }
        
        return null;
    }
    
    private static String getColorNameFromIndex(short index) {
        // Mapeo de colores comunes indexados
        if (index == IndexedColors.BLACK.getIndex()) return "#000000";
        if (index == IndexedColors.WHITE.getIndex()) return "#FFFFFF";
        if (index == IndexedColors.RED.getIndex()) return "#FF0000";
        if (index == IndexedColors.GREEN.getIndex()) return "#00FF00";
        if (index == IndexedColors.BLUE.getIndex()) return "#0000FF";
        if (index == IndexedColors.YELLOW.getIndex()) return "#FFFF00";
        if (index == IndexedColors.PINK.getIndex()) return "#FF00FF";
        if (index == IndexedColors.TURQUOISE.getIndex()) return "#00FFFF";
        if (index == IndexedColors.DARK_RED.getIndex()) return "#800000";
        if (index == IndexedColors.GREY_25_PERCENT.getIndex()) return "#C0C0C0";
        if (index == IndexedColors.GREY_50_PERCENT.getIndex()) return "#808080";
        if (index == IndexedColors.LIGHT_BLUE.getIndex()) return "#ADD8E6";
        if (index == IndexedColors.LIGHT_GREEN.getIndex()) return "#90EE90";
        if (index == IndexedColors.LIGHT_ORANGE.getIndex()) return "#FFD700";
        if (index == IndexedColors.LIGHT_YELLOW.getIndex()) return "#FFFFE0";
        if (index == IndexedColors.ORANGE.getIndex()) return "#FFA500";
        if (index == IndexedColors.BROWN.getIndex()) return "#A52A2A";
        if (index == IndexedColors.LIME.getIndex()) return "#00FF00";
        if (index == IndexedColors.LAVENDER.getIndex()) return "#E6E6FA";
        if (index == IndexedColors.TAN.getIndex()) return "#D2B48C";
        if (index == IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()) return "#6495ED";
        if (index == IndexedColors.PALE_BLUE.getIndex()) return "#AFEEEE";
        if (index == IndexedColors.ROSE.getIndex()) return "#FF007F";
        if (index == IndexedColors.VIOLET.getIndex()) return "#EE82EE";
        if (index == IndexedColors.GOLD.getIndex()) return "#FFD700";
        if (index == IndexedColors.SEA_GREEN.getIndex()) return "#2E8B57";
        if (index == IndexedColors.DARK_BLUE.getIndex()) return "#00008B";
        if (index == IndexedColors.DARK_GREEN.getIndex()) return "#006400";
        if (index == IndexedColors.DARK_YELLOW.getIndex()) return "#808000";
        if (index == IndexedColors.BRIGHT_GREEN.getIndex()) return "#00FF00";
        if (index == IndexedColors.MAROON.getIndex()) return "#800000";
        if (index == IndexedColors.INDIGO.getIndex()) return "#4B0082";
        
        // Por defecto, retornar el índice
        return "INDEX_" + index;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (backgroundColor != null) {
            sb.append("BG:").append(backgroundColor).append(" ");
        }
        if (foregroundColor != null) {
            sb.append("FG:").append(foregroundColor).append(" ");
        }
        if (isBold) sb.append("Bold ");
        if (isItalic) sb.append("Italic ");
        if (hasBorder) sb.append("Border");
        return sb.toString().trim();
    }
    
    public boolean hasStyle() {
        return backgroundColor != null || foregroundColor != null || 
               isBold || isItalic || hasBorder;
    }
}
