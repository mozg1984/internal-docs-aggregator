package com.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import com.project.configuration.Configurator;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.log4j.PropertyConfigurator;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

/**
 * Document parser.
 * It retrieves document content and metadata 
 * independently of document format
 */
public class DocumentParser {
	private String path = null;
	private int charsLimit = Configurator.getInt("tika.limit");
	private String content = "";
  private HashMap<String, String> metadata = new HashMap<String, String>();
	private File file = null;
	private InputStream stream = null;
	
	// Configuring loging by specific log file
	{ PropertyConfigurator.configure(Configurator.getString("log4j.properties")); }

	public DocumentParser(String path) throws IOException {
		this.path = path;		
		file = new File(path);
		stream = new FileInputStream(file);
  }
  
  public DocumentParser(File file) throws IOException {
    this.file = file;
		this.path = file.getPath();
		stream = new FileInputStream(file);
	}

	public DocumentParser(InputStream stream) {
    this.stream = stream;
	}
	
	public void parse() throws IOException, SAXException, TikaException {
		BodyContentHandler handler = new BodyContentHandler(charsLimit);
    AutoDetectParser parser = new AutoDetectParser();
    Metadata metadata = new Metadata();
		
		try {
			parser.parse(stream, handler, metadata);
			content = handler.toString().replaceAll(" \\s+", "");
			extractMetadata(metadata);
		} catch (Exception e) {
      e.printStackTrace();
    }
	}

	private void extractMetadata(Metadata metadata) {
		String[] metadataNames = metadata.names();
		for (String name : metadataNames) {
			this.metadata.put(name, metadata.get(name));
		}
	}

	public String getPath() {
		return path;
	}

	public String getContent() {
		return content;
	}

	public HashMap<String, String> getMetadata() {
		return metadata;
	}

	public void addToMetada(String key, String value) {
		this.metadata.put(key, value);
	}

	public void resetMetada() {
		this.metadata.clear();
	}

	public String getContentType() {
		return metadata.get("Content-Type");
	}
}