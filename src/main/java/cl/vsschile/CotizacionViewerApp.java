package cl.vsschile;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.*;

/**
 * Aplicación JavaFX para cargar cotizaciones y visualizarlas
 * con el formato original del broker (colores, estilos)
 */
public class CotizacionViewerApp extends Application {
    
    private ComboBox<String> brokerComboBox;
    private Button uploadButton;
    private Button viewButton;
    private Label fileLabel;
    private TableView<RowData> tableView;
    private File selectedFile;
    
    private Connection conn;
    private Map<String, BrokerFormat> brokerFormats;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Visor de Cotizaciones VSS");
        
        // Conectar a BD
        connectDatabase();
        
        // UI Principal
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        
        // Header
        Label titleLabel = new Label("VISOR DE COTIZACIONES");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        // Panel de selección de broker
        HBox brokerPanel = createBrokerPanel();
        
        // Panel de carga de archivo
        HBox filePanel = createFilePanel();
        
        // Cargar brokers (después de crear el ComboBox)
        loadBrokers();
        
        // Botón de visualizar
        viewButton = new Button("Visualizar con Formato");
        viewButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px;");
        viewButton.setDisable(true);
        viewButton.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                visualizeQuotation();
            }
        });
        
        // Tabla
        tableView = new TableView<RowData>();
        tableView.setPlaceholder(new Label("Seleccione un broker y cargue un archivo Excel"));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        
        root.getChildren().addAll(
            titleLabel,
            new Separator(),
            brokerPanel,
            filePanel,
            viewButton,
            new Separator(),
            tableView
        );
        
        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private HBox createBrokerPanel() {
        HBox panel = new HBox(10);
        panel.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label("Broker:");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        brokerComboBox = new ComboBox<String>();
        brokerComboBox.setPromptText("Seleccione un broker");
        brokerComboBox.setPrefWidth(300);
        brokerComboBox.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                updateViewButton();
            }
        });
        
        panel.getChildren().addAll(label, brokerComboBox);
        return panel;
    }
    
    private HBox createFilePanel() {
        HBox panel = new HBox(10);
        panel.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label("Archivo:");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        uploadButton = new Button("Seleccionar Excel");
        uploadButton.setStyle("-fx-font-size: 12px;");
        uploadButton.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                selectFile();
            }
        });
        
        fileLabel = new Label("Ningún archivo seleccionado");
        fileLabel.setStyle("-fx-text-fill: gray;");
        
        panel.getChildren().addAll(label, uploadButton, fileLabel);
        return panel;
    }
    
    private void connectDatabase() {
        try {
            String dbUrl = "jdbc:postgresql://localhost:5432/sistema_cotizacion_2025";
            conn = DriverManager.getConnection(dbUrl, "postgres", "");
            System.out.println("✓ Conectado a PostgreSQL");
        } catch (SQLException e) {
            showError("Error conectando a base de datos", e.getMessage());
        }
    }
    
    private void loadBrokers() {
        brokerFormats = new HashMap<String, BrokerFormat>();
        List<String> brokerNames = new ArrayList<String>();
        
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT broker_name, formato_id, header_row FROM v_formatos_activos ORDER BY broker_name"
            );
            
            while (rs.next()) {
                String name = rs.getString("broker_name");
                BrokerFormat format = new BrokerFormat();
                format.brokerName = name;
                format.formatoId = rs.getInt("formato_id");
                format.headerRow = rs.getInt("header_row");
                
                brokerFormats.put(name, format);
                brokerNames.add(name);
            }
            
            rs.close();
            stmt.close();
            
            brokerComboBox.setItems(FXCollections.observableArrayList(brokerNames));
            System.out.println("✓ Cargados " + brokerNames.size() + " brokers");
            
        } catch (SQLException e) {
            showError("Error cargando brokers", e.getMessage());
        }
    }
    
    private void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Cotización Excel");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx", "*.xls"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );
        
        selectedFile = fileChooser.showOpenDialog(tableView.getScene().getWindow());
        
        if (selectedFile != null) {
            fileLabel.setText(selectedFile.getName());
            fileLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            updateViewButton();
        }
    }
    
    private void updateViewButton() {
        viewButton.setDisable(
            brokerComboBox.getValue() == null || selectedFile == null
        );
    }
    
    private void visualizeQuotation() {
        String brokerName = brokerComboBox.getValue();
        if (brokerName == null || selectedFile == null) {
            return;
        }
        
        try {
            // Obtener formato del broker
            BrokerFormat format = brokerFormats.get(brokerName);
            loadBrokerColumns(format);
            
            // Leer archivo Excel
            Workbook workbook = loadWorkbook(selectedFile);
            Sheet sheet = workbook.getSheetAt(0);
            
            // Crear columnas en TableView
            createTableColumns(format);
            
            // Cargar datos
            ObservableList<RowData> data = loadExcelData(sheet, format);
            tableView.setItems(data);
            
            workbook.close();
            
            showInfo("Cotización cargada", 
                "Se visualizó la cotización con el formato de " + brokerName);
            
        } catch (Exception e) {
            showError("Error cargando cotización", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadBrokerColumns(BrokerFormat format) throws SQLException {
        format.columns = new ArrayList<ColumnFormat>();
        
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM v_columnas_detalladas WHERE broker_name = ? ORDER BY indice_columna"
        );
        ps.setString(1, format.brokerName);
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            ColumnFormat col = new ColumnFormat();
            col.campoEstandar = rs.getString("campo_estandar");
            col.nombreOriginal = rs.getString("nombre_columna_original");
            col.indiceColumna = rs.getInt("indice_columna");
            col.colorFondo = rs.getString("color_fondo");
            col.colorTexto = rs.getString("color_texto");
            col.esNegrita = rs.getBoolean("es_negrita");
            format.columns.add(col);
        }
        
        rs.close();
        ps.close();
    }
    
    private Workbook loadWorkbook(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook;
        
        if (file.getName().endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(fis);
        } else {
            workbook = new HSSFWorkbook(fis);
        }
        
        fis.close();
        return workbook;
    }
    
    private void createTableColumns(BrokerFormat format) {
        tableView.getColumns().clear();
        
        for (int i = 0; i < format.columns.size(); i++) {
            ColumnFormat colFormat = format.columns.get(i);
            
            TableColumn<RowData, String> column = new TableColumn<RowData, String>(
                colFormat.nombreOriginal
            );
            
            final int index = i;
            column.setCellValueFactory(new javafx.util.Callback<TableColumn.CellDataFeatures<RowData, String>, javafx.beans.value.ObservableValue<String>>() {
                public javafx.beans.value.ObservableValue<String> call(TableColumn.CellDataFeatures<RowData, String> cellData) {
                    return cellData.getValue().getProperty(index);
                }
            });
            
            // Aplicar estilo de header con colores del broker
            column.setStyle(getHeaderStyle(colFormat));
            
            // Cell factory para aplicar colores a las celdas
            column.setCellFactory(new javafx.util.Callback<TableColumn<RowData, String>, TableCell<RowData, String>>() {
                public TableCell<RowData, String> call(TableColumn<RowData, String> col) {
                    return new TableCell<RowData, String>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setStyle("");
                            } else {
                                setText(item);
                                setStyle(getCellStyle(colFormat));
                            }
                        }
                    };
                }
            });
            
            column.setPrefWidth(120);
            tableView.getColumns().add(column);
        }
    }
    
    private String getHeaderStyle(ColumnFormat format) {
        StringBuilder style = new StringBuilder();
        
        if (format.colorFondo != null && !format.colorFondo.isEmpty()) {
            style.append("-fx-background-color: ").append(format.colorFondo).append("; ");
        }
        
        if (format.colorTexto != null && !format.colorTexto.isEmpty()) {
            style.append("-fx-text-fill: ").append(format.colorTexto).append("; ");
        }
        
        if (format.esNegrita) {
            style.append("-fx-font-weight: bold; ");
        }
        
        return style.toString();
    }
    
    private String getCellStyle(ColumnFormat format) {
        StringBuilder style = new StringBuilder();
        style.append("-fx-border-color: lightgray; ");
        style.append("-fx-border-width: 0.5px; ");
        return style.toString();
    }
    
    private ObservableList<RowData> loadExcelData(Sheet sheet, BrokerFormat format) {
        ObservableList<RowData> data = FXCollections.observableArrayList();
        
        // Leer desde la fila después del header
        int startRow = format.headerRow + 1;
        int endRow = sheet.getLastRowNum();
        
        for (int i = startRow; i <= endRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            RowData rowData = new RowData(format.columns.size());
            boolean hasData = false;
            
            for (ColumnFormat colFormat : format.columns) {
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(colFormat.indiceColumna);
                String value = getCellValue(cell);
                rowData.setValue(format.columns.indexOf(colFormat), value);
                
                if (value != null && !value.trim().isEmpty()) {
                    hasData = true;
                }
            }
            
            if (hasData) {
                data.add(rowData);
            }
        }
        
        return data;
    }
    
    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.format("%d", (long) numValue);
                    } else {
                        return String.format("%.2f", numValue);
                    }
                }
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void stop() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            // Ignorar
        }
    }
    
    // Clases auxiliares
    private static class BrokerFormat {
        String brokerName;
        int formatoId;
        int headerRow;
        List<ColumnFormat> columns;
    }
    
    private static class ColumnFormat {
        String campoEstandar;
        String nombreOriginal;
        int indiceColumna;
        String colorFondo;
        String colorTexto;
        boolean esNegrita;
    }
}
