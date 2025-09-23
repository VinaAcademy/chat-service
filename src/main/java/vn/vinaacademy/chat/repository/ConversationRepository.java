package vn.vinaacademy.chat.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.vinaacademy.chat.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {}
