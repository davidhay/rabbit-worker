package com.ealanta.rabbit;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitConfig {

  public final static String WORKER_QUEUE_NAME = "${worker.recv.queue.name}";

  @Value(RabbitConfig.WORKER_QUEUE_NAME)
  String recvQueueName;

  @Autowired
  RabbitListenerEndpointRegistry registry;

  @Bean
  public Queue qBean() {
    Map<String, Object> args = new HashMap<>();
    args.put("x-max-priority", 2);
    return new Queue(recvQueueName, true, false, false, args);
  }

  @Bean
  RabbitListeners rabbitListeners() {
    return new RabbitListeners(registry);
  }

  @Bean
  RabbitMessageListener rabbitMessageListener() {
    return new RabbitMessageListener(messageProcessor(), shutdownHandler(), rabbitListeners());
  }

  @Bean
  ShutdownHandler shutdownHandler() {
    return new ExitingShutdownHandler();
  }

  @Bean
  MessageProcessor messageProcessor() {
    return messageInfo -> {
      log.info("message [{}] being consumed", messageInfo);
      return false;
    };
  }
}
