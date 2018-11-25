package com.project;

import org.apache.lucene.document.Document;

public class DocumentItem {
  private Document document;
  private String score;

  public DocumentItem(Document document, String score) {
    this.document = document;
    this.score = score;
  }

  public Document getDocument() {
    return document;
  }

  public void setDocument(Document document) {
    this.document = document;
  }

  public String getScore() {
    return score;
  }

  public void setScore(String score) {
    this.score = score;
  }
}