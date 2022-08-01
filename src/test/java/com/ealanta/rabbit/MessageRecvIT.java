package com.ealanta.rabbit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Slf4j
@ActiveProfiles("test")
class MessageRecvIT extends BaseRabbitTCTest {

  private static final int MESSAGES_TO_SEND = 5;
  private static final int MESSAGES_TO_RECV = MESSAGES_TO_SEND;

  @Autowired
  AmqpAdmin admin;

  @Autowired
  RabbitTemplate template;

  @Autowired
  Queue qBean;

  @MockBean
  MessageProcessor mProcessor;

  @MockBean
  ShutdownHandler mShutdownHandler;

  List<MessageInfo> messageInfos;

  private CountDownLatch latch;

  @Test
  void testSendAndRecvMessages() throws InterruptedException {
    Map<String, Long> messages = new HashMap<>();
    this.latch = new CountDownLatch(MESSAGES_TO_RECV);
    for (long i = 0; i < MESSAGES_TO_SEND; i++) {
      messages.put(sendTestMessage(i), i);
    }
    //okay is true only after we've recvd 10 messages
    boolean okay = latch.await(3, TimeUnit.SECONDS);
    for (int i = 0; i < messageInfos.size(); i++) {
      log.info("[{}]{}", i, messageInfos.get(i));
    }
    if (!okay) {
      fail("problem waiting for messageInfos");
    }

    verify(mProcessor, times(MESSAGES_TO_RECV)).processMessage(any(MessageInfo.class));
    verifyNoMoreInteractions(mProcessor, mShutdownHandler);
  }

  String sendTestMessage(long value) {
    MessageProperties props = new MessageProperties();
    props.setMessageId(UUID.randomUUID().toString());
    Message msg = new Message(String.valueOf(value).getBytes(StandardCharsets.UTF_8), props);
    template.send(qBean.getActualName(), msg);
    return props.getMessageId();
  }

  @BeforeEach
  void setup() {
    this.messageInfos = new ArrayList<>();
    admin.purgeQueue(qBean.getActualName());
    log.info("q[{}]purged prior to test", qBean.getActualName());

    doAnswer(invocation -> {
      assertEquals(1, invocation.getArguments().length);
      MessageInfo info = invocation.getArgument(0);
      messageInfos.add(info);
      this.latch.countDown();
      return false;
    }).when(mProcessor).processMessage(any(MessageInfo.class));
  }

}
