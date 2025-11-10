package com.unluki;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class XML implements FormatoRespuesta {

    @Override
    public String format(ResultSet resultSet) throws SQLException {
        try {
            ResultSetMetaData metadata = resultSet.getMetaData();

            // Columnas
            int columns = metadata.getColumnCount();
            StringBuilder output = new StringBuilder();
            output.append("<query>\n");
            output.append("  <cols>\n");
            for (int i = 1; i <= columns; i++) {
                String colName = metadata.getColumnLabel(i);
                if (colName == null || colName.isEmpty()) colName = metadata.getColumnName(i);
                output.append("    <colname").append(i).append(">")
                        .append(escapeXml(colName))
                        .append("</colname").append(i).append(">\n");
            }
            output.append("  </cols>\n");

            // Filas
            output.append("  <rows>\n");
            int rows = 1;
            while (resultSet.next()) {
                output.append("    <row").append(rows).append(">\n");
                for (int c = 1; c <= columns; c++) {
                    Object v = resultSet.getObject(c);
                    String sval = (v == null) ? "" : v.toString();
                    output.append("      <col").append(c).append(">")
                            .append(escapeXml(sval))
                            .append("</col").append(c).append(">\n");
                }
                output.append("    </row").append(rows).append(">\n");
                rows++;
            }
            output.append("  </rows>\n");
            output.append("</query>");
            return output.toString();
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    private static String errorXml(String msg) {
        return "<query>\n  <error>" + escapeXml(msg) + "</error>\n</query>";
    }

    // Reemplazo los caracteres especiales que rompen XML
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
