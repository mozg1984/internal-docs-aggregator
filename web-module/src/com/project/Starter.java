package com.project;

import com.project.rest.RestServer;

public class Starter {
  public static void main(String args[]) {
    try {
      new RestServer().start();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}