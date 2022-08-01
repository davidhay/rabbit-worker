package com.ealanta.rabbit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExitingShutdownHandler implements ShutdownHandler{

  public static final int NORMAL_TERMINATION = 0;
  @Override
  public void handleShutdown(MessageInfo messageInfo) {
    log.info("The message [{}] caused a shutdown", messageInfo);
    System.exit(NORMAL_TERMINATION);
  }
}
