package vn.vinaacademy.chat.dto.request;

import java.util.UUID;
import lombok.Data;
import vn.vinaacademy.chat.entity.enums.MessageType;

@Data
public class PrivateMessage {
  private String recipientId;
  private MessageType type;
  private String textContent;
  private UUID fileId;
  private String fileName;
  private Long fileSize;
}
