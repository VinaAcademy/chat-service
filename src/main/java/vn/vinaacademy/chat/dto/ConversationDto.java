package vn.vinaacademy.chat.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vinaacademy.chat.entity.enums.ConversationType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
  private String id;
  private String name;
  private String avatarUrl;
  private String createdAt;
  private String updatedAt;
  private String title;
  private ConversationType type;
  private MessageDto lastMessage;
  private Instant lastMessageAt;
  private List<MemberDto> members;
}
