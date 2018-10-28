package com.project;

import com.project.queue.ProcessingQueue;
import com.project.queue.RedisQueue;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.File;

import com.project.configuration.Configurator;
import org.apache.log4j.PropertyConfigurator;

import com.project.parser.DocumentParser;
import com.project.index.DocumentIndexer;

import java.util.Set;

public class QueueWorker {
	public static void main(String[] args) {
		PropertyConfigurator.configure(Configurator.getString("log4j.properties"));
		String bufferDirectories = Configurator.getString("storage.buffer.files");

		ProcessingQueue queue = new RedisQueue();
		DocumentIndexer indexer = new DocumentIndexer();

		while(true) {
			String message = queue.dequeue();

			try {
				JSONObject json = new JSONObject(message);

				Long docId = json.getLong("id");
				String docName = json.getString("name");
				String service = json.getString("service");
				String docPath = bufferDirectories + "/" + service + "/" + docId;
				String category = json.getString("category");
				String catalog = json.getString("catalog");

				File docFile = new File(docPath);
				
				if (docFile.exists()) {
					DocumentParser parser = new DocumentParser(docPath);

					parser.addToMetada("id", String.valueOf(docId));
					parser.addToMetada("name", docName);
					parser.addToMetada("path", docPath);
					parser.addToMetada("service", service);
					parser.addToMetada("category", category);
					parser.addToMetada("catalog", catalog);

					try {
						parser.parse();
						indexer.index(parser.getContent(), parser.getMetadata());
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
	}
}