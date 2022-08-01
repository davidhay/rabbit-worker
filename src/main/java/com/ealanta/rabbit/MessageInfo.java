package com.ealanta.rabbit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageInfo {

  private String id;
  private String value;
  private Boolean isRedeliver;

  boolean isShutdown() {
    return "shutdown".equalsIgnoreCase(value);
  }
}
