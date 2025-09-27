package vn.vinaacademy.chat.service;

import java.util.List;
import java.util.UUID;
import vn.vinaacademy.chat.dto.ConversationDto;

public interface ConversationService {
  ConversationDto getConversationById(UUID conversationId);

  ConversationDto getDirectConversation(UUID userId1, UUID userId2);
  
  List<ConversationDto> getAllConversationsByCurrentUser();
  
  ConversationDto createGroupConversation(String title, List<UUID> memberIds);
  
  void markConversationAsRead(UUID conversationId);
}
