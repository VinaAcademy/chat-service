package vn.vinaacademy.chat.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UuidGenerator;
import vn.vinaacademy.chat.entity.enums.ConversationType;

@Entity
@Table(
    name = "conversation",
    indexes = {@Index(name = "idx_conv_last", columnList = "last_msg_at DESC, id DESC")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

  @Id @UuidGenerator private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ConversationType type; // DIRECT | GROUP

  @Column(columnDefinition = "text")
  private String title; // null với DIRECT

  @OneToMany(
      mappedBy = "conversation",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  private List<ConversationMember> members;

  @Column(name = "avatar_file_id")
  private UUID avatarFileId;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_msg_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private Message lastMessage; // optional

  @Column(name = "last_msg_at")
  private Instant lastMessageAt;

  public void setLastMessage(Message lastMessage) {
    this.lastMessage = lastMessage;
    if (lastMessage != null) {
      var msgConv = lastMessage.getConversation();
      if (msgConv != null && this.id != null && !this.id.equals(msgConv.getId())) {
        throw new IllegalArgumentException(
            "Message's conversation does not match this conversation");
      }
      this.lastMessageAt = lastMessage.getCreatedAt();
    } else {
      this.lastMessageAt = null;
    }
  }
}
