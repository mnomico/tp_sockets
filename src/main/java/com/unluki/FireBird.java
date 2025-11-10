package com.unluki;

import java.sql.*;

public class FireBird implements BD {
    private static final String IP_FIREBIRD = "localhost"; // aca hice la prueba con la bd en el mismo servidor conmutador
    private static final int PUERTO_FIREBIRD = 3050;
    private static final String RUTA_BASE_DATOS = "/var/lib/firebird/3.0/data/biblioteca.fdb";
    private static final String USUARIO_FB = "SYSDBA";
    private static final String PASSWORD_FB = "masterkey";

    /**
       Ejecuto el sql, Si es un SELECT devuelve filas en XML
       si es DML devuelve <affected>.
       En caso de error devuelve <query><error>algo</error></query>
     **/
    public String comprobarConexion(String sql) {
        // Cargar driver Jaybird
        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
        } catch (ClassNotFoundException e) {
            return "Driver Jaybird no encontrado: " + e.getMessage();
        }
        return "Estableciendo conexión...";
    }

    public ResultSet ejecutarSELECT(String sql) throws SQLException {
        String url = String.format("jdbc:firebirdsql://%s:%d/%s",
                IP_FIREBIRD, PUERTO_FIREBIRD, RUTA_BASE_DATOS);
        try (Connection conn = DriverManager.getConnection(url, USUARIO_FB, PASSWORD_FB)) {
                PreparedStatement pstmt = conn.prepareStatement(sql);
                return pstmt.executeQuery();
        }
    }

    public String ejecutarDML(String sql) {
        String url = String.format("jdbc:firebirdsql://%s:%d/%s",
                IP_FIREBIRD, PUERTO_FIREBIRD, RUTA_BASE_DATOS);
        try (Connection conn = DriverManager.getConnection(url, USUARIO_FB, PASSWORD_FB)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int affected = pstmt.executeUpdate();
                return "<query>\n  <affected>" + affected + "</affected>\n</query>";
            }
        } catch (SQLException e) {
            return "Error SQL: " + e.getMessage();
        } catch (Exception e) {
            return "Error inesperado: " + e.getMessage();
        }
    }
}