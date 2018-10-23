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
		PropertyConfigurator.configure(Configurator.get("log4j.properties"));
		String bufferDirectories = Configurator.get("storage.buffer.files");

		ProcessingQueue queue = new RedisQueue();
		DocumentIndexer indexer = new DocumentIndexer();

		while(true) {
			System.out.println("Waiting for a message in the queue");
			
			String message = queue.dequeue();
			
			System.out.println("Message received:" + message);

			try {
				JSONObject json = new JSONObject(message);

				Long docId = json.getLong("id");
				String docName = json.getString("name");
				String service = json.getString("service");
				String docPath = bufferDirectories + "/" + service + "/" + docId;
				JSONObject docAttributes = json.getJSONObject("attributes");

				File docFile = new File(docPath);
				
				if (docFile.exists()) {
					DocumentParser parser = new DocumentParser(docPath);

					parser.addToMetada("id", String.valueOf(docId));
					parser.addToMetada("name", docName);
					parser.addToMetada("path", docPath);
					parser.addToMetada("service", service);

					Set<String> attributes = docAttributes.keySet();
					attributes.forEach((key) -> { parser.addToMetada("attribute:" + key, String.valueOf(docAttributes.get(key))); });

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