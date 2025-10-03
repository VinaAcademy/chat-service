package vn.vinaacademy.chat.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vinaacademy.chat.entity.Message;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  List<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

  @Query(
      "SELECT m FROM Message m WHERE m.conversation.id = :conversationId "
          + "ORDER BY m.createdAt DESC, m.id DESC "
          + "LIMIT 1")
  Optional<Message> findLastMessageByConversationId(@Param("conversationId") UUID conversationId);
}
