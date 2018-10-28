package com.project.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import com.project.configuration.Configurator;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;

import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class DocumentParser {
	private String path;
	private int charsLimit;
	private String content;
  private HashMap<String, String> metadata;
  private File file;
	
	public DocumentParser(String path) {
		this.path = path;
		charsLimit = Configurator.getInt("tika.limit");
    file = new File(path);
		this.metadata = new HashMap<String, String>();
  }
  
  public DocumentParser(File file) {
    this.file = file;
    this.path = file.getPath();
		this.metadata = new HashMap<String, String>();
	}
	
	public void parse() throws IOException, SAXException, TikaException {
		BodyContentHandler handler = new BodyContentHandler(charsLimit);
    AutoDetectParser parser = new AutoDetectParser();
    Metadata metadata = new Metadata();
		
		try {
			InputStream stream = new FileInputStream(file);
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