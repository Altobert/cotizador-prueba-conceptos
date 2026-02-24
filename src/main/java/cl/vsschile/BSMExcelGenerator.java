package cl.vsschile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.util.IOUtils;

import java.io.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Generador de archivo Excel BSM CATERING con formato idéntico al original
 * Equivalente a generar_bsm_mejorado.py
 */
public class BSMExcelGenerator {
    
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    
    // Estilos de fuente
    private XSSFFont fontCalibri10;
    private XSSFFont fontCalibri10Bold;
    private XSSFFont fontCalibri11;
    private XSSFFont fontCalibri11Bold;
    private XSSFFont fontCalibri15Bold;
    private XSSFFont fontCalibri36Bold;
    private XSSFFont fontCalibri8;
    private XSSFFont fontCalibri9Bold;
    
    // Colores
    private XSSFColor colorAzul1F497D;
    private XSSFColor colorGris4B7279;
    private XSSFColor colorAzulLink;
    private XSSFColor colorHeader9DBEC3;
    private XSSFColor colorAmarillo;
    private XSSFColor colorFondo;
    
    // Estilos de borde
    private BorderStyle borderThin = BorderStyle.THIN;
    private BorderStyle borderMedium = BorderStyle.MEDIUM;
    private BorderStyle borderThick = BorderStyle.THICK;
    
    public BSMExcelGenerator() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Quote");
        
        inicializarFuentes();
        inicializarColores();
        configurarDimensiones();
    }
    
    private void inicializarFuentes() {
        fontCalibri8 = workbook.createFont();
        fontCalibri8.setFontName("Calibri");
        fontCalibri8.setFontHeightInPoints((short) 8);
        
        fontCalibri9Bold = workbook.createFont();
        fontCalibri9Bold.setFontName("Calibri");
        fontCalibri9Bold.setFontHeightInPoints((short) 9);
        fontCalibri9Bold.setBold(true);
        
        fontCalibri10 = workbook.createFont();
        fontCalibri10.setFontName("Calibri");
        fontCalibri10.setFontHeightInPoints((short) 10);
        
        fontCalibri10Bold = workbook.createFont();
        fontCalibri10Bold.setFontName("Calibri");
        fontCalibri10Bold.setFontHeightInPoints((short) 10);
        fontCalibri10Bold.setBold(true);
        
        fontCalibri11 = workbook.createFont();
        fontCalibri11.setFontName("Calibri");
        fontCalibri11.setFontHeightInPoints((short) 11);
        
        fontCalibri11Bold = workbook.createFont();
        fontCalibri11Bold.setFontName("Calibri");
        fontCalibri11Bold.setFontHeightInPoints((short) 11);
        fontCalibri11Bold.setBold(true);
        
        fontCalibri15Bold = workbook.createFont();
        fontCalibri15Bold.setFontName("Calibri");
        fontCalibri15Bold.setFontHeightInPoints((short) 15);
        fontCalibri15Bold.setBold(true);
        
        fontCalibri36Bold = workbook.createFont();
        fontCalibri36Bold.setFontName("Calibri");
        fontCalibri36Bold.setFontHeightInPoints((short) 36);
        fontCalibri36Bold.setBold(true);
    }
    
    private void inicializarColores() {
        colorAzul1F497D = new XSSFColor(new byte[]{(byte)0x1F, (byte)0x49, (byte)0x7D}, null);
        colorGris4B7279 = new XSSFColor(new byte[]{(byte)0x4B, (byte)0x72, (byte)0x79}, null);
        colorAzulLink = new XSSFColor(new byte[]{(byte)0x05, (byte)0x63, (byte)0xC1}, null);
        colorHeader9DBEC3 = new XSSFColor(new byte[]{(byte)0x9D, (byte)0xBE, (byte)0xC3}, null);
        colorAmarillo = new XSSFColor(new byte[]{(byte)0xFF, (byte)0xFF, (byte)0x00}, null);
        colorFondo = new XSSFColor(new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF}, null);
    }
    
    private void configurarDimensiones() {
        // Anchos de columna (en unidades de Excel)
        sheet.setColumnWidth(0, 7 * 256);   // A
        sheet.setColumnWidth(1, 10 * 256);  // B
        sheet.setColumnWidth(2, 11 * 256);  // C
        sheet.setColumnWidth(3, 30 * 256);  // D
        sheet.setColumnWidth(4, 15 * 256);  // E
        sheet.setColumnWidth(5, 10 * 256);  // F
        sheet.setColumnWidth(6, 8 * 256);   // G
        sheet.setColumnWidth(7, 8 * 256);   // H
        sheet.setColumnWidth(8, 10 * 256);  // I
        sheet.setColumnWidth(9, 12 * 256);  // J
        sheet.setColumnWidth(10, 10 * 256); // K
        sheet.setColumnWidth(11, 8 * 256);  // L
        sheet.setColumnWidth(12, 8 * 256);  // M
        sheet.setColumnWidth(13, 10 * 256); // N
        sheet.setColumnWidth(14, 8 * 256);  // O
        sheet.setColumnWidth(15, 10 * 256); // P
        sheet.setColumnWidth(16, 8 * 256);  // Q
        sheet.setColumnWidth(17, 8 * 256);  // R
        sheet.setColumnWidth(18, 15 * 256); // S
        sheet.setColumnWidth(19, 8 * 256);  // T
        sheet.setColumnWidth(20, 15 * 256); // U
    }
    
    public void generarExcel(String rutaOutput) throws IOException {
        // Agregar logo BSM
        agregarLogo();
        
        // Generar contenido
        crearFila1Titulo();
        crearFila3VesselDetails();
        crearFilas4a6CompanyInfo();
        crearFilas7a11ContactInfo();
        crearFila12VendorDetails();
        crearFilas13a17VendorInfo();
        crearFila19Headers();
        crearFila20Provisions();
        crearFila21DatosEjemplo();
        
        // Guardar archivo
        try (FileOutputStream fos = new FileOutputStream(rutaOutput)) {
            workbook.write(fos);
        }
        
        workbook.close();
        
        System.out.println("✅ Archivo generado exitosamente: " + rutaOutput);
        System.out.println("\nEstructura del archivo:");
        System.out.println("- Formato idéntico al original BSM CATERING");
        System.out.println("- Logo BSM incluido");
        System.out.println("- Líneas resaltadas negras (bordes thick y medium)");
        System.out.println("- Header row: Fila 19");
        System.out.println("- Celdas mezcladas: Múltiples secciones");
        System.out.println("- Metadata completa con formato original");
        System.out.println("- Columnas A-V (incluyendo columna B con datos)");
    }
    
    private void agregarLogo() {
        try {
            InputStream logoStream = new FileInputStream("/Users/albertosanmartin/Documents/VSS-COTIZACIONES/cotizacion-organizer/bsm_logo.jpeg");
            byte[] bytes = IOUtils.toByteArray(logoStream);
            int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
            logoStream.close();
            
            CreationHelper helper = workbook.getCreationHelper();
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = helper.createClientAnchor();
            
            // Posición A1
            anchor.setCol1(0);
            anchor.setRow1(0);
            anchor.setCol2(5);
            anchor.setRow2(2);
            
            Picture pict = drawing.createPicture(anchor, pictureIdx);
            pict.resize();
            
        } catch (IOException e) {
            System.err.println("⚠️  No se pudo agregar el logo: " + e.getMessage());
        }
    }
    
    private void crearFila1Titulo() {
        Row row = getOrCreateRow(0);
        row.setHeightInPoints(51.0f); // Altura de fila 1
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 12, 21)); // M1:V1
        
        Cell cell = row.createCell(12); // M1
        cell.setCellValue("Request for Quote");
        
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 36);
        font.setBold(true);
        font.setColor(colorAzul1F497D);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        cell.setCellStyle(style);
    }
    
    private void crearFila3VesselDetails() {
        Row row = getOrCreateRow(2); // Fila 3 (índice 2)
        row.setHeightInPoints(20.25f); // Altura de fila 3
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 11)); // A3:L3
        
        Cell cell = row.createCell(0);
        cell.setCellValue("Vessel \\ Company Details");
        
        XSSFCellStyle style = crearEstilo(fontCalibri15Bold, colorAzul1F497D, 
                                         HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        cell.setCellStyle(style);
        
        // Agregar borde thick bottom en columnas A-E
        aplicarBordeBottom(row, 0, 4, borderThick);
    }
    
    private void crearFilas4a6CompanyInfo() {
        // FILA 4: Company name y Vessel
        Row row4 = getOrCreateRow(3);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 11)); // A4:L4
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 12, 13)); // M4:N4
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 14, 21)); // O4:V4
        
        Cell cellA4 = row4.createCell(0);
        cellA4.setCellValue("BERNHARD SCHULTE SHIPMANAGEMENT (HONG KONG) LIMITED PARTNERS");
        cellA4.setCellStyle(crearEstilo(fontCalibri11Bold, colorAzul1F497D, 
                                       HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellM4 = row4.createCell(12);
        cellM4.setCellValue("Vessel");
        cellM4.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D, 
                                       HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellO4 = row4.createCell(14);
        cellO4.setCellValue("Barism Alice");
        cellO4.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D, 
                                       HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        // Bordes medium
        aplicarBordeBottom(row4, 0, 9, borderMedium);
        aplicarBordeBottom(row4, 12, 21, borderMedium);
        
        // FILA 5: BSM - CATERING SERVICES y RFQ No.
        Row row5 = getOrCreateRow(4);
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 11));
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 12, 13));
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 14, 21));
        
        Cell cellA5 = row5.createCell(0);
        cellA5.setCellValue("BSM - CATERING SERVICES,");
        cellA5.setCellStyle(crearEstilo(fontCalibri11Bold, colorAzul1F497D, 
                                       HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellM5 = row5.createCell(12);
        cellM5.setCellValue("RFQ No.");
        cellM5.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D, 
                                       HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellO5 = row5.createCell(14);
        cellO5.setCellValue("EQ/SCF/BAA/0031");
        cellO5.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D, 
                                       HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        aplicarBordeBottom(row5, 12, 21, borderMedium);
        
        // FILA 6: BERNHARD SCHULTE SHIPMANAGEMENT y RFQ Date
        Row row6 = getOrCreateRow(5);
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 11));
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 12, 13));
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 14, 21));
        
        Cell cellA6 = row6.createCell(0);
        cellA6.setCellValue("BERNHARD SCHULTE SHIPMANAGEMENT (L) LTD.,");
        cellA6.setCellStyle(crearEstilo(fontCalibri11Bold, colorAzul1F497D, 
                                       HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellM6 = row6.createCell(12);
        cellM6.setCellValue("RFQ Date");
        cellM6.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D, 
                                       HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellO6 = row6.createCell(14);
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.DECEMBER, 11);
        cellO6.setCellValue(cal.getTime());
        
        XSSFCellStyle dateStyle = crearEstilo(fontCalibri10Bold, colorAzul1F497D, 
                                              HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("DD/MM/YYYY"));
        cellO6.setCellStyle(dateStyle);
        
        aplicarBordeBottom(row6, 0, 6, borderMedium);
        aplicarBordeBottom(row6, 12, 21, borderMedium);
    }
    
    private void crearFilas7a11ContactInfo() {
        // FILA 7
        Row row7 = getOrCreateRow(6);
        sheet.addMergedRegion(new CellRangeAddress(6, 6, 0, 11));
        
        Cell cellA7 = row7.createCell(0);
        cellA7.setCellValue("C\\O Bernhard Schulte Shipmanagement (India) Pvt. Limited");
        cellA7.setCellStyle(crearEstilo(fontCalibri10, colorGris4B7279, 
                                       HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        aplicarBordeBottom(row7, 12, 21, borderMedium);
        
        // FILA 8
        Row row8 = getOrCreateRow(7);
        sheet.addMergedRegion(new CellRangeAddress(7, 7, 0, 11));
        sheet.addMergedRegion(new CellRangeAddress(7, 7, 12, 13));
        sheet.addMergedRegion(new CellRangeAddress(7, 7, 14, 21));
        
        Cell cellA8 = row8.createCell(0);
        cellA8.setCellValue("401 Olympia, Hiranandani Gardens, Powai, Mumbai 400 076, India");
        cellA8.setCellStyle(crearEstilo(fontCalibri10, colorGris4B7279, 
                                       HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellM8 = row8.createCell(12);
        cellM8.setCellValue("Submit Quote Before:");
        cellM8.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D, 
                                       HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellO8 = row8.createCell(14);
        Calendar cal8 = Calendar.getInstance();
        cal8.set(2025, Calendar.DECEMBER, 11);
        cellO8.setCellValue(cal8.getTime());
        
        XSSFCellStyle dateStyle8 = crearEstilo(fontCalibri10Bold, colorAzul1F497D, 
                                               HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        dateStyle8.setDataFormat(workbook.createDataFormat().getFormat("DD/MM/YYYY"));
        cellO8.setCellStyle(dateStyle8);
        
        aplicarBordeBottom(row8, 12, 21, borderMedium);
        
        // FILA 9: Tel/Fax y Port of Delivery
        Row row9 = getOrCreateRow(8);
        sheet.addMergedRegion(new CellRangeAddress(8, 8, 0, 10)); // A9:K9
        
        Cell cellA9 = row9.createCell(0);
        cellA9.setCellValue("Tel: +91-22-40017300        Fax: +91-22-40017555");
        cellA9.setCellStyle(crearEstilo(fontCalibri10, colorGris4B7279,
                                       HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellL9 = row9.createCell(11);
        cellL9.setCellValue("Port of Delivery");
        cellL9.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                       HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        aplicarBordeBottom(row9, 0, 8, borderMedium);
        
        // FILA 10: Email y Vessel ETA
        Row row10 = getOrCreateRow(9);
        Cell cellA10 = row10.createCell(0);
        cellA10.setCellValue("Email:");
        cellA10.setCellStyle(crearEstilo(fontCalibri10, colorGris4B7279,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        sheet.addMergedRegion(new CellRangeAddress(9, 9, 2, 11)); // C10:L10
        Cell cellC10 = row10.createCell(2);
        cellC10.setCellValue("seachef@seachef.com");
        cellC10.setCellStyle(crearEstilo(fontCalibri11, colorAzul1F497D,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellM10 = row10.createCell(12);
        cellM10.setCellValue("Vessel ETA");
        cellM10.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        sheet.addMergedRegion(new CellRangeAddress(9, 9, 14, 21)); // O10:V10
        Cell cellO10 = row10.createCell(14);
        Calendar cal10 = Calendar.getInstance();
        cal10.set(2025, Calendar.DECEMBER, 12, 12, 0);
        cellO10.setCellValue(cal10.getTime());
        
        XSSFCellStyle dateTimeStyle = crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                                  HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        dateTimeStyle.setDataFormat(workbook.createDataFormat().getFormat("DD/MM/YYYY HH:MM"));
        cellO10.setCellStyle(dateTimeStyle);
        
        aplicarBordeBottom(row10, 0, 6, borderMedium);
        aplicarBordeBottom(row10, 12, 21, borderMedium);
        
        // FILA 11: Web y Payment Terms
        Row row11 = getOrCreateRow(10);
        Cell cellA11 = row11.createCell(0);
        cellA11.setCellValue("Web:");
        cellA11.setCellStyle(crearEstilo(fontCalibri10, colorGris4B7279,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        sheet.addMergedRegion(new CellRangeAddress(10, 10, 2, 11)); // C11:L11
        Cell cellC11 = row11.createCell(2);
        cellC11.setCellValue("www.seachef.com /  www.bs-shipmanagement.com");
        cellC11.setCellStyle(crearEstilo(fontCalibri10, colorGris4B7279,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellM11 = row11.createCell(12);
        cellM11.setCellValue("Payment Terms");
        cellM11.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellO11 = row11.createCell(14);
        cellO11.setCellValue("Credit");
        cellO11.setCellStyle(crearEstiloConFondo(fontCalibri11Bold, colorAzul1F497D, colorFondo,
                                                 HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellP11 = row11.createCell(15);
        cellP11.setCellValue("Days");
        cellP11.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                        HorizontalAlignment.RIGHT, VerticalAlignment.CENTER));
        
        Cell cellS11 = row11.createCell(18);
        cellS11.setCellValue(9);
        cellS11.setCellStyle(crearEstiloConFondo(fontCalibri11Bold, colorAzul1F497D, colorFondo,
                                                 HorizontalAlignment.RIGHT, VerticalAlignment.CENTER));
        
        aplicarBordeBottom(row11, 0, 21, borderMedium);
    }
    
    private void crearFila12VendorDetails() {
        Row row = getOrCreateRow(11);
        sheet.addMergedRegion(new CellRangeAddress(11, 11, 0, 11)); // A12:L12
        sheet.addMergedRegion(new CellRangeAddress(11, 11, 12, 21)); // M12:V12
        
        Cell cellA12 = row.createCell(0);
        cellA12.setCellValue("Vendor Details");
        cellA12.setCellStyle(crearEstilo(fontCalibri11Bold, colorAzul1F497D, 
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellM12 = row.createCell(12);
        cellM12.setCellValue("Vendor Reference ");
        cellM12.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D, 
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        // Borde thick en columnas A-L
        aplicarBordeBottom(row, 0, 11, borderThick);
    }
    
    private void crearFilas13a17VendorInfo() {
        // FILA 13
        Row row13 = getOrCreateRow(12);
        sheet.addMergedRegion(new CellRangeAddress(12, 12, 0, 2));
        sheet.addMergedRegion(new CellRangeAddress(12, 12, 3, 11));
        sheet.addMergedRegion(new CellRangeAddress(12, 12, 12, 13));
        sheet.addMergedRegion(new CellRangeAddress(12, 12, 14, 21));
        
        Cell cellA13 = row13.createCell(0);
        cellA13.setCellValue("Name");
        cellA13.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D, 
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellD13 = row13.createCell(3);
        cellD13.setCellValue("Valparaiso Ship Services S.A.");
        cellD13.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D, 
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellM13 = row13.createCell(12);
        cellM13.setCellValue("Delivery Term");
        cellM13.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D, 
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellO13 = row13.createCell(14);
        cellO13.setCellValue("Free On Board");
        cellO13.setCellStyle(crearEstiloConFondo(fontCalibri11Bold, colorAzul1F497D, colorFondo,
                                                 HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        // FILA 14: Address y Select Currency
        Row row14 = getOrCreateRow(13);
        sheet.addMergedRegion(new CellRangeAddress(13, 13, 0, 2)); // A14:C14
        sheet.addMergedRegion(new CellRangeAddress(13, 13, 3, 11)); // D14:L14
        sheet.addMergedRegion(new CellRangeAddress(13, 13, 12, 13)); // M14:N14
        
        Cell cellA14 = row14.createCell(0);
        cellA14.setCellValue("Address");
        cellA14.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellD14 = row14.createCell(3);
        cellD14.setCellValue("Calle Tercera N°820 , Placilla, Valparaiso");
        cellD14.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellM14 = row14.createCell(12);
        cellM14.setCellValue("Select Currency *");
        cellM14.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellO14 = row14.createCell(14);
        cellO14.setCellValue("USD");
        cellO14.setCellStyle(crearEstiloConFondo(fontCalibri11, colorAzul1F497D, colorFondo,
                                                 HorizontalAlignment.RIGHT, VerticalAlignment.CENTER));
        
        // FILA 15: City y Discount
        Row row15 = getOrCreateRow(14);
        sheet.addMergedRegion(new CellRangeAddress(14, 14, 0, 2)); // A15:C15
        sheet.addMergedRegion(new CellRangeAddress(14, 14, 12, 13)); // M15:N15
        
        Cell cellA15 = row15.createCell(0);
        cellA15.setCellValue("City");
        cellA15.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellM15 = row15.createCell(12);
        cellM15.setCellValue("Discount (%) for all items");
        cellM15.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellO15 = row15.createCell(14);
        cellO15.setCellValue(0);
        cellO15.setCellStyle(crearEstiloConFondo(fontCalibri11, colorAzul1F497D, colorFondo,
                                                 HorizontalAlignment.RIGHT, VerticalAlignment.CENTER));
        
        Cell cellS15 = row15.createCell(18);
        cellS15.setCellValue(0);
        cellS15.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        // FILA 16: Phone y VAT
        Row row16 = getOrCreateRow(15);
        sheet.addMergedRegion(new CellRangeAddress(15, 15, 0, 2)); // A16:C16
        sheet.addMergedRegion(new CellRangeAddress(15, 15, 3, 11)); // D16:L16
        sheet.addMergedRegion(new CellRangeAddress(15, 15, 12, 13)); // M16:N16
        
        Cell cellA16 = row16.createCell(0);
        cellA16.setCellValue("Phone");
        cellA16.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellD16 = row16.createCell(3);
        cellD16.setCellValue("56 32 2298972");
        cellD16.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellM16 = row16.createCell(12);
        cellM16.setCellValue("VAT(%) for all items");
        cellM16.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellO16 = row16.createCell(14);
        cellO16.setCellValue(0);
        cellO16.setCellStyle(crearEstiloConFondo(fontCalibri11, colorAzul1F497D, colorFondo,
                                                 HorizontalAlignment.RIGHT, VerticalAlignment.CENTER));
        
        // FILA 17: Email y Place
        Row row17 = getOrCreateRow(16);
        sheet.addMergedRegion(new CellRangeAddress(16, 16, 0, 2)); // A17:C17
        sheet.addMergedRegion(new CellRangeAddress(16, 16, 3, 11)); // D17:L17
        sheet.addMergedRegion(new CellRangeAddress(16, 16, 12, 21)); // M17:V17
        
        Cell cellA17 = row17.createCell(0);
        cellA17.setCellValue("Email");
        cellA17.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellD17 = row17.createCell(3);
        cellD17.setCellValue("vss@vsschile.cl");
        cellD17.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzulLink,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        Cell cellM17 = row17.createCell(12);
        cellM17.setCellValue("Place (CITY)");
        cellM17.setCellStyle(crearEstilo(fontCalibri10Bold, colorAzul1F497D,
                                        HorizontalAlignment.LEFT, VerticalAlignment.CENTER));
        
        // Borde medium bottom en fila 17
        aplicarBordeBottom(row17, 0, 21, borderMedium);
    }
    
    private void crearFila19Headers() {
        Row row = getOrCreateRow(18); // Fila 19
        
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(fontCalibri9Bold);
        headerStyle.setFillForegroundColor(colorHeader9DBEC3);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.LEFT);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        
        String[] headers = {"No", null, "Product Code", "Description", "Part Number", 
                           "Brand", "Weight", "Unit", "Package", "Contract Price", 
                           "Quantity", null, null, "Discount %", "VAT %", 
                           "Total Price", "MD", "sDoC", null, null, null};
        
        for (int i = 0; i < headers.length; i++) {
            if (headers[i] != null) {
                Cell cell = row.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
        }
        
        // Merged cells para headers
        sheet.addMergedRegion(new CellRangeAddress(18, 18, 11, 12)); // L19:M19 - Unit Price
        sheet.addMergedRegion(new CellRangeAddress(18, 18, 18, 19)); // S19:T19 - Vendor Remarks
        sheet.addMergedRegion(new CellRangeAddress(18, 18, 20, 21)); // U19:V19 - Office Remarks
        
        Cell cellL19 = row.getCell(11);
        if (cellL19 == null) cellL19 = row.createCell(11);
        cellL19.setCellValue("Unit Price ( USD )");
        cellL19.setCellStyle(headerStyle);
        
        Cell cellS19 = row.getCell(18);
        if (cellS19 == null) cellS19 = row.createCell(18);
        cellS19.setCellValue("Vendor Remarks");
        cellS19.setCellStyle(headerStyle);
        
        Cell cellU19 = row.getCell(20);
        if (cellU19 == null) cellU19 = row.createCell(20);
        cellU19.setCellValue("Office Remarks");
        cellU19.setCellStyle(headerStyle);
    }
    
    private void crearFila20Provisions() {
        Row row = getOrCreateRow(19); // Fila 20
        
        Cell cell = row.createCell(2); // C20
        cell.setCellValue("PROVISIONS");
        
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(fontCalibri8);
        style.setFillForegroundColor(colorAmarillo);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        cell.setCellStyle(style);
    }
    
    private void crearFila21DatosEjemplo() {
        Row row = getOrCreateRow(20); // Fila 21
        
        XSSFCellStyle dataStyle = crearEstilo(fontCalibri8, colorAzul1F497D, 
                                             HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        
        // A21
        Cell cellA = row.createCell(0);
        cellA.setCellValue(1);
        cellA.setCellStyle(dataStyle);
        
        // B21
        Cell cellB = row.createCell(1);
        cellB.setCellValue(5210702);
        cellB.setCellStyle(dataStyle);
        
        // C21
        Cell cellC = row.createCell(2);
        cellC.setCellValue("BAK38");
        cellC.setCellStyle(dataStyle);
        
        // D21
        Cell cellD = row.createCell(3);
        cellD.setCellValue("PIZZA BASE");
        cellD.setCellStyle(dataStyle);
        
        // E21
        Cell cellE = row.createCell(4);
        cellE.setCellValue("BAK38");
        cellE.setCellStyle(dataStyle);
        
        // F21
        Cell cellF = row.createCell(5);
        cellF.setCellValue("local");
        cellF.setCellStyle(dataStyle);
        
        // G21
        Cell cellG = row.createCell(6);
        cellG.setCellValue(1);
        XSSFCellStyle styleRight = crearEstilo(fontCalibri8, colorAzul1F497D,
                                               HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
        cellG.setCellStyle(styleRight);
        
        // H21
        Cell cellH = row.createCell(7);
        cellH.setCellValue("Nos");
        cellH.setCellStyle(dataStyle);
        
        // J21
        Cell cellJ = row.createCell(9);
        cellJ.setCellValue(0);
        cellJ.setCellStyle(styleRight);
        
        // K21
        Cell cellK = row.createCell(10);
        cellK.setCellValue(30);
        XSSFCellStyle styleRightWhite = crearEstiloConFondo(fontCalibri8, colorAzul1F497D, colorFondo,
                                                            HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
        cellK.setCellStyle(styleRightWhite);
        
        // M21
        Cell cellM = row.createCell(12);
        cellM.setCellValue(1.73);
        cellM.setCellStyle(styleRightWhite);
        
        // N21
        Cell cellN = row.createCell(13);
        cellN.setCellValue(0);
        cellN.setCellStyle(styleRightWhite);
        
        // O21
        Cell cellO = row.createCell(14);
        cellO.setCellValue(0);
        cellO.setCellStyle(styleRightWhite);
        
        // P21
        Cell cellP = row.createCell(15);
        cellP.setCellValue(51.9);
        XSSFCellStyle styleTotal = crearEstiloConFondo(fontCalibri8, colorAzul1F497D,
                                                       new XSSFColor(new byte[]{(byte)0xED, (byte)0xF3, (byte)0xF3}, null),
                                                       HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
        cellP.setCellStyle(styleTotal);
        
        // Q21
        Cell cellQ = row.createCell(16);
        cellQ.setCellValue("No");
        XSSFCellStyle styleFont11 = crearEstiloConFondo(fontCalibri11, colorAzul1F497D, colorFondo,
                                                        HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
        cellQ.setCellStyle(styleFont11);
        
        // R21
        Cell cellR = row.createCell(17);
        cellR.setCellValue("No");
        cellR.setCellStyle(styleFont11);
        
        // S21:T21 (merged)
        sheet.addMergedRegion(new CellRangeAddress(20, 20, 18, 19));
        Cell cellS = row.createCell(18);
        cellS.setCellValue("price per unit, ready");
        XSSFCellStyle styleRemarks = crearEstiloConFondo(fontCalibri11, colorAzul1F497D, colorFondo,
                                                         HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        cellS.setCellStyle(styleRemarks);
    }
    
    // Métodos auxiliares
    
    private Row getOrCreateRow(int rowIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }
        return row;
    }
    
    private XSSFCellStyle crearEstilo(XSSFFont font, XSSFColor color, 
                                      HorizontalAlignment hAlign, VerticalAlignment vAlign) {
        XSSFCellStyle style = workbook.createCellStyle();
        
        XSSFFont styledFont = workbook.createFont();
        styledFont.setFontName(font.getFontName());
        styledFont.setFontHeightInPoints(font.getFontHeightInPoints());
        styledFont.setBold(font.getBold());
        styledFont.setColor(color);
        
        style.setFont(styledFont);
        style.setAlignment(hAlign);
        style.setVerticalAlignment(vAlign);
        
        return style;
    }
    
    private XSSFCellStyle crearEstiloConFondo(XSSFFont font, XSSFColor color, XSSFColor bgColor,
                                              HorizontalAlignment hAlign, VerticalAlignment vAlign) {
        XSSFCellStyle style = crearEstilo(font, color, hAlign, vAlign);
        style.setFillForegroundColor(bgColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    private void aplicarBordeBottom(Row row, int colStart, int colEnd, BorderStyle borderStyle) {
        for (int i = colStart; i <= colEnd; i++) {
            Cell cell = row.getCell(i);
            if (cell == null) {
                cell = row.createCell(i);
            }
            
            XSSFCellStyle style = (XSSFCellStyle) cell.getCellStyle();
            if (style.getIndex() == 0) {
                style = workbook.createCellStyle();
            } else {
                style = workbook.createCellStyle();
                style.cloneStyleFrom(cell.getCellStyle());
            }
            
            style.setBorderBottom(borderStyle);
            style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            
            cell.setCellStyle(style);
        }
    }
    
    public static void main(String[] args) {
        try {
            BSMExcelGenerator generator = new BSMExcelGenerator();
            String outputPath = "/Users/albertosanmartin/Documents/VSS-COTIZACIONES/cotizacion-organizer/BSM_CATERING_EJEMPLO.xlsx";
            generator.generarExcel(outputPath);
            
        } catch (IOException e) {
            System.err.println("Error al generar el archivo Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
