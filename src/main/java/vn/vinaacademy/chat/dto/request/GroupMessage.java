package vn.vinaacademy.chat.dto.request;

import java.util.UUID;
import lombok.Data;
import vn.vinaacademy.chat.entity.enums.MessageType;

@Data
public class GroupMessage {
  private String conversationId;
  private MessageType type;
  private String textContent;
  private UUID fileId;
  private String fileName;
  private Long fileSize;
}
