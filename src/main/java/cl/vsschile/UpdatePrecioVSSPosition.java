package cl.vsschile;

import java.sql.*;

public class UpdatePrecioVSSPosition {
    public static void main(String[] args) throws Exception {
        String brokerName = "GARRETS INTERNATIONAL LTD";
        String campo = "PRECIO_VSS";
        
        FormatoDatabaseManager db = new FormatoDatabaseManager();
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            
            int brokerId = -1;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT broker_id FROM brokers WHERE broker_name = ?")) {
                ps.setString(1, brokerName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) brokerId = rs.getInt(1);
                }
            }
            if (brokerId == -1) throw new RuntimeException("Broker no encontrado: " + brokerName);
            
            int formatoId = -1;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT formato_id FROM broker_formatos WHERE broker_id = ? AND version='1.0'")) {
                ps.setInt(1, brokerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) formatoId = rs.getInt(1);
                }
            }
            if (formatoId == -1) throw new RuntimeException("Formato no encontrado para broker: " + brokerName);
            
            // Obtener max indice_columna actual
            int maxIdx = -1;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT MAX(indice_columna) FROM formato_columnas WHERE formato_id = ?")) {
                ps.setInt(1, formatoId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) maxIdx = rs.getInt(1);
                }
            }
            if (maxIdx < 0) throw new RuntimeException("No hay columnas registradas en formato_id=" + formatoId);
            
            int newIndex = maxIdx + 1; // último
            String newLetter = getColumnLetter(newIndex);
            
            // Asegurar existencia del registro
            boolean exists;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM formato_columnas WHERE formato_id = ? AND campo_estandar = ?")) {
                ps.setInt(1, formatoId);
                ps.setString(2, campo);
                try (ResultSet rs = ps.executeQuery()) { exists = rs.next(); }
            }
            if (!exists) {
                // Insert simple con nombre visible
                try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO formato_columnas (formato_id, campo_estandar, nombre_columna_original, indice_columna, letra_columna) VALUES (?,?,?,?,?)")) {
                    ps.setInt(1, formatoId);
                    ps.setString(2, campo);
                    ps.setString(3, "PRECIO VSS");
                    ps.setInt(4, newIndex);
                    ps.setString(5, newLetter);
                    ps.executeUpdate();
                }
            } else {
                // Update posición al final
                try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE formato_columnas SET indice_columna = ?, letra_columna = ? WHERE formato_id = ? AND campo_estandar = ?")) {
                    ps.setInt(1, newIndex);
                    ps.setString(2, newLetter);
                    ps.setInt(3, formatoId);
                    ps.setString(4, campo);
                    ps.executeUpdate();
                }
            }
            
            conn.commit();
            System.out.println("✓ PRECIO_VSS reubicado como última columna (indice=" + newIndex + ", letra=" + newLetter + ") en formato_id=" + formatoId);
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
