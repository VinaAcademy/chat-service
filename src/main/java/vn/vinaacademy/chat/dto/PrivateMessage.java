package vn.vinaacademy.chat.dto;

import lombok.Data;

@Data
public class PrivateMessage {
  private String toUserId;
  private String content;
  private Long timestamp;
}
