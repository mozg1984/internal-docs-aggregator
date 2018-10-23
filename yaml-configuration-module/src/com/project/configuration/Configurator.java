package com.project.configuration;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

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

  public static String get(String key) {
    if (configuration == null) {
      load();
    }
    
    Map<String, Object> config = null;
    
    for (String part : key.split("\\.")) {
      Object value = (config == null ? configuration : config).get(part);

      if (value == null) {
        break;
      }

      switch (value.getClass().getName()) {
        case "java.lang.String": return (String) value;
        case "java.lang.Integer": return String.valueOf(value);
        case "java.util.LinkedHashMap": config = (Map<String, Object>) value;      
      }
    }

    return null;
  }

  public static int getInt(String key) {
    return Integer.parseInt(get(key));
  }
}