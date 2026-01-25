package cl.vsschile;

import java.sql.*;

public class InsertPrecioVSS {
    public static void main(String[] args) throws Exception {
        String brokerName = "GARRETS INTERNATIONAL LTD";
        int columnIndex = 15; // índice 0-based -> columna 16 (P)
        String columnLetter = "P";
        String campoEstandar = "PRECIO_VSS"; // nombre lógico para la columna personalizada
        String nombreOriginal = "PRECIO VSS"; // cómo quieres que aparezca en la grilla

        FormatoDatabaseManager db = new FormatoDatabaseManager();
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            // 1) obtener broker_id
            int brokerId = -1;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT broker_id FROM brokers WHERE broker_name = ?")) {
                ps.setString(1, brokerName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) brokerId = rs.getInt(1);
                }
            }
            if (brokerId == -1) throw new RuntimeException("Broker no encontrado: " + brokerName);

            // 2) obtener formato_id (versión 1.0)
            int formatoId = -1;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT formato_id FROM broker_formatos WHERE broker_id = ? AND version = '1.0'")) {
                ps.setInt(1, brokerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) formatoId = rs.getInt(1);
                }
            }
            if (formatoId == -1) throw new RuntimeException("Formato no encontrado para broker: " + brokerName);

            // 3) eliminar si ya existe para evitar duplicados
            try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM formato_columnas WHERE formato_id = ? AND campo_estandar = ?")) {
                ps.setInt(1, formatoId);
                ps.setString(2, campoEstandar);
                ps.executeUpdate();
            }

            // 4) insertar columna personalizada
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO formato_columnas (formato_id, campo_estandar, nombre_columna_original, indice_columna, letra_columna, color_fondo, color_texto, es_negrita, es_cursiva, tiene_borde) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, formatoId);
                ps.setString(2, campoEstandar);
                ps.setString(3, nombreOriginal);
                ps.setInt(4, columnIndex);
                ps.setString(5, columnLetter);
                ps.setString(6, null);
                ps.setString(7, null);
                ps.setBoolean(8, false);
                ps.setBoolean(9, false);
                ps.setBoolean(10, false);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("✓ Columna personalizada agregada: " + campoEstandar + " al formato " + formatoId + " (" + brokerName + ")");
        }
    }
}
