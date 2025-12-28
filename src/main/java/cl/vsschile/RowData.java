package cl.vsschile;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Clase para representar una fila dinámica en el TableView
 */
public class RowData {
    
    private StringProperty[] properties;
    
    public RowData(int columnCount) {
        properties = new StringProperty[columnCount];
        for (int i = 0; i < columnCount; i++) {
            properties[i] = new SimpleStringProperty("");
        }
    }
    
    public StringProperty getProperty(int index) {
        if (index >= 0 && index < properties.length) {
            return properties[index];
        }
        return new SimpleStringProperty("");
    }
    
    public void setValue(int index, String value) {
        if (index >= 0 && index < properties.length) {
            properties[index].set(value != null ? value : "");
        }
    }
    
    public String getValue(int index) {
        if (index >= 0 && index < properties.length) {
            return properties[index].get();
        }
        return "";
    }
}
