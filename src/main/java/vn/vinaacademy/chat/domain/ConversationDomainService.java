package vn.vinaacademy.chat.domain;

import java.util.List;
import java.util.UUID;
import vn.vinaacademy.chat.entity.Conversation;

public interface ConversationDomainService {

  Conversation createDirectConversation(UUID currentUserId, UUID recipientId);

  Conversation getOrCreateDirectConversation(UUID currentUserId, UUID recipientId);

  boolean isUserInConversation(UUID currentUserId, Conversation conversation);
  
  Conversation createGroupConversation(UUID creatorId, String title, List<UUID> memberIds);
}
