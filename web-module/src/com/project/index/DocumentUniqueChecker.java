package com.project.index;

import com.project.DocumentCollection;
import com.project.DocumentSearcher;

public class DocumentUniqueChecker {
  private DocumentSearcher documentSearcher;
  

  public DocumentUniqueChecker(DocumentSearcher documentSearcher) {
    this.documentSearcher = documentSearcher;
  }

  public boolean checkByHash(String hash) {
    DocumentCollection collection = documentSearcher.searchByQuery("hash:" + hash);
    return collection.isEmpty();
  }
}