package com.company.Server.DatabaseAccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

abstract class DBAccess {
    Connection connection;

    public DBAccess() throws SQLException {
        this.connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/mtcg",
                "basem",
                "");
    }
}
