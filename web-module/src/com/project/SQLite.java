package com.project;

import com.project.configuration.Configurator;
import com.project.utility.FileIdGenerator;
import java.util.ArrayList;

public class SQLite {
  public static void main( String args[] ) {
    // System.out.println(new FileIdGenerator().generateFor("control"));
    // System.out.println(new FileIdGenerator().generateFor("control"));
    // System.out.println(new FileIdGenerator().generateFor("control"));

    ArrayList<String> attributes = Configurator.<String>getArrayList("document-attributes.default");
    for (String attribute : attributes) {
      System.out.println(attribute);  
    }
  }
}