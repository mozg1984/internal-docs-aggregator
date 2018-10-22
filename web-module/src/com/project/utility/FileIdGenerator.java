package com.project.utility;

import com.project.sql.SQLiteConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 
 * Todo: add logging
 */
public class FileIdGenerator {
  
  public FileIdGenerator() {}

  public String generateFor(String serviceId) {
    Connection connection = null;
    String fileId = "-1";

    try {
      connection = SQLiteConnection.get();
      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30);  // set timeout to 30 sec.
      
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