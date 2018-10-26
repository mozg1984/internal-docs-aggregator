package com.project;

import com.project.configuration.Configurator;
import com.project.utility.FileIdGenerator;
import java.util.ArrayList;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.project.rest.RestServer;;

public class SQLite {
  public static void main( String args[] ) {
    // System.out.println(new FileIdGenerator().generateFor("control"));
    // System.out.println(new FileIdGenerator().generateFor("control"));
    // System.out.println(new FileIdGenerator().generateFor("control"));

    // ArrayList<String> attributes = Configurator.<String>getArrayList("document-attributes.default");
    // for (String attribute : attributes) {
    //   System.out.println(attribute);  
    // }

    try {
      new RestServer().start();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}