package com.project.queue;

public interface ProcessingQueue {
  public void enqueue(String message);
  public String dequeue();
}