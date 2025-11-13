package com.unluki;

import java.sql.*;

public class PostgreSQL {

    private static final String host = "localhost";
    private static final String port = "5432";
    private static final String database = "supermercado";
    private static final String user = "sysdba";
    private static final String password = "masterkey";
    private static final String URL = "jdbc:postgresql://" + host + ":" + port + "/" + database;

    public static String ejecutarConsulta(String sql) {
        try (Connection conn = DriverManager.getConnection(URL, user, password)) {
            Statement statement = conn.createStatement();
            int rowsAffected = statement.executeUpdate(sql);
            return "Filas afectadas: " + rowsAffected;
        } catch (SQLException ex) {
            return "Error SQL: " + ex.getMessage();
        }
    }

    public String ejecutarSELECT(String sql) throws SQLException {
        sql = sql.trim();
        try (Connection conn = DriverManager.getConnection(URL, user, password)) {

            if (sql.startsWith("SELECT") || sql.toUpperCase().startsWith("WITH")) {
                // SELECT
                try (PreparedStatement pstmt = conn.prepareStatement(sql);
                     ResultSet rs = pstmt.executeQuery()) {

                    ResultSetMetaData meta = rs.getMetaData();
                    int cols = meta.getColumnCount();

                    StringBuilder xml = new StringBuilder();
                    xml.append("<query>\n");
                    // columnas
                    xml.append("  <cols>\n");
                    for (int i = 1; i <= cols; i++) {
                        String colName = meta.getColumnLabel(i);
                        if (colName == null || colName.isEmpty()) colName = meta.getColumnName(i);
                        xml.append("    <colname").append(i).append(">")
                                .append(escapeXml(colName))
                                .append("</colname").append(i).append(">\n");
                    }
                    xml.append("  </cols>\n");

                    // filas
                    xml.append("  <rows>\n");
                    int rowNum = 1;
                    while (rs.next()) {
                        xml.append("    <row").append(rowNum).append(">\n");
                        for (int c = 1; c <= cols; c++) {
                            Object v = rs.getObject(c);
                            String sval = (v == null) ? "" : v.toString();
                            xml.append("      <col").append(c).append(">")
                                    .append(escapeXml(sval))
                                    .append("</col").append(c).append(">\n");
                        }
                        xml.append("    </row").append(rowNum).append(">\n");
                        rowNum++;
                    }
                    xml.append("  </rows>\n");
                    xml.append("</query>");
                    return xml.toString();
                }
            } else {
                // DML
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    int affected = pstmt.executeUpdate();
                    return "<query>\n  <affected>" + affected + "</affected>\n</query>";
                }
            }

        } catch (SQLException e) {
            return errorXml("Error SQL: " + e.getMessage());
        } catch (Exception e) {
            return errorXml("Error inesperado: " + e.getMessage());
        }
    }

    private static String errorXml(String msg) {
        return "<query>\n  <error>" + escapeXml(msg) + "</error>\n</query>";
    }

    // reemplazo los caracteres especiales que rompen XML
    private static String escapeXml(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '&': out.append("&amp;"); break;
                case '<': out.append("&lt;"); break;
                case '>': out.append("&gt;"); break;
                case '\"': out.append("&quot;"); break;
                case '\'': out.append("&apos;"); break;
                default: out.append(ch);
            }
        }
        return out.toString();
    }

}
