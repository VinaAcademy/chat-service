package vn.vinaacademy.chat.service;

import java.util.List;
import java.util.UUID;
import vn.vinaacademy.chat.dto.MessageDto;
import vn.vinaacademy.chat.dto.request.GroupMessage;
import vn.vinaacademy.chat.dto.request.PrivateMessage;

public interface MessageService {
  List<MessageDto> getMessagesByRecipientId(UUID recipientId, int page, int size);
  
  List<MessageDto> getMessagesByConversationId(UUID conversationId, int page, int size);

  void sendPrivateMessage(PrivateMessage messageDto, UUID senderId);
  
  void sendGroupMessage(GroupMessage messageDto, UUID senderId);
}
