package com.project.sql;

import com.project.configuration.Configurator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 */
public class SQLiteConnection {
  
  private static final String database = Configurator.getString("sqlite-db");
  private static Connection connection = null;

  private SQLiteConnection () {}

  public static Connection get() throws ClassNotFoundException, SQLException {
    if (connection == null) {
      Class.forName("org.sqlite.JDBC");
      connection = DriverManager.getConnection(database);
    }

    if (connection.isClosed()) {
      connection = DriverManager.getConnection(database);
    }

    return connection;
  }

  public static void close() throws SQLException {
    if (connection != null) {
      connection.close();
      connection = null;
    }
  }
}