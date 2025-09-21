package vn.vinaacademy.chat.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import vn.vinaacademy.chat.entity.enums.MessageType;

@Entity
@Table(
    name = "message",
    indexes = {
      @Index(
          name = "idx_msg_conv_paging",
          columnList = "conversation_id, created_at DESC, id DESC"),
      @Index(name = "idx_msg_conv_seq", columnList = "conversation_id, seq DESC")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

  @Id @GeneratedValue private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "conversation_id", nullable = false)
  private Conversation conversation;

  @Column(name = "sender_id", nullable = false)
  private UUID senderId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private MessageType type;

  @Column(name = "text_content", columnDefinition = "TEXT")
  private String textContent;

  // Metadata file
  @Column(name = "file_id")
  private UUID fileId;

  @Column(name = "file_name")
  private String fileName;

  @Column(name = "file_size")
  private Long fileSize;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  // BIGSERIAL -> chỉ đọc, không update từ app
  @Column(name = "seq", insertable = false, updatable = false)
  private Long seq;
}
