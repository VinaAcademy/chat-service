package vn.vinaacademy.chat.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.vinaacademy.chat.entity.Conversation;
import vn.vinaacademy.chat.entity.Message;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  List<Message> findTop50ByConversationAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(
      Conversation conversation, Instant before);

  List<Message> findTop50ByConversationAndSeqLessThanOrderBySeqDesc(
      Conversation conversation, Long seqCursor);

  List<Message> findTop1ByConversationOrderByCreatedAtDescIdDesc(Conversation conversation);

  List<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);
}
