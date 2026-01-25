package cl.vsschile;

import java.sql.*;

public class PrintFormatoColumns {
    public static void main(String[] args) throws Exception {
        String brokerName = args.length > 0 ? args[0] : "GARRETS INTERNATIONAL LTD";
        FormatoDatabaseManager db = new FormatoDatabaseManager();
        try (Connection conn = db.getConnection()) {
            int brokerId = -1;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT broker_id FROM brokers WHERE broker_name=?")) {
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
            if (formatoId == -1) throw new RuntimeException("Formato no encontrado para: " + brokerName);
            
            System.out.println("Broker: " + brokerName + " formato_id=" + formatoId);
            System.out.println("Columnas (ordenadas por indice_columna):\n");
            
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT campo_estandar, nombre_columna_original, indice_columna, letra_columna FROM formato_columnas WHERE formato_id=? ORDER BY indice_columna")) {
                ps.setInt(1, formatoId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String campo = rs.getString(1);
                        String nombre = rs.getString(2);
                        int idx = rs.getInt(3);
                        String letra = rs.getString(4);
                        System.out.printf("  %-15s -> %s (%d) \"%s\"%n", campo, letra, idx+1, nombre);
                    }
                }
            }
        }
    }
}
