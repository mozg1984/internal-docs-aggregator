package com.project;

import com.project.utility.FileIdGenerator;;

public class SQLite {
  public static void main( String args[] ) {
    System.out.println(new FileIdGenerator().generateFor("control"));
    System.out.println(new FileIdGenerator().generateFor("control"));
    System.out.println(new FileIdGenerator().generateFor("control"));
  }
}