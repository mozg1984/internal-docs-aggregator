package com.project.utility;

import com.project.index.DocumentCollection;
import com.project.index.DocumentSearcher;

public class DocumentIdentifier {
  private DocumentSearcher documentSearcher;
  private FileIdGenerator fileIdGenerator;
  private boolean uniqueness;

  public DocumentIdentifier(DocumentSearcher documentSearcher, FileIdGenerator fileIdGenerator) {
    this.documentSearcher = documentSearcher;
    this.fileIdGenerator = fileIdGenerator;
    uniqueness = true;
  }

  public String get(String hash) {
    String identifier = "-1";
    
    try {
      DocumentCollection collection = documentSearcher.searchByQuery("hash:" + hash);

      if (collection.isEmpty()) {
        identifier = fileIdGenerator.generate();
      } else {
        uniqueness = false;
        identifier = collection.get(0).getDocument().get("id");
      }
    } catch(Exception e) {
      System.out.println("Document identifier error");
      e.printStackTrace();
    }
    
    return identifier;
  }

  public boolean isUnique() {
    return uniqueness;
  }
}