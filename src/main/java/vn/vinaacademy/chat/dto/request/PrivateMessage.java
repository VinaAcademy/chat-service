package vn.vinaacademy.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;
import vn.vinaacademy.chat.entity.enums.MessageType;

@Data
public class PrivateMessage {
  @NotNull private UUID recipientId;
  @NotNull private MessageType type;
  private String textContent;
  private UUID fileId;
  private String fileName;
  private Long fileSize;
}
