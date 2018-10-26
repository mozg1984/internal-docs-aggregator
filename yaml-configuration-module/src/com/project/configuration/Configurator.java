package com.project.configuration;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.ArrayList;

public class Configurator {  
  private static final String path = "resources/config.yml";
  private static Yaml yaml = new Yaml();  
  private static Map<String, Object> configuration = null;

  private Configurator() {}

  public static void load() {
    InputStream stream = null;
    
    try {
      stream = new FileInputStream(new File(path)); 
      configuration = yaml.load(stream);
    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (stream != null) stream.close(); 
      } catch(Exception e) {}
    }
  }

  public static Object get(String key) {
    Object value = null;
    Map<String, Object> config = null;

    if (configuration == null) {
      load();
    }   
    
    for (String part : key.split("\\.")) {
      value = (config == null ? configuration : config).get(part);

      if (value == null) {
        break;
      }

      switch (value.getClass().getName()) {
        case "java.lang.String": return value;
        case "java.lang.Integer": return value;
        case "java.util.ArrayList": return value;
        case "java.util.LinkedHashMap": config = (Map<String, Object>) value;      
      }
    }

    return value;
  }

  public static int getInt(String key) {
    return (Integer) get(key);
  }

  public static String getString(String key) {
    return (String) get(key);
  }

  public static <T> ArrayList<T> getArrayList(String key) {
    return (ArrayList<T>) get(key);
  }
}