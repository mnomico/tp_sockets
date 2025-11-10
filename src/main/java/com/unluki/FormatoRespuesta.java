package com.unluki;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface FormatoRespuesta {
    String format(ResultSet resultSet) throws SQLException;
}
