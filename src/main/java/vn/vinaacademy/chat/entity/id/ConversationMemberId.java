package vn.vinaacademy.chat.entity.id;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ConversationMemberId implements Serializable {
  @Column(name = "conversation_id", nullable = false)
  private UUID conversationId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  public ConversationMemberId() {}

  public ConversationMemberId(UUID conversationId, UUID userId) {
    this.conversationId = conversationId;
    this.userId = userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConversationMemberId that)) return false;
    return Objects.equals(conversationId, that.conversationId)
        && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(conversationId, userId);
  }
}
