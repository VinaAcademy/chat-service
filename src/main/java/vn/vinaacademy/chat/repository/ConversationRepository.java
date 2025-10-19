package vn.vinaacademy.chat.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vinaacademy.chat.entity.Conversation;
import vn.vinaacademy.chat.repository.projection.ConversationUnreadDto;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

  @Query(
      "SELECT c FROM Conversation c "
          + "JOIN FETCH c.members m "
          + "WHERE c.type = vn.vinaacademy.chat.entity.enums.ConversationType.DIRECT "
          + "AND EXISTS (SELECT 1 FROM c.members member1 WHERE member1.id.userId = :currentUserId) "
          + "AND EXISTS (SELECT 1 FROM c.members member2 WHERE member2.id.userId = :recipientId)")
  List<Conversation> findOneByUserIds(
      @Param("currentUserId") UUID currentUserId, @Param("recipientId") UUID recipientId);

  @EntityGraph(attributePaths = "members")
  @Query(
"""
      SELECT c FROM Conversation c
      WHERE c.id IN (
        SELECT m.conversation.id
        FROM ConversationMember m
        WHERE m.id.userId = :userId AND m.leftAt IS NULL
      )
      ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC
""")
  List<Conversation> findConversationsByUserId(@Param("userId") UUID userId);

  @Query(
"""
      SELECT new vn.vinaacademy.chat.repository.projection.ConversationUnreadDto(
          c.id,
          COUNT(m.id)
      )
      FROM Conversation c
      JOIN c.members cm
      LEFT JOIN c.messages m
           ON m.createdAt > COALESCE(cm.lastReadAt, '1970-01-01T00:00:00Z')
           AND m.senderId <> cm.id.userId
      WHERE cm.id.userId = :userId
      GROUP BY c.id
      ORDER BY c.id
""")
  List<ConversationUnreadDto> findUnreadCountPerConversation(@Param("userId") UUID userId);
}
