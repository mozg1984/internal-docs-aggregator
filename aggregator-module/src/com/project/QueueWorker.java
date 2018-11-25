package com.project;

import com.project.queue.ProcessingQueue;
import com.project.queue.RedisQueue;
import org.json.JSONObject;

public class QueueWorker {
	public static void main(String[] args) {
		ProcessingQueue queue = new RedisQueue();

		while(true) {
			String message = queue.dequeue();

			try {
				new QueueMessageHandler().handle(
					new JSONObject(message)
				);
			} catch(Exception e) {
				System.out.println("Queue message retrieve error");
				e.printStackTrace();
			}
		}
	}
}