package com.project.index;
 
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

import com.project.configuration.Configurator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
 
public class DocumentIndexer {
    private String indexPath;

    public DocumentIndexer() {
        indexPath = Configurator.get("storage.buffer.indexes");
    }

    public void index(String documentContent) {
        Document document = new Document();
        document.add(new TextField("contents", documentContent, Store.NO));
        addToIndex(document);
    }

    public void index(String documentContent, HashMap<String, String> metadata) {
        Document document = new Document();

        document.add(new TextField("contents", documentContent, Store.NO));

        String id = metadata.get("id");
        if (id != null) { document.add(new StringField("id", id, Field.Store.YES)); }
        
        String name = metadata.get("name");
        if (name != null) { document.add(new StringField("name", name, Field.Store.YES)); }
        
        String path = metadata.get("path");
        if (path != null) { document.add(new StringField("path", path, Field.Store.YES)); }

        String service = metadata.get("service");
        if (service != null) { document.add(new StringField("service", service, Field.Store.YES)); }    
        
        for (String key : metadata.keySet()) {
            if (key.startsWith("attribute:")) {
                String[] attribute = key.split(":");
                if (attribute.length == 2 && attribute[1] != null && attribute[1].length() > 0) {
                    document.add(new StringField(attribute[1], metadata.get(key), Field.Store.YES));
                }
            }
        }

        addToIndex(document);
    }

    private void addToIndex(Document document) {
        try {
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
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