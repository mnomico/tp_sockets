package com.unluki;

import java.sql.*;

public class PostgreSQL implements BD {
    private static final String host = "localhost";
    private static final String port = "5432";
    private static final String database = "supermercado";
    private static final String user = "sysdba";
    private static final String password = "masterkey";
    private static final String URL = "jdbc:postgresql://" + host + ":" + port + "/" + database;

    public String comprobarConexion(String sql) {
        return "Estableciendo conexión...";
    }

    public String ejecutarDML(String sql) {
        try (Connection conn = DriverManager.getConnection(URL, user, password)) {
            Statement statement = conn.createStatement();
            int rowsAffected = statement.executeUpdate(sql);
            return "Filas afectadas: " + rowsAffected;
        } catch (SQLException ex) {
            return "Error SQL: " + ex.getMessage();
        }
    }

    public ResultSet ejecutarSELECT(String sql) throws SQLException {
        sql = sql.trim();
        try (Connection conn = DriverManager.getConnection(URL, user, password)) {
            Statement statement = conn.createStatement();
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                return resultSet;
            }
        }
    }
}
