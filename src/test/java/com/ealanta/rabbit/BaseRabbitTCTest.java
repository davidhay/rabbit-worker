package com.ealanta.rabbit;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@Slf4j
@DirtiesContext
public abstract class BaseRabbitTCTest {

  @Value("${spring.rabbitmq.username}")
  private String rabbitUsername;

  @Value("${spring.rabbitmq.password}")
  private String rabbitPassword;

  @Value("${spring.rabbitmq.host}")
  private String rabbitHost;

  @Value("${spring.rabbitmq.port}")
  private int rabbitPort;

  @Container
  private static final RabbitMQContainer RABBIT = new RabbitMQContainer(
      "rabbitmq:3.10.0-management-alpine").withExposedPorts(5672,15672).withReuse(false);

  @DynamicPropertySource
  static void setupProperties(DynamicPropertyRegistry registry) {
    log.info("RABBIT HTTP URL [ {} ]",RABBIT.getHttpUrl());
    registry.add("spring.rabbitmq.host", RABBIT::getHost);
    registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
    registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
    registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
  }

  @PostConstruct
  void init() {
    log.info("rabbit username {}", this.rabbitUsername);
    log.info("rabbit password {}", this.rabbitPassword);
    log.info("rabbit host {}", this.rabbitHost);
    log.info("rabbit port {}", this.rabbitPort);
  }
}
