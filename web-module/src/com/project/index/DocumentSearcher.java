package com.project.index;

import java.io.IOException;
import java.nio.file.Paths;
import com.project.configuration.Configurator; 
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class DocumentSearcher {  
  private String bufferIndexPath;
  private String storageIndexPath;
  private int maxCountResult;
  private String defaultField;

  public DocumentSearcher(String service) {
    bufferIndexPath = Configurator.getString("storage.buffer.indexes") + "/" + service;
    storageIndexPath = Configurator.getString("storage.indexes") + "/" + service;
    maxCountResult = Configurator.getInt("index-search.query.max-count-result");
    defaultField = Configurator.getString("index-search.query.default-field");
  }

  private IndexSearcher createSearcherInBuffer() throws IOException {
    Directory directory = FSDirectory.open(Paths.get(bufferIndexPath));
    IndexReader reader = DirectoryReader.open(directory);     
    return new IndexSearcher(reader);
  }

  private IndexSearcher createSearcherInStorage() throws IOException {
    Directory directory = FSDirectory.open(Paths.get(storageIndexPath));
    IndexReader reader = DirectoryReader.open(directory);     
    return new IndexSearcher(reader);
  }

  public DocumentCollection searchByQuery(String queryString) {
    DocumentCollection collection = new DocumentCollection();

    try {
      QueryParser qp = new QueryParser(defaultField, new StandardAnalyzer());
      Query query = qp.parse(queryString);

      IndexSearcher searcher = null;   
      TopDocs foundDocs = null;

      // Tries to search in buffer index
      try {
        searcher = createSearcherInBuffer();    
        foundDocs = searcher.search(query, maxCountResult);
      } catch(IndexNotFoundException e) {
        System.out.println("Buffer index: IndexNotFoundException");
      }

      // If not found tries to search in storage index
      if (searcher == null || foundDocs == null || foundDocs.totalHits == 0) {
        try {
          searcher = createSearcherInStorage();
          foundDocs = searcher.search(query, maxCountResult);
        } catch(IndexNotFoundException e) {
          System.out.println("Storage index: IndexNotFoundException");
          return collection; // returns empty list
        }
      }

      for (ScoreDoc scoreDoc : foundDocs.scoreDocs) {
        Document document = searcher.doc(scoreDoc.doc);
        collection.add(document, String.valueOf(scoreDoc.score));
      }
    } catch(Exception e) {
      e.printStackTrace();
    }

    return collection;
  }
}