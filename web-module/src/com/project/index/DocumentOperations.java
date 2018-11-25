package com.project.index;

import com.project.utility.FileIdGenerator;
import com.project.utility.FileHasher;
import com.project.DocumentParser;
import com.project.DocumentCollection;
import com.project.DocumentSearcher;
import org.json.JSONObject;
import java.util.List;
import javax.ws.rs.core.StreamingOutput;
import java.io.OutputStream;


import com.project.configuration.Configurator;
import com.project.DocumentParser;
import com.project.utility.FileIdGenerator;
import com.project.utility.FileHasher;
import com.project.utility.DocumentIdentifier;
import com.project.queue.ProcessingQueue;
import com.project.queue.RedisQueue;

import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.FileSystems;

public class DocumentOperations {
  public static final String CREATE_ACTION = "CREATE";
  public static final String DELETE_ACTION = "DELETE";

  public DocumentOperations() {}

  public String search(String service, String query) {
    DocumentSearcher searcher = new DocumentSearcher(service);
    List<String> documents = searcher.searchByQuery(query).getInJsonString();
    return documents.toString();
  }

  public StreamingOutput read(String service, String id) {
    return new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
          try {
              // Firstable, try to find file in buffer
              java.nio.file.Path path = Paths.get(getDocPathInBuffer(service, id));

              // If document does not exists in buffer try to find it in storage
              if (!Files.exists(path)) {
                  path = Paths.get(getDocPathInStorage(service, id));
              }

              byte[] data = Files.readAllBytes(path);
              output.write(data);
              output.flush();
          } catch (Exception e) {
              throw new WebApplicationException("Document Not Found");
          }
      }
    };
  }

  public String create(FormDataMultiPart multiPart) {
    FormDataBodyPart attributesPart = multiPart.getField("attributes");
    String attributes = attributesPart.getValue();

    List<FormDataBodyPart> documentParts = multiPart.getFields("document");
    List<String> ids = new ArrayList<>();
    JSONObject documentAttributes;
    
    for (FormDataBodyPart part : documentParts) {
      FormDataContentDisposition fileDetail = part.getFormDataContentDisposition();

      documentAttributes = new JSONObject(attributes);
      String service = documentAttributes.getString("service");

      DocumentParser documentParser = new DocumentParser(part.getEntityAs(InputStream.class));
      try { documentParser.parse(); } catch(Exception e) { return ids.toString(); }

      DocumentIdentifier documentIdentifier = new DocumentIdentifier(
          new DocumentSearcher(service),
          new FileIdGenerator(service)
      );

      String documentHash = FileHasher.getMD5Hash(documentParser.getContent());
      String id = documentIdentifier.get(documentHash);
      
      if (id.equals("-1")) { // Check correctness of taken document id
        System.out.println("Not correct id");
        return ids.toString();
      }
      
      boolean isIdUnique = documentIdentifier.isUnique();
      String fileName = fileDetail.getFileName();

      documentAttributes.put("id", id);
      documentAttributes.put("hash", documentHash);
      documentAttributes.put("unique", isIdUnique);
      documentAttributes.put("name", fileName);

      if (writeToBuffer(part.getEntityAs(InputStream.class), documentAttributes)) {
        ids.add(
          new JSONObject()
            .put("name", fileName)
            .put("id", id)
            .toString()
        );
      }
    }

    return ids.toString();
  }

  public void delete() {
    // return uniqueness;
  }

  private boolean writeToBuffer(InputStream uploadedInputStream, JSONObject documentAttributes) {
    boolean result = true;
    
    String id = documentAttributes.getString("id");
    boolean isUnique = documentAttributes.getBoolean("unique");
    String service = documentAttributes.getString("service");

    try {
      ProcessingQueue queue = new RedisQueue();

      if (isUnique) {
        System.out.println("is unique");
        
        String uploadedDocumentLocation = getDocPathInBuffer(service, id);
        // Saves document into buffer
        java.nio.file.Path path = FileSystems.getDefault().getPath(uploadedDocumentLocation);
        Files.copy(uploadedInputStream, path);
      }

      // Enqueue attributes into Redis queue for processing
      documentAttributes.put("action", CREATE_ACTION);
      queue.enqueue(documentAttributes.toString());
    } catch (IOException e) {
      e.printStackTrace();
      result = false;
    }
      
    return result;
  }

  private String getDocPathInBuffer(String service, String id) {
    String bufferPath = Configurator.getString("storage.buffer.files");
    return bufferPath + "/" + service + "/" + id;
  }

  private String getDocPathInStorage(String service, String id) {
    String storagePath = Configurator.getString("storage.files");
    return storagePath + "/" + service + "/" + id;
  }
}