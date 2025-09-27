package vn.vinaacademy.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;
import vn.vinaacademy.chat.entity.ConversationMember;
import vn.vinaacademy.chat.entity.id.ConversationMemberId;

public interface ConversationMemberRepository
    extends JpaRepository<ConversationMember, ConversationMemberId> {
  @Modifying
  @Query(
      "UPDATE ConversationMember cm SET cm.lastReadMsgId = :messageId, cm.lastReadAt = :readAt "
          + "WHERE cm.id.conversationId = :conversationId AND cm.id.userId = :userId")
  void updateLastReadMessage(
      @Param("conversationId") UUID conversationId,
      @Param("userId") UUID userId,
      @Param("messageId") UUID messageId,
      @Param("readAt") Instant readAt);
}
