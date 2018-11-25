package com.project;

import org.json.JSONObject;
import org.json.JSONException;
import java.io.File;
import com.project.configuration.Configurator;
import com.project.DocumentParser;
import com.project.index.DocumentIndexer;
import org.apache.lucene.document.Document;

public class QueueMessageHandler {
  public static final String CREATE_ACTION = "CREATE";
  public static final String DELETE_ACTION = "DELETE";

  public void handle(JSONObject message) {
    switch (message.getString("action")) {
      case CREATE_ACTION: create(message); break;
      case DELETE_ACTION: delete(message); break;
    }
  }

  private void create(JSONObject message) {
    String bufferDirectories = Configurator.getString("storage.buffer.files");
		String bufferIndexDirectory = Configurator.getString("storage.buffer.indexes");

    try {
      Long docId = message.getLong("id");
      String docHash = message.getString("hash");
      String docName = message.getString("name");
      String service = message.getString("service");
      String docPath = bufferDirectories + "/" + service + "/" + docId;
      String indexPath = bufferIndexDirectory + "/" + service;
      String category = message.getString("category");
      String catalog = message.getString("catalog");

      File docFile = new File(docPath);
        
      if (docFile.exists()) {
        try {
          DocumentParser parser = new DocumentParser(docPath);
          DocumentIndexer indexer = new DocumentIndexer(indexPath);

          parser.addToMetada("id", String.valueOf(docId));
          parser.addToMetada("hash", docHash);
          parser.addToMetada("name", docName);
          parser.addToMetada("path", docPath);
          parser.addToMetada("service", service);
          parser.addToMetada("category", category);
          parser.addToMetada("catalog", catalog);

          parser.parse();
          indexer.index(parser.getContent(), parser.getMetadata());

          System.out.println("QueueMessageHandler: content");
          System.out.println(parser.getContent());
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    } catch(JSONException e) {
      e.printStackTrace();
    }
  }

  private void delete(JSONObject message) {}
}