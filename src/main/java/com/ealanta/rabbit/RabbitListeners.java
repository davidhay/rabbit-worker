package com.ealanta.rabbit;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;

@Slf4j
public class RabbitListeners {

  private final RabbitListenerEndpointRegistry registry;

  public RabbitListeners(RabbitListenerEndpointRegistry registry) {
    this.registry = registry;
  }

  void stop() {
    // instruct the listener containers to stop listening
    Set<String> ids = registry.getListenerContainerIds();
    log.info("# listener containers [{}]", ids.size());
    int i = 0;
    boolean allStopped = true;
    for (String id : ids) {
      MessageListenerContainer container = registry.getListenerContainer(id);
      container.stop();
      boolean stopped = !container.isRunning();
      allStopped &= stopped;
      log.info("#[{}]id[{}]container[{}]stopped?[{}]", i++, id, container, !container.isRunning());
    }
    log.info("listener containers all stopped? [{}]", allStopped);
  }
}

