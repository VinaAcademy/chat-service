package vn.vinaacademy.chat.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import vn.vinaacademy.chat.entity.enums.MessageType;

@Entity
@Table(
    name = "message",
    indexes = {
      @Index(
          name = "idx_msg_conv_paging",
          columnList = "conversation_id, created_at DESC, id DESC"),
      @Index(name = "idx_msg_conv_seq", columnList = "conversation_id, seq DESC"),
      @Index(name = "idx_msg_sender", columnList = "conversation_id, sender_id, created_at DESC")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
  @Id @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "conversation_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_msg_conv"))
  private Conversation conversation;

  @Column(name = "sender_id", nullable = false)
  private UUID senderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private MessageType type; // TEXT | IMAGE | FILE

  @Column(name = "text_content", columnDefinition = "text")
  private String textContent;

  // file meta (nếu IMAGE/FILE)
  @Column(name = "file_id")
  private UUID fileId;

  @Column(name = "file_name")
  private String fileName;

  @Column(name = "file_size")
  private Long fileSize;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Column(name = "seq", insertable = false, updatable = false)
  private Long seq;

  @PrePersist
  @PreUpdate
  private void validateContent() {
    if (type == MessageType.TEXT) {
      if (textContent == null || textContent.isBlank())
        throw new IllegalArgumentException("TEXT message must have text_content");
    } else {
      if (fileId == null || fileName == null || fileName.isBlank())
        throw new IllegalArgumentException(type + " message must have file meta");
    }
  }
}
