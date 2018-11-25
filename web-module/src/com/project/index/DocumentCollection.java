package com.project.index;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.apache.lucene.document.Document;

public class DocumentCollection {
  List<DocumentItem> collection;

  public DocumentCollection() {
    collection = new ArrayList<>();
  }

  public void add(Document document, String score) {
    collection.add(new DocumentItem(document, score));
  }

  public void add(DocumentItem item) {
    collection.add(item);
  }

  public void clear() {
    collection.clear();
  }

  public List<DocumentItem> get() {
    return collection;
  }

  public DocumentItem get(int index) {
    return collection.get(index);
  }

  public boolean isEmpty() {
    return collection.isEmpty();
  }

  public List<String> getInJsonString() {
    List<String> collection = new ArrayList<>();

    for (DocumentItem documentItem : this.collection) {
      collection.add(
        new JSONObject()
          .put("id", documentItem.getDocument().get("id"))
          .put("name", documentItem.getDocument().get("name"))
          .put("service", documentItem.getDocument().get("service"))
          .put("category", documentItem.getDocument().get("category"))
          .put("catalog", documentItem.getDocument().get("catalog"))
          .put("score", documentItem.getScore())
          .toString()
      );
    }

    return collection;
  }
}
