package com.project.index;
 
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
 
public class DocumentIndexer {
    private String indexPath;

    public DocumentIndexer(String indexPath) {
        this.indexPath = indexPath;
    }

    public void index(String documentContent) {
        Document document = new Document();
        document.add(new TextField("contents", documentContent, Store.NO));
        addToIndex(document);
    }

    public void index(String documentContent, HashMap<String, String> metadata) {
        Document document = new Document();

        document.add(new TextField("content", documentContent, Store.NO));

        String id = metadata.get("id");
        if (id != null) { document.add(new StringField("id", id, Field.Store.YES)); }
        
        String name = metadata.get("name");
        if (name != null) { document.add(new TextField("name", name, Field.Store.YES)); }

        String hash = metadata.get("hash");
        if (hash != null) { document.add(new StringField("hash", hash, Field.Store.YES)); }

        String service = metadata.get("service");
        if (service != null) { document.add(new StringField("service", service, Field.Store.YES)); }
        
        String category = metadata.get("category");
        if (category != null) { document.add(new TextField("category", category, Field.Store.YES)); }
        
        String catalog = metadata.get("catalog");
        if (catalog != null) { document.add(new TextField("catalog", catalog, Field.Store.YES)); }

        addToIndex(document);
    }

    public void deleteFromIndex(Term term) {
        try {
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig conf = new IndexWriterConfig(analyzer);
            conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(directory, conf);
            
            writer.deleteDocuments(term);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToIndex(Document document) {
        try {
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            //Analyzer analyzer = new RussianAnalyzer();
            IndexWriterConfig conf = new IndexWriterConfig(analyzer);
            conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(directory, conf);
            
            writer.addDocument(document);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}