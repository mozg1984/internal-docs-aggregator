package com.project.queue;

import java.util.List;
import redis.clients.jedis.Jedis;
import com.project.configuration.Configurator;

public class RedisQueue implements ProcessingQueue {
  private static String queueName; 
  private static int timeout;
  private Jedis jedis;
  
  public RedisQueue() {
    queueName = Configurator.getString("processing-queue.name");
    timeout = Configurator.getInt("processing-queue.timeout");
    jedis = new Jedis(Configurator.getString("processing-queue.address"));
  }
  
  public void enqueue(String message) {
    jedis.rpush(queueName, message);
  }

  public String dequeue() {
    List<String> message = jedis.blpop(timeout, queueName);
    return message.get(1);
  }
}