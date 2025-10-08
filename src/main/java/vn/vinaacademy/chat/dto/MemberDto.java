package vn.vinaacademy.chat.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vinaacademy.chat.entity.enums.MemberRole;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {

  private UUID memberId;

  private String username;

  private String fullName;

  private String avatarUrl;

  private UUID conversationId;

  @Builder.Default private MemberRole role = MemberRole.MEMBER;

  private Instant joinedAt;

  private Instant leftAt;

  private Instant muteUntil;

  private UUID lastReadMsgId;

  private Instant lastReadAt;
}
