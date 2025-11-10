package com.unluki;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface BD {
    String comprobarConexion(String sql);
    String ejecutarDML(String sql) throws SQLException;
    ResultSet ejecutarSELECT(String sql) throws SQLException;
}
