package vn.vinaacademy.chat.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vinaacademy.chat.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

  @Query(
      value =
          "SELECT c FROM Conversation c "
              + "JOIN FETCH c.members "
              + "WHERE c.type = vn.vinaacademy.chat.entity.enums.ConversationType.DIRECT "
              + "AND :currentUserId IN (SELECT m.id.userId FROM c.members m) "
              + "AND :recipientId IN (SELECT m.id.userId FROM c.members m)")
  Optional<Conversation> findOneByUserIds(UUID currentUserId, UUID recipientId);
  
  @Query("SELECT DISTINCT c FROM Conversation c " +
         "JOIN FETCH c.members m " +
         "WHERE m.id.userId = :userId " +
         "AND m.leftAt IS NULL " +
         "ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC")
  List<Conversation> findConversationsByUserId(@Param("userId") UUID userId);
}
