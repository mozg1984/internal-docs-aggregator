package com.project.utility;

import com.project.sql.SQLiteConnection;
import com.project.configuration.Configurator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class FileIdGenerator {
  
  private String serviceId;

  public FileIdGenerator(String serviceId) {
    this.serviceId = serviceId;
  }

  public String generate() {
    Connection connection = null;
    String fileId = "-1";

    try {
      connection = SQLiteConnection.get();
      Statement statement = connection.createStatement();
      statement.setQueryTimeout(Configurator.getInt("sqlite.timeout"));
      
      ResultSet rs = statement.executeQuery("select value from files_counter where service_id = '" + serviceId + "'");
      
      while(rs.next()) {
        fileId = String.valueOf(rs.getLong("value"));
        break;
      }

      statement.executeUpdate("update files_counter set value = value + 1 where service_id = '" + serviceId + "'");
    } catch(Exception e) {
      System.err.println(e.getMessage());
    } finally {
      try {
        connection.close();
      } catch(Exception e) {
        System.err.println(e);
      }
    }

    return fileId;
  }
}