package vn.vinaacademy.chat.dto;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vinaacademy.chat.entity.enums.MessageType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto implements Serializable {
  private UUID id;
  private Long seq;
  private UUID conversationId;
  private UUID senderId;
  private MessageType type;
  private String textContent;
  private UUID fileId;
  private String fileName;
  private Long fileSize;
  private String createdAt;
  private String deletedAt;
}
