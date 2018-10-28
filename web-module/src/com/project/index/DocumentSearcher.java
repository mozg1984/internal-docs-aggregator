package com.project.index;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.file.Paths;
import com.project.configuration.Configurator;
import org.json.JSONObject;
 
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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

  public DocumentSearcher() {
    bufferIndexPath = Configurator.getString("storage.buffer.indexes");
    storageIndexPath = Configurator.getString("storage.indexes");
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

  public List<String> searchBy(String queryString) {    
    List<String> documents = new ArrayList<>();
    
    try {
      QueryParser qp = new QueryParser(defaultField, new StandardAnalyzer());
      Query query = qp.parse(queryString);

      IndexSearcher searcher = createSearcherInBuffer();    
      TopDocs foundDocs = searcher.search(query, maxCountResult);
      
      if (foundDocs.totalHits == 0) {
        searcher = createSearcherInStorage();
        foundDocs = searcher.search(query, maxCountResult);
      }
    
      for (ScoreDoc scoreDoc : foundDocs.scoreDocs) {
          Document document = searcher.doc(scoreDoc.doc);

          documents.add(
            new JSONObject()
              .put("id", document.get("id"))
              .put("name", document.get("name"))
              .put("service", document.get("service"))
              .put("category", document.get("category"))
              .put("catalog", document.get("catalog"))
              .put("score", scoreDoc.score)
              .toString()
          );
      }
    } catch(Exception e) {
      e.printStackTrace();
    }

    return documents;
  }
}