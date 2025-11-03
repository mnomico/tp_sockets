package com.unluki;

import java.sql.*;

public class FireBird {
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
    public static String ejecutarConsulta(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return errorXml("SQL invalido o vacío");
        }

        // Cargar driver Jaybird
        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
        } catch (ClassNotFoundException e) {
            return errorXml("Driver Jaybird no encontrado: " + e.getMessage());
        }

        String url = String.format("jdbc:firebirdsql://%s:%d/%s",
                IP_FIREBIRD, PUERTO_FIREBIRD, RUTA_BASE_DATOS);

        String sqlTrim = sql.trim();
        String sqlStart = sqlTrim.length() >= 6 ? sqlTrim.substring(0, 6).toUpperCase() : sqlTrim.toUpperCase();

        try (Connection conn = DriverManager.getConnection(url, USUARIO_FB, PASSWORD_FB)) {

            if (sqlStart.startsWith("SELECT") || sqlTrim.toUpperCase().startsWith("WITH")) {
                // SELECT
                try (PreparedStatement pstmt = conn.prepareStatement(sqlTrim);
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
                try (PreparedStatement pstmt = conn.prepareStatement(sqlTrim)) {
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

