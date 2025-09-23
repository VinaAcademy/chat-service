package vn.vinaacademy.chat.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import vn.vinaacademy.chat.entity.enums.MemberRole;
import vn.vinaacademy.chat.entity.id.ConversationMemberId;

@Entity
@Table(
    name = "conversation_member",
    indexes = {
      @Index(name = "idx_member_user", columnList = "user_id, conversation_id"),
      @Index(name = "idx_member_conv", columnList = "conversation_id"),
      @Index(
          name = "idx_member_lastread",
          columnList = "conversation_id, user_id, last_read_at DESC")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMember {

  @EmbeddedId private ConversationMemberId id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId("conversationId")
  @JoinColumn(
      name = "conversation_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_member_conv"))
  private Conversation conversation;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  @Builder.Default
  private MemberRole role = MemberRole.MEMBER;

  @CreationTimestamp
  @Column(name = "joined_at", nullable = false, updatable = false)
  private Instant joinedAt;

  @Column(name = "left_at")
  private Instant leftAt;

  @Column(name = "mute_until")
  private Instant muteUntil;

  @Column(name = "last_read_msg_id")
  private java.util.UUID lastReadMsgId;

  @Column(name = "last_read_at")
  private Instant lastReadAt;
}
